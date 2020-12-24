import requests
import logging
import time
import sys
import json
import google.auth
import google.auth.exceptions
import google.auth.transport.requests
import constants

logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)

def create_compute_profile(api_endpoint, credential_token):
    ''' Creates CDF compute profiles
    
        parameters:

        api_endpoint(string): Endpoint of cdf instance

        credential_token(string): credential token
    '''
    bearer_token = str('Bearer ' + credential_token).strip()

    headers = {
        'Content-Type': 'application/json',
        'Authorization': bearer_token
    }
    
    for json_url,profile in zip(constants.json_path,constants.profiles):
        url = api_endpoint + "/v3/namespaces/default/profiles/{}".format(profile) 
        file = open(json_url).read()
        json_file = json.loads(file)
        json_body = json.dumps(json_file)
            
        try:
            logger.info("Creating Compute profile {}...".format(profile))
            response = requests.put(url=url, headers=headers, data=json_body)
        except requests.exceptions.Timeout as e:
            logger.debug("Request timed out. {}".format(e))
            logger.debug("Trying again in 30s")
            time.sleep(30)
            response = requests.get(url=url, headers=headers)

        if response.status_code == 200:
            logger.info("Response: {} {}".format(response.status_code, response.reason))
            logger.info(" compute profile {} created successfully.".format(profile))
        else:
            logger.error("Could not create compute profile {}".format(profile))
print(create_compute_profile.__doc__)

def load_credentials():
    '''Loads credentials of project'''
    try:
        logger.info("Checking credentials...")
        credential_scopes = ["https://www.googleapis.com/auth/cloud-platform"]
        credentials, project_id = google.auth.default(credential_scopes)

        request = google.auth.transport.requests.Request()
        credentials.refresh(request)

        logger.info("Project ID set to '{}'".format(project_id))

        return credentials.token, project_id
    except google.auth.exceptions.DefaultCredentialsError as e:
        logger.error("Could not load credentials. Please set 'GOOGLE_APPLICATION_CREDENTIALS' to the path of a "
                     "valid service account JSON key or set application default credentials using 'gcloud auth "
                     "application-default login'")
        raise RuntimeError(e)
print(load_credentials.__doc__)

def get_instance_details(credential_token, project_id):
    '''Get details of cdf instance.
    
        parameters:

        credential_token (string): credential token

        project_id(string): project id

        returns:

        instance_details: details of cdf instance
    '''
    bearer_token = str('Bearer ' + credential_token).strip()
    url = "https://datafusion.googleapis.com/v1beta1/projects/{}/locations/{}/instances/{}".format(project_id, region,
                                                                                                   instance_name)
    headers = {
        'Content-Type': 'application/json',
        'Authorization': bearer_token
    }

    try:
        logger.info("Getting details of instance '{}'.".format(instance_name))
        response = requests.get(url=url, headers=headers)
    except requests.exceptions.Timeout as e:
        logger.debug("Request timed out. {}".format(e))
        logger.debug("Trying again in 30s")
        time.sleep(30)
        response = requests.get(url=url, headers=headers)

    if response.status_code == 200:
        logger.info("Response: {} {}".format(response.status_code, response.reason))
    else:
        logger.error(
            "Unable to connect to CDF instance '{}'. Response: {} {}".format(instance_name, response.status_code,
                                                                             response.reason))
        raise RuntimeError(
            "Unable to connect to CDF instance '{}'. Response: {} {}".format(instance_name, response.status_code,
                                                                             response.reason))

    instance_details = json.loads(response.content.decode("UTF-8"))
    return instance_details
print(get_instance_details.__doc__)

def get_artifact_version(artifact_name, scope, api_endpoint, headers):
    '''Gets artifact version of plugins

        parameters:

        artifact_name(string): Name of artifact

        scope(string): scope of artifact. SYSTEM/USER

    '''
    url = api_endpoint + "/v3/namespaces/default/artifacts/{}?scope={}".format(artifact_name, scope)

    try:
        logger.info("Getting details of artifact '{}'.".format(artifact_name))
        response = requests.get(url=url, headers=headers)
    except requests.exceptions.Timeout as e:
        logger.debug("Request timed out. {}".format(e))
        logger.debug("Trying again in 30s")
        time.sleep(30)
        response = requests.get(url=url, headers=headers)

    if response.status_code == 200:
        logger.info("Checking latest version for artifact '{}'".format(artifact_name, response.status_code,
                                                                       response.reason))
        artifact_details = json.loads(response.text)
        artifact_version = artifact_details[0]["version"]

        return artifact_version
    else:
        logger.error("Could not retrieve details of artifact '{}'. Response: {} {}", artifact_name,
                     response.status_code, response.reason)
print(get_artifact_version.__doc__)

def change_plugin_version(pipeline_config, latest_version, pipeline_stage):
    ''' Change the plugin version to lastest version

        parameters:

        pipeline_config(string): configuration of pipeline with plugin verions

        latest_version(string): plugin latest version

        pipeline_stage(string): stage of cdf pipeline

        returns:

        pipeline_config(string): Details of pipeline.
    '''

    logger.info("Upgrading the pipeline configuration to use the latest version")
    pipeline_config["config"]["stages"][pipeline_stage]["plugin"]["artifact"]["version"] = latest_version
    return pipeline_config
print(change_plugin_version.__doc__)

def deploy_pipeline(api_endpoint, credential_token, pipeline_name,pipeline_json_path):
    ''' Deploys CDF pipeline

        parameters:

        pipeline_name(string): Name of the pipeline

        pipeline_json_path(string): Path of pipeline's exported json file.

        returns:

        payload: json file of pipeline
    '''
    bearer_token = str('Bearer ' + credential_token).strip()

    headers = {
        'Content-Type': 'application/json',
        'Authorization': bearer_token
    }
    url = api_endpoint + "/v3/namespaces/default/apps/{}".format(pipeline_name)
    
    file = open(pipeline_json_path).read()
    pipeline_config = json.loads(file)

    source_plugin_name = pipeline_config["config"]["stages"][0]["plugin"]["artifact"]["name"]
    source_plugin_version = pipeline_config["config"]["stages"][0]["plugin"]["artifact"]["version"]

    latest_version = get_artifact_version(artifact_name=source_plugin_name, scope="SYSTEM", api_endpoint=api_endpoint,
                                          headers=headers)
                                        
    if source_plugin_version != latest_version:
        logger.info("'{}' version does not match the latest version".format(source_plugin_name))
        pipeline_config = change_plugin_version(pipeline_config, latest_version, 0)

    sink_plugin_name = pipeline_config["config"]["stages"][1]["plugin"]["artifact"]["name"]
    sink_plugin_version = pipeline_config["config"]["stages"][1]["plugin"]["artifact"]["version"]

    latest_version = get_artifact_version(artifact_name=sink_plugin_name, scope="system", api_endpoint=api_endpoint,
                                          headers=headers)
    if sink_plugin_version != latest_version:
        logger.info("'{}' version does not match the latest version.".format(source_plugin_name))
        pipeline_config = change_plugin_version(pipeline_config, latest_version, 1)

    payload = json.dumps(pipeline_config)

    try:
        logger.info("Deploying '{}' pipeline...".format(pipeline_name))
        response = requests.put(url=url, headers=headers, data=payload)
    except requests.exceptions.Timeout as e:
        logger.debug("Request timed out. {}".format(e))
        logger.debug("Trying again in 30s")
        time.sleep(30)
        response = requests.get(url=url, headers=headers, data=payload)

    if response.status_code == 200:
        logger.info("Response: {} {}".format(response.status_code, response.reason))
        logger.info("'{}' deployed successfully.".format(pipeline_name))
    else:
        logger.error("Could not deploy '{}' pipeline. Response: {} {}".format(pipeline_name, response.status_code,
                                                                              response.reason))
        raise RuntimeError("Could not deploy '{}' pipeline. Response: {} {}".format(pipeline_name, response.status_code,
                                                                                    response.reason))
    return payload
print(deploy_pipeline.__doc__)

def run_pipeline(api_endpoint, credential_token, pipeline_name,payload):
    ''' Runs CDF pipeline

        parameters:

        pipeline_name(string): Name of the cdf pipeline

        payload(string): exported json file of pipeline
    '''    
    bearer_token = str('Bearer ' + credential_token).strip()

    headers = {
        'Content-Type': 'application/json',
        'Authorization': bearer_token
    }
    url = api_endpoint + "/v3/namespaces/default/apps/{}/workflows/DataPipelineWorkflow/start".format(pipeline_name)

    try:
        logger.info("Starting '{}' pipeline...".format(pipeline_name))
        response = requests.post(url=url, headers=headers, data=payload)
    except requests.exceptions.Timeout as e:
        logger.debug("Request timed out. {}".format(e))
        logger.debug("Trying again in 30s")
        time.sleep(30)
        response = requests.get(url=url, headers=headers)

    if response.status_code == 200:
        logger.info("Response: {} {}".format(response.status_code, response.reason))
        logger.info("'{}' run started successfully.".format(pipeline_name))
    else:
        logger.error("Could not start '{}' pipeline. Response: {} {}".format(pipeline_name, response.status_code,
                                                                             response.reason))
        raise RuntimeError("Could not start '{}' pipeline. Response: {} {}".format(pipeline_name, response.status_code,
                                                                                   response.reason))
print(run_pipeline.__doc__)

def runtime_argument(pipeline_name):
    ''' Provides runtime arg for pipeline

        parameters:

        pipeline_name(string): Name of pipeline

        returns:

        runtime_arg_list: list of runtime arguments
    '''    
    runtime_arg_list=[]
    runtime_arg={}
    runtime_arg['connection_string']= connection_string
    runtime_arg['system.profile.name']= compute_profile
    runtime_arg_list.append(json.dumps(runtime_arg))
    return runtime_arg_list
print(run_pipeline.__doc__)

def main(region, instance_name, compute_profile, pipeline_name, pipeline_json_path, connection_string):
    ''' Main function to create,deploy,run pipeline

        parameters:

        region(string): Region of CDF instance

        instance_name(string): Name of CDF instance

        compute_profile(string): Name of compute profile to be use

        pipeline_name(string): Name of CDF pipeline

        pipeline_json_path(string): Path of json file of pipeline

        connection_string(string): JDBC connection string of database instance
    '''

    credential_token, project_id = load_credentials()
    instance_details = get_instance_details(credential_token, project_id)
    instance_state = instance_details['state']

    if instance_state == "RUNNING":
        api_endpoint = instance_details['apiEndpoint']
        logger.info("API endpoint of instance '{}': {}".format(instance_name, api_endpoint))
        create_compute_profile(api_endpoint, credential_token)

        deploy_pipeline(api_endpoint, credential_token, pipeline_name=pipeline_name, pipeline_json_path=pipeline_json_path)
        runs_payload = runtime_argument(pipeline_name=pipeline_name)
        for payload in runs_payload:
            run_pipeline(api_endpoint, credential_token, pipeline_name=pipeline_name, payload=payload)
    else:
        logger.error("Instance status: {}".format(instance_state))
        logger.error("CDF instance '{}' is not running. Aborting data pipeline.".format(instance_name))
        raise RuntimeError("CDF instance '{}' is not running. Aborting data pipeline.".format(instance_name))
print(main.__doc__)

if __name__ == '__main__':
    if len(sys.argv) <= 6:
        sys.exit('region, instance_name, compute_profile, pipeline_name, pipeline_json_path, "connection_string"')
    region = sys.argv[1]
    instance_name = sys.argv[2]
    compute_profile = sys.argv[3]
    pipeline_name = sys.argv[4]
    pipeline_json_path = sys.argv[5]
    connection_string = sys.argv[6]
    main(region, instance_name, compute_profile, pipeline_name, pipeline_json_path, connection_string)
