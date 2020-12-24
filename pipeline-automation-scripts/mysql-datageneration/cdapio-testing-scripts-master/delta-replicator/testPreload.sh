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
    "--schemaPath")   set -- "$@" "-s" ;;
	"--chunkSize")   set -- "$@" "-c" ;;
	"--DB")   set -- "$@" "-d" ;;	
    *)        set -- "$@" "$arg"
  esac
done
# Default behavior
host="xx.xx.xx.xx"
user="username"
password="password"
timezone="PST"
port=1111
schema_path="schemaPath"
chunk_size=10
DB="mysql_db"
# Parse short options
while getopts "o":"u":"p":"z":"t":"s":"c":"d": opt
do
  case "$opt" in
    "o") host=${OPTARG} ;;
    "u") user=${OPTARG} ;;
    "p") password=${OPTARG} ;;
    "z") timezone=${OPTARG} ;;
    "t") port=${OPTARG} ;;
    "s") schema_path=${OPTARG} ;;
	"c") chunk_size=${OPTARG} ;;
	"d") DB=${OPTARG} ;;
    "h") echo "Example: ./testPreload.sh --host localhost --user root --password pwd --timezone PST --port 1433 --schemaPath 'schemaPath' --chunkSize 10 --DB 'sql_db'"; exit 0 ;;
    "?") echo "Invalid Option(s)" >&2; exit 1 ;;
  esac
done
echo "========================================================"
echo "host : $host"
echo "user : $user"
echo "password : $password"
echo "timzeone : $timezone"
echo "port : $port"
echo "schema path : $schema_path"
echo "chunk size : $chunk_size"
echo "DB : $DB"
echo "========================================================"

# comments
CP=$PWD/jdbc/mysql-connector-java-8.0.20.jar
CP2=$PWD/lib/gson-2.2.2.jar
CP3=$PWD/lib/HikariCP-3.4.5.jar
CP4=$PWD/lib/slf4j-api-1.7.2.jar
echo "CP:$CP"
echo "CP2: $CP2"
echo "CP3: $CP3"
echo "CP4: $CP4"
printf "\n\n**Starting Delta Replicator SQL Server Testing**\n"
echo $CP
# Compile current java files
javac -cp .:$CP:$CP2:$CP3:$CP4 -d code/output code/*.java
printf "\n\n==Start Inserting Rows into Table(s)==\n"
now=$(date)
printf "Current Time:%s\n" "$now"
java -cp ./code/output:$CP:$CP2:$CP3:$CP4 MySqlServerPreLoadTest $host $user $password $timezone $port $schema_path $chunk_size $DB
