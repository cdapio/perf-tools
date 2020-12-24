#Scripts for MySql Server Delta Replicator
#Summary
#This script is used to automate the process of creating databases and tables for the provided input schemas and inserting/updating events to MySql database periodically.
#Prerequisite
1. Install JVM
#Variables for testPreload.sh
1. host : ip of the MySql database
2. user : username of the MySql database
3. password : password of the MySql database
4. timezone : timezone setting of MySql database (default: PST)
5. port : port number of the MySql database (default: 1433)
6. schemaPath : num of tables to create
7. chunkSize: Adjust the event Size of the record
8. DB : name of the database

#How to execute
./testPreload.sh --host ipOfDbInstance --user root --password 'pwd' --timezone PST --port 3306 --schemaPath '/home/mysql/cdapio-testing-scripts-master/delta-replicator/resources' chunkSize 500 --DB 'mysql_db'





#Configure, Create and Run the CDF Replicate Pipeline for the above DB where Preload has done
#Update db.properties

#Update jdbcUrl with drivername, hostname(ip address), port number and databaseName

jdbcUrl=jdbc:mysql://xx.xxx.xx.xx:nnnn/dbname
#Variables for testCdc.sh
1. schemaPath : schemaPath for input files
2. durationMins : duration to keep running the script in minutes 
3. batchSize : number of rows to insert per table per interval
4. updateFactor : the factor between number of insert and update events (default: 1). For example, if the factor is set to 0.5 and batch size is 10, it means that this script will generate 10 insert events and 5 update events per interval.)
5. chunkSize : Adjusts the event Size of the record
6. configPath : path of db.properties
7. noOfThreads : any number eg: 1 to 500
8. periodInSecs : for given second batches will get insert
9. deleteFactor : if the factor is set to 0.3 and batch size is 10, it means that this script will generate 10 insert events and 3 update events per interval.  
#How to execute
sudo bash testCdc.sh --schemaPath '/home/mysql/cdapio-testing-scripts-master/delta-replicator/resources' --durationMins 1 --batchSize 40 --updateFactor 1.25 --chunkSize 500 --configPath '/home/mysql/cdapio-testing-scripts-master/delta-replicator/db.properties' --noOfThreads 1 --periodInSecs 1 --deleteFactor 0.30
