#!/bin/bash
# Transform long options to short ones
for arg in "$@"; do
  shift
  case "$arg" in
    "--help") set -- "$@" "-h" ;;
    "--schemaPath")   set -- "$@" "-s" ;;
    "--durationMins")   set -- "$@" "-d" ;;
    "--batchSize")   set -- "$@" "-b" ;;
    "--updateFactor")   set -- "$@" "-f" ;;
    "--chunkSize")   set -- "$@" "-c" ;;
    "--configPath")   set -- "$@" "-g" ;;	
    "--noOfThreads")   set -- "$@" "-e" ;;
    "--periodInSecs")   set -- "$@" "-a" ;;
    "--deleteFactor")   set -- "$@" "-x" ;;	
    *)        set -- "$@" "$arg"
  esac
done
# Default behavior
schema_path="schema_path"
terminate_duration=1
batch_size=10
update_factor=0.5
chunk_size=10
config_path="/home/cdapio-testing-scripts-master/delta-replicator/resources/db.properties"
no_threads=10
period_in_secs=1
deleteFactor=0.1
# Parse short options
while getopts "s":"d":"b":"h":"f":"c":"g":"e":"a":"x": opt
do
  case "$opt" in
    "s") schema_path=${OPTARG} ;;
    "d") terminate_duration=${OPTARG} ;;
    "b") batch_size=${OPTARG} ;;
    "f") update_factor=${OPTARG} ;;
	"c") chunk_size=${OPTARG} ;;
    "g") config_path=${OPTARG} ;;
	"e") no_threads=${OPTARG} ;;
	"a") period_in_secs=${OPTARG} ;;
	"x") deleteFactor=${OPTARG} ;;	
    "h") echo "Example: ./testPreload.sh --schemaPath 'schemaPath' --durationMins 15 --batchSize 1 --updateFactor 0.5 --chunkSize 10  --configPath 'configPath' --noOfThreads 100 --periodInSecs 1 --deleteFactor 0.1 " ; exit 0 ;;
    "?") echo "Invalid Option(s)" >&2; exit 1 ;;
  esac
done
echo "========================================================"
echo "schema path : $schema_path"
echo "terminate duration in minutes : $terminate_duration"
echo "batch size : $batch_size"
echo "update factor : $update_factor"
echo "chunk size : $chunk_size"
echo "config path : $config_path"
echo "no threads : $no_threads"
echo "period in secs : $period_in_secs"
echo "Delete Factor : $deleteFactor"
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
java -cp ./code/output:$CP:$CP2:$CP3:$CP4  MySqlServerCdcTest $host $user $password $timezone $port $schema_path $terminate_duration $batch_size $update_factor $chunk_size $config_path $no_threads $period_in_secs $deleteFactor
