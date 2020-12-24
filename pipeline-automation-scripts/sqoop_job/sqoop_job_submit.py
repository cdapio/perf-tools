import random
import re
import sys
import logging
from google.cloud import dataproc_v1 as dataproc
from google.cloud import storage

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger()

def submit_job(project_id, region, cluster_name, jdbc_string, gcs_bucket, mappers_task):
        ''' Creates sqoop job and submit job in the dataproc cluster.
        
            parameters:
            
            project_id(string): project ID of working project
            
            region(string): Region of dataproc cluster
            
            cluster_name(string): Name of dataproc cluster
            
            jdbc_string(string): JDBC string of database 
            
            gcs_bucket(string): GCS bucket path for target-dir.
            
            mappers_task(string): Number of mappers tasks
        '''

    # creates the job client
    job_client = dataproc.JobControllerClient(client_options={
        'api_endpoint': '{}-dataproc.googleapis.com:443'.format(region) 
    })
    random_string = random.randint(0, 255)
    target = str(gcs_bucket)+str(random_string)

    job = {
        'placement': {
            'cluster_name': cluster_name
        },
        'hadoop_job': {
            "args": [
                "import",
                "-Dorg.apache.sqoop.splitter.allow_text_splitter=true",
                "--driver",     #mssql-server jdbc driver name
                "com.microsoft.sqlserver.jdbc.SQLServerDriver",
                "--connection-manager",        #connection manager for sqoop job import
                "org.apache.sqoop.manager.GenericJdbcManager",
                "--connect",                   #jdbc connection of database
                jdbc_string,
                "--username",                  #username of sql server instance
                "SA",
                "--password",                  #password of sql server
                "sql-server1",
                "--query",                     #sql query to get data from databases
                "select * from lineitem where $CONDITIONS",
                "--target-dir",                #output target directory
                target,
                "--split-by",                  #split-by field name
                "L_ORDERKEY",
                "--m",                         #number of mappers
                mappers_task,
                "--boundary-query",            #Boundary query for particular table
                "SELECT MIN(L_ORDERKEY),MAX(L_ORDERKEY) FROM lineitem",
                ],
            "jar_file_uris": [                 #gcs path of required jar files stored in gcs bucket
                "gs://dataproc-sqoop_job-usecase/jar/sqoop-1.4.7.jar",
                "gs://dataproc-sqoop_job-usecase/jar/avro-1.9.2.jar",
                "gs://dataproc-sqoop_job-usecase/jar/avro-mapred-1.9.2.jar",
                "gs://dataproc-sqoop_job-usecase/jar/sqljdbc42.jar",
                ],
            "properties": {
                "dataproc:dataproc.conscrypt.provider.enable": "false"
                },
            "main_class": "org.apache.sqoop.Sqoop"
            }
    }
    print("Sqoop job command is : {}".format(job))
    try: 
        logger.info("Starting sqoop job...%")
        operation = job_client.submit_job_as_operation(
        request={"project_id": project_id, "region": region, "job": job}
    )
        logger.info("Running sqoop job...%")
        response = operation.result()
        matches = re.match("gs://(.*?)/(.*)", response.driver_output_resource_uri)
        output = (storage.Client().get_bucket(matches.group(1)))
        print("Job finished successfully:{}".format(output))
    except:
        print("Sqoop job execution failed")
    return 
print(submit_job.__doc__)

if __name__ == "__main__":
    '''Takes system arguments mentioned below and run submit_job function. '''
    if len(sys.argv) <= 6:
        sys.exit('python3 sqoop_job_submit.py project_id region cluster_name "jdbc_string" gcs_bucket splits')

    Project_id = sys.argv[1]
    Region = sys.argv[2]
    Cluster_name = sys.argv[3]
    jdbc_string = sys.argv[4]
    gcs_bucket = sys.argv[5]
    mappers_task = sys.argv[6]
    submit_job(Project_id, Region, Cluster_name, jdbc_string, gcs_bucket, mappers_task)
print(__name__.__doc__)