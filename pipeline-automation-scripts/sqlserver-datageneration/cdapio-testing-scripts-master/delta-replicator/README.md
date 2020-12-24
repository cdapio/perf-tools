#Scripts for Sql Server Delta Replicator
##Summary
#This script is used to automate the process of creating databases and tables and inserting/updating events to the Sql Server database periodically.
#Prerequisite
Install JVM
Variables for testPreDP.sh
1. host : hostname or ip of the SqlServer database
2. user : username of the SqlServer database
3. password : password of the SqlServer database
4. timezone : timezone setting of SqlServer database (default: PST)
5. port : port number of the SqlServer database (default: 1433)
6. noOfBodyColumns: num of tables to create (default: 100)
7. numOfTables : number of tables count (default: 100)
8. chunkSize : size of each field in bytes (default: 10)
9. DB : name of the database

#How to execute
./testPreDP.sh --host ipOfDbInstance --user username --password 'pwd' --timezone PST --port 1433 --numOfTables 1 --chunkSize 10 --noOfBodyColumns 5 --DB sql_db



#Configure, Create and Run the CDF Replicate Pipeline for the above DB where Preload has done
#Variables for testCdc.sh
1. host : hostname or ip of the SqlServer database
2. user : username of the SqlServer database
3. password : password of the SqlServer database
4. timezone : timezone setting of SqlServer database (default: PST)
5. port : port number of the SqlServer database (default: 1433)
6. numOfTables : number of tables count (default: 100)
7. chunkSize : size of each field in bytes (default: 10)
8. durationMins : duration to keep running the script in minutes
9. batchSize : number of rows to insert per table per interval
10. updateFactor : the factor between number of insert and update events (default: 1). For example, if the factor is set to 0.5 and batch size is 10, it means that this script will generate 10 insert events and 5 update events per interval.)
11. deleteFactor : if the factor is set to 0.3 and batch size is 10, it means that this script will generate 10 insert events and 3 update events per interval.
12. noOfBodyColumns: num of tables to create (default: 100)
13. DB: name of the database

(note that, the default interval is 1s, if you want to change, you will need to update variable 'PERIOD_IN_SECONDS' in the java code)

#How to execute

./testCdcDP.sh --host localhost ipOfDbInstance --user username --password 'pwd' --timezone PST --port 1433 --numOfTables 1 --chunkSize 10 --durationMins 5 --batchSize 100--updateFactor 1.25 --deleteFactor 0.25 --noOfBodyColumns 5 --DB test_db
