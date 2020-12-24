# Sqoop job execution steps

**Introduction**

This python script use dataproc cluster to submit sqoop job and stores output in GCS bucket.

**Prerequisites:**
* Create Dataproc cluster to submit the sqoop job(cluster configuration is mentioned in the cdf performance benchmarking sheet).
* Download sqoop-1.4.7.jar,
avro-1.9.2.jar,avro-mapred-1.9.2.jar,
sqljdbc42.jar and upload it into gcs bucket.
* Mention gcs path of above jar files in "jar_file_uris"
* Create GCS bucket(for target-dir) to store output data.Use gsutil command to create bucket 

        $gsutil mb gs://BUCKET_NAME

**To submit sqoop job in dataproc cluster use following command :**

    $ python3 sqoop_job_submit.py <project_id>  <region>  <cluster_name> <jdbc_string> <gcs_bucket> <mappers_task>

* project_id = Project ID 
* region = Name of dataproc cluster region.
* cluster_name = Name of static dataproc cluster.
* jdbc_string = JDBC string of database instance 
* gcs_bucket = Name of target gcs bucket
* mappers_task = Number of task to perform.

Example: python3 sqoop_job_submit.py  quantiphi-datafusion  europe-west1  cdf-benchmarking-final "jdbc:sqlserver://10.2.0.4:1433;databaseName=tpch_1000gb" gs://sql-server-output-data/lineitem  700