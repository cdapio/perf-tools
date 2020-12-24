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
    "--durationMins")   set -- "$@" "-d" ;;
    "--batchSize")   set -- "$@" "-b" ;;
    "--updateFactor")   set -- "$@" "-f" ;;
    "--deleteFactor")   set -- "$@" "-x" ;;	
    "--noOfBodyColumns")   set -- "$@" "-y" ;;	
    "--DB")   set -- "$@" "-w" ;;		
	
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
chunkSize=5000
terminate_duration=5
batch_size=1
update_factor=1.0
deleteFactor=0.1
# Parse short options
while getopts "o":"u":"p":"z":"t":"n":"s":"d":"b":"h":"f":"x":"y":"w": opt
do
  case "$opt" in
    "o") host=${OPTARG} ;;
    "u") user=${OPTARG} ;;
    "p") password=${OPTARG} ;;
    "z") timezone=${OPTARG} ;;
    "t") port=${OPTARG} ;;	
    "n") num_of_tables=${OPTARG} ;;
    "s") chunkSize=${OPTARG} ;;
    "d") terminate_duration=${OPTARG} ;;
    "b") batch_size=${OPTARG} ;;
    "f") update_factor=${OPTARG} ;;
    "x") deleteFactor=${OPTARG} ;;
    "y") noOfBodyColumns=${OPTARG} ;;
    "w") DB=${OPTARG} ;;	
    "h") echo "Example: ./testDP.sh --host localhost --user root --password pwd --timezone PST --port 1433 --numOfT
ables 100 --chunkSize 10000 --durationMins 15 --batchSize 1 --updateFactor 1.0 --deleteFactor 0.1 --noOfBodyColumns 5 --DB test_db"; exit 0 ;;
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
echo "row size in bytes : $chunkSize"
echo "terminate duration in minutes : $terminate_duration"
echo "batch size : $batch_size"
echo "update factor : $update_factor"
echo "delete factor : $deleteFactor"
echo "no of body columns : $noOfBodyColumns"
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
java -cp ./code/output:$CP SqlSerCdcTest $host $user $password $timezone $port $num_of_tables $chunkSize $terminate_duration $batch_size $update_factor $deleteFactor $noOfBodyColumns $DB
