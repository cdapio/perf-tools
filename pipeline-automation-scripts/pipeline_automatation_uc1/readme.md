# CDF Pipeline automation python script

This python script creates, deploy and run CDF data pipeline from exported json.

Python script creates 3 different compute profiles named 12wn_profile,25wn_profile and 50wn_profile.Compute profiles are created from 
compute_profile_1.json,compute_profile_2.json,compute_profile_3.json these json files.

Download Mssql-server jdbc driver for database plugin from [here](https://docs.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server?view=sql-server-ver15)

**To run Python script use following command :** 

    $python3 main.py <region> <instance_name> <compute_profile> <pipeline_name> <pipeline_json_path> <connection_string>

* region = Name of CDF instance region
* instance_name = Name of CDF instance
* compute_profile = Name of compute profile which mentioned above.
* pipeline_name = Name of the CDF pipeline.
* pipeline_json_path = Path of exported json file.
* connection_string = JDBC connection string in database plugin.

Example: $python3 main.py us-west1 auto-run 25wn_profile Sqlserver-To-BQ_700splits ./pipelines/Sqlserver-To-BQ_700splits-cdap-data-pipeline.json "jdbc:sqlserver://10.2.0.4:1433;databaseName=tpch_1000gb"