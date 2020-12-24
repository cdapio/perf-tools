#!/bin/bash
# Transform long options to short ones
for arg in "$@"; do
  shift
  case "$arg" in
    "--help") set -- "$@" "-h" ;;
    "--host") set -- "$@" "-o" ;;
    "--user")   set -- "$@" "-u" ;;
    "--password")   set -- "$@" "-p" ;;
    "--timezone")   set -- "$@" "-z" ;;
    "--port")   set -- "$@" "-t" ;;	
    "--numOfTables")   set -- "$@" "-n" ;;
    "--chunkSize")   set -- "$@" "-s" ;;
    "--noOfBodyColumns")   set -- "$@" "-f" ;;
    "--DB")   set -- "$@" "-d" ;;	
    *)        set -- "$@" "$arg"
  esac
done
# Default behavior
host="localhost"
user="root"
password="pwd"
timezone="PST"
port=1433
num_of_tables=100
chunkSize=10
noOfBodyColumns=5
# Parse short options
while getopts "o":"u":"p":"z":"t":"n":"s":"f":"d": opt
do
  case "$opt" in
    "o") host=${OPTARG} ;;
    "u") user=${OPTARG} ;;
    "p") password=${OPTARG} ;;
    "z") timezone=${OPTARG} ;;
    "t") port=${OPTARG} ;;	
    "n") num_of_tables=${OPTARG} ;;
    "s") chunkSize=${OPTARG} ;;
    "f") noOfBodyColumns=${OPTARG} ;;
    "d") DB=${OPTARG} ;;	
    "h") echo "Example: ./testDP.sh --host localhost --user root --password pwd --timezone PST --port 1433 --numOfT
ables 100 --chunkSize 10000 --noOfBodyColumns 5 --DB test_db"; exit 0 ;;
    "?") echo "Invalid Option(s)" >&2; exit 1 ;;
  esac
done
echo "========================================================"
echo "host : $host"
echo "user : $user"
echo "password : $password"
echo "timzeone : $timezone"
echo "port : $port"
echo "num of tables : $num_of_tables"
echo "chunkSize : $chunkSize"
echo "no Of Body Columns : $noOfBodyColumns"
echo "DB : $DB"
echo "========================================================"
# comments
CP=$PWD/jdbc/mssql-jdbc-8.2.1.jre8.jar
printf "\n\n**Starting Delta Replicator SQL Server Testing**\n"
echo $CP
# Compile current java files
javac -cp .:$CP -d code/output code/*.java
printf "\n\n==Start Inserting Rows into Table(s)==\n"
now=$(date)
printf "Current Time:%s\n" "$now"
java -cp ./code/output:$CP SqlSerPreLoadTest $host $user $password $timezone $port $num_of_tables $chunkSize $noOfBodyColumns $DB
