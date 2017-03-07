# MixedEmotions' Orchestrator

## Description

This MixedEmotions Orchestrator was developed by Paradigma Digital.

The code of this orchestrator will let users have an starting point on how to interact with the MixedEmotion Toolbox modules. It is written in scala and can interact with RESTservices and DockerServices deployed in Mesos with a Mesos-DNS as a discovery service.

#THE FOLLOWING SECTIONS SHOULD BE CONSIDERED AS WORK IN PROGRESS

# Installation
This project has been compiled using java 1.8, scala 2.10.4, sbt 0.13.5 and sbt-assembly 0.12.0.

# Compile
sbt assembly
You can find compiled jars in the [releases section](https://github.com/MixedEmotions/Orchestrator/releases) of this github.

# Usage
Run the following command:


`java -cp  MixedEmotionsExampleOrchestrator-assembly-0.15.jar orchestrator.ListFutureOrchestrator {confFilePath} {inputPath}`


# Configuration

## General Configuration File


Example:


	languages = ["es", "en"]
	modules = ["rest_topic_local","rest_concept_local","docker_spanish_topic_service"]
	elasticsearch {
	 ip = "mixednode2back"
	 port = 9300
	 clusterName = "Mixedemotions Elasticsearch"
	 indexName = "myanalyzed"
	}
	mesos_dns {
	 ip="mixednode2back"
	 port=8123
	}




	docker_conf_folder="/home/cnavarro/workspace/mixedemotions/me_extractors/DockerSparkPipeline/src/main/resources/dockerServices/"
	rest_conf_folder="/home/cnavarro/workspace/mixedemotions/me_extractors/DockerSparkPipeline/src/main/resources/restServices/"
	outputFilePath="/home/cnavarro/workspace/mixedemotions/temp/scalaOutputOut.txt"
	executionTimeoutSeconds=500




Fields description

 
* **modules**: Modules to be executed, in order. There are 2 types of modules: rest and docker modules. The modules names will be the configuration file name, preceded by “rest” or “docker”. The configuration file for the module should be in the corresponding folder and should end in ‘.conf’ .
* **elasticsearch**: Not used in the tutorial.
* **mesos-dns**: Address of the Mesos-dns api
* **docker_conf_folder**: Folder with the docker modules configurations.
* **rest_conf_folder**: Folder with the rest modules configurations.
* **output_file_path**: File in which the output will be wrote
* **ExecutionTimeoutSeconds**: Maximum number of seconds the application has to run or else will fail




As stated earlier, there are two types of services: docker and rest services. Each of them will have its configuration files in a different folder. The name of the configuration file should end in ‘.conf’ and be the name present in the ‘modules’ param, minus the ‘rest/docker_’ header.


For example, if I have this modules attribute: [“rest_service1”, “docker_service2”], in the folder stated in the “docker_conf_folder” attribute there should be a “service2.conf” file, and in the folder stated in “rest_conf_folder” there should be a “service1.conf” file.




## Service Conf File
There are two kinds of service configuration file, the rest configuration files and the docker configuration files. The fields in them are almost identical, excepting ip and port, that are unique for the rest services and the serviceId which is unique for the Docker services.

### Rest Service Conf File

Example:

	ip = "localhost"
	port = 32769
	method = "GET"
	requestUrl = "?text=${text}"
	outputField = "concepts"
	response.json.path = "result.concepts"
	body = ""
	requestDelayMs=500
	requestTimeoutSeconds=300




In this case, the service is called by a GET request to http://localhost:32769/?text=”the+text+to+analyze”


* **ip**: address of the host
* **port**: port of the host. (If the service does not use a port, just an address, this should be set to 80)
* **method**: REST method, at the moment GET and POST are supported.
* **requestUrl**: Everything after the slash of the port, including url params. The values to be included in the param values are put in the form of ${mapAttribute}, where mapAttribute is the key in the map obtained  after parsing the input json. For example, in a json like: 
  `                       {“id”:132, “lang”:”es”, “text”:”some text to analyze”}
?text=${text} will translate to ?text=”some+text+to+analyze”. `

* **response.json.path**: where to find the result in the json that the service responded with. The dots separate nested keys. For example, if the service responds with:
	{“result”:{“time”:0.444, “concepts”:[“banking”, “loan”]}}
 and response.json.path is “result.concepts”, the extracted result will be [“banking”, “loan”].
 If the response is just an array, an empty string (“”) will get that array as result.
 Internally dots are translated to the “\” operation of scala’s json4s. For more information, look the “xpath and HOFs” of http://json4s.org/ . If the key has inner dots, you will have to escape them using _DOT_. For example: entries.emotions.onyx:hasEmotion.http://www_DOT_gsi_DOT_dit_DOT_upm_DOT_es/ontologies/onyx/vocabularies/anew/ns#arousal
* **response.json.map**: when there are multiple fields in a json to extract. Uses the same syntax as response.json.path. They will be stored in a json object using as keys the responseMap keys. This object will be put inside the outputField. 
 For example:
 `	Response: {“result”:{“time”:0.444, “concepts”:[“banking”, “loan”]}}
	response.json.map {
 		extracted_time = “result.time”
		detected_concepts = “result.concepts”
	}`
 Will result into an object: {“extracted_time”:0.444, “detected_concepts”:[“banking”, “loan”]}
* **response.json.deleteString**: deletes the corresponding string in each obtained result ina response.json.map or response.json.path.
* **response.string.parse**: for responses that are not in json format. Expects a regex with some grouping.
* **response.json.pivotPath**: for responses that have multiple items as response and expect the entry to multiply itself by those multiple items. For example, if a text is to be divided n phrases. It requires also a pivotId and a pivotName.
* **pivotName**: Name of the field that will identify the parts of a whole that has been divided. For example, it could be “textId” if the service splits a text into phrases.
* **pivotId**: value to assign to the pivotName. It could use the ${} syntaxt of the request url if it is needed to input a value from the input map. For example, if your request has the form: {“text”:”Lorem impsum….”, “text_name”:”111”} you could use pivotId=”${textName}”.
* **outputField**: The key to be added to the input json with the result of the service request. If outputField is ‘concepts’, taking into account the previous examples, the result json will be this:
	 `{“id”:132, “lang”:”es”, “text”:”some text to analyze”, “concepts”:[“banking”, “loan”]}`
* **body** (optional): For POST requests, the key of the input json that will be used as body. For example if body=”videoPath” and the input json is: `{“id”:132, “lang”:”es”,”videoPath”:”/home/videos/video”}`
 The content of the body will be “/home/videos/video”.
* **RequestDelayMs**: Delay before http requests. Useful if the server might not be able to handle all the requests at once. Defaults to 500
* **RequestTimeoutSeconds**: Time to wait before considering a request failed. Useful if there are services that can halt unexpectedly and never return an error message. Defaults to 100.










### Docker Conf File
Example conf file:


	serviceId = "concept-container"
	requestUrl = "/?text=${text}"
	outputField = "concepts"
	method = "GET"
	responsePath = "result.concepts"



Most of the fields of the Docker conf file are equal to the ones in Rest configuration files. Refer to the section above for those. The only fields that change are **ip** and **port** which are substituted by **serviceId**.

* **serviceId**: The marathon Id for that service




# Input File
The input file must be formed of valid json maps, one on each line.

## Acknowledgement

This orchestrator was developed by [Paradigma Digital](https://en.paradigmadigital.com/) as part of the MixedEmotions project. This development has been partially funded by the European Union through the MixedEmotions Project (project number H2020 655632), as part of the `RIA ICT 15 Big data and Open Data Innovation and take-up` programme.

![MixedEmotions](https://raw.githubusercontent.com/MixedEmotions/MixedEmotions/master/img/me.png) 

![EU](https://raw.githubusercontent.com/MixedEmotions/MixedEmotions/master/img/H2020-Web.png)

 http://ec.europa.eu/research/participants/portal/desktop/en/opportunities/index.html
