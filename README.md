# MixedEmotions' Orchestrator

## Description

This MixedEmotions Orchestrator was developed by Paradigma Digital.

The code of this orchestrator will let users have an starting point on how to interact with the MixedEmotion Toolbox modules. It is written in scala and can interact with RESTservices and DockerServices deployed in Mesos with a Mesos-DNS as a discovery service. The orchestrator will execute input documents in a pipeline with the defined modules.



# Installation
This project has been compiled using java 1.8, scala 2.10.4, sbt 0.13.5 and sbt-assembly 0.12.0.

# Compile
To compile the project use `sbt assembly`.

Compiled jars can be found in the [releases section](https://github.com/MixedEmotions/Orchestrator/releases) of this repository.

# Usage
Run the following command:



`java -jar  MixedEmotionsOrchestrator-assembly-x.x.jar {confFilePath} {inputPath}`

Where x.x is the release version of the jar, `{confFilePath}` is the pipeline configuration and `{inputPath}` is the path to the input file containing documents to be processed. The pipeline configuration main task is to define the sequence of modules to be called. The input file should be formed by jsons, each in a single line.


# Configuration

## Pipeline Configuration File

This configuration file main task is to define which modules are to be used and in which order. It also defines the output to be used (elasticsearch and/or file) and general configurations, such mesos-dns configuration and a global timeout for the process.

Example:


	modules = ["rest_some_external_service","docker_sentiment_extraction","docker_emotion_recognition"]
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
    docker_conf_folder="/some/absolute/path/dockerServices/"
	rest_conf_folder="/some/absolute/path/restServices/"
	outputFilePath="/some/absolute/path/output.txt"
	executionTimeoutSeconds=500




Fields description

 
* **modules**: modules to be executed, in order. There are 2 types of modules: rest and docker modules. The modules names will be the configuration file name, preceded by “rest” or “docker”. The configuration file for the module should be in the corresponding folder and should end in ‘.conf’. So, for the previous example, there should be a module configuration in `/some/absolute/path/restServices/some_external_service.conf`, in `/some/absolute/path/dockerServices/sentiment_extraction.conf`.
* **elasticsearch**: if present, elasticsearch where to send the results. Be careful as this works with a plugin that uses the API, so it is only compatible with certain versions of Elasticsearch. At the moment it uses sksamuel 2.4.0 which seems to be compatible also with 2.3.5
* **mesos-dns**: address of the Mesos-dns api
* **docker_conf_folder**: folder with the docker modules configurations.
* **rest_conf_folder**: folder with the rest modules configurations.
* **output_file_path**: file in which the output will be wrote.
* **ExecutionTimeoutSeconds**: maximum number of seconds the application has to run or else will fail.




As stated earlier, there are two types of services: docker and rest services. Each of them will have its configuration files in a different folder. The name of the configuration file should end in ‘.conf’ and be the name present in the ‘modules’ param, minus the `rest_` or `docker_` header.


For example, if I have this modules attribute: `[“rest_service1”, “docker_service2”]`, the folder stated in the “docker_conf_folder” attribute should contain a `“service2.conf”` file, and in the folder stated in `“rest_conf_folder”` there should be a `“service1.conf”` file.




## Service Conf File

There are two kinds of service configuration files, the rest configuration files and the docker configuration files. The fields in them are almost identical, excepting ip and port, that are unique for the rest services and the serviceId which is unique for the Docker services.

Example:

	ip = "localhost"
	port = 32769
	method = "GET"
	requestUrl = "?text=${text}"
	outputField = "emotions"
	response.json.path = "result.emotions"
	body = ""
	requestDelayMs=500
	requestTimeoutSeconds=300

In this case, the service is called by a GET request to the endpoint. Then, the result is searched in the response and put into the concepts. A delay of 500ms is put before calls to not overwhelm the service. If the whole processing for all files is not completed after 300 seconds, the orchestrator will stop launching a TimeoutException.

So, if the input file has this json as a line

    {"author": "someguy",
     "lang": "en",
     "text": "I love this",
    }
    
The orchestrator will wait 500ms and then make the request http://localhost:32769?text="I+love+this". That can responde like with these:

    {"header": {
	"time": "500 ms",
	"algorithm": "algo1"
      },
      "result": {
	"emotions": ["joy", "anger"]
      }
    }
      
Then the final result will be:

    {"author": "someguy",
     "lang": "en",
     "text": "I love this",
     "emotions": ["joy","anger"]
    }


Following there is an explanation of the fields that can be used in modules configuration files.

### Rest Service Conf File

* **ip**: address of the host
* **port**: port of the host. (If the service does not use a port, just an address, this should be set to 80)
* **method**: REST method, at the moment GET and POST are supported.
* **requestUrl**: Everything after the slash of the port, including url params. The values to be included in the param values are put in the form of ${mapAttribute}, where mapAttribute is the key in the map obtained  after parsing the input json. For example, in a json like: 
  `{“id”:132, “lang”:”es”, “text”:”some text to analyze”}` the query `?text=${text}` will translate to `?text=”some+text+to+analyze”`. 
* **response.json.path**: where to find the result in the json that the service responded with. The dots separate nested keys. For example, if the service responds with:
	`{“result”:{“time”:0.444, “concepts”:[“banking”, “loan”]}}`
 and `response.json.path` is `“result.concepts”`, the extracted result will be `[“banking”, “loan”]`.
 If the response is just an array, an empty string (“”) will get that array as result.
 Internally dots are translated to the “\” operation of scala’s json4s. For more information, look the “xpath and HOFs” of http://json4s.org/ . If the key has inner dots, you will have to escape them using `_DOT_`. For example: `entries.emotions.onyx:hasEmotion.http://www_DOT_gsi_DOT_dit_DOT_upm_DOT_es/ontologies/onyx/vocabularies/anew/ns#arousal` .
* **response.json.map**: when there are multiple fields in a json to extract. Uses the same syntax as `response.json.path`. They will be stored in a json object using as keys the `responseMap` keys. This object will be put inside the `outputField`. 
 For example:
 `Response: {“result”:{“time”:0.444, “concepts”:[“banking”, “loan”]}}` with 
	`response.json.map {
 		extracted_time = “result.time”
		detected_concepts = “result.concepts”
	}`
 Will result in an object: `{“extracted_time”:0.444, “detected_concepts”:[“banking”, “loan”]}`
* **response.json.deleteString**: deletes the corresponding string in each obtained result in a `response.json.map` or `response.json.path`.
* **response.string.parse**: for responses that are not in json format. Expects a regex with some grouping.
* **response.json.pivotPath**: for responses that have multiple items as response and expect the entry to multiply itself by those multiple items. For example, if a text is to be divided n phrases. It requires also a pivotId and a pivotName.
* **pivotName**: Name of the field that will identify the parts of a whole that has been divided. For example, it could be `“textId”` if the service splits a text into phrases.
* **pivotId**: value to assign to the `pivotName`. It could use the `${}` syntaxt of the request url if it is needed to input a value from the input map. For example, if your request has the form: `{“text”:”Lorem impsum….”, “text_name”:”111”}` you could use `pivotId=”${textName}”`.
* **outputField**: The key to be added to the input json with the result of the service request. If outputField is `‘concepts’`, taking into account the previous examples, the result json will be this:
	 `{“id”:132, “lang”:”es”, “text”:”some text to analyze”, “concepts”:[“banking”, “loan”]}`
* **body** (optional): For POST requests, the content of the body. It substitutes the chunks within "${_}" for its corresponding json path. For example if `body="""{"path": "${videoPath}" }”` and the input json is: `{“id”:132, “lang”:”es”,”videoPath”:”/home/videos/video”}`
 The content of the body will be `{"path":"/home/videos/video"}`. If body is set to empty string ("") it will send the whole input json.
* **contentType** (optional): Sets the contentType to be sent. In none is present, it defaults to 'application/json'
* **requestDelayMs**: Delay before http requests. Useful if the server might not be able to handle all the requests at once. Defaults to 500
* **requestTimeoutSeconds**: Time to wait before considering a request failed. Useful if there are services that can halt unexpectedly and never return an error message. Defaults to 100.
* **polling.condition**: If present, the orchestrator will retry this module until the field defined in `response.json.path` contains the value of this field.








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

# Logging

Default logging is set to debug. In case another level of loggin is to be set, a logging configuration file could be passed as an argument. For example:

    java -Dlogback.configurationFile=/path/to/config.xml -jar MixedEmotionsOrchestrator-assembly-1.0.jar {confPath} {inputPath}

The default logger configuration is at src/main/resources/logback.xml

Following there is an example configuration with which ERROR level is set for each class except for Orchestrator.

    <configuration>
    
        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
            <!-- encoders are assigned the type
                 ch.qos.logback.classic.encoder.PatternLayoutEncoder by default -->
            <encoder>
                <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
    
        <logger name="orchestrator.Orchestrator" level="DEBUG"/>
    
        <root level="ERROR">
            <appender-ref ref="STDOUT" />
        </root>
    </configuration>

# More info
More info and advanced examples of Orchestrator configuration can be found in this repository's [wiki](https://github.com/MixedEmotions/Orchestrator/wiki).

## Acknowledgement

This orchestrator was developed by [Paradigma Digital](https://en.paradigmadigital.com/) as part of the MixedEmotions project. This development has been partially funded by the European Union through the MixedEmotions Project (project number H2020 655632), as part of the `RIA ICT 15 Big data and Open Data Innovation and take-up` programme.

![MixedEmotions](https://raw.githubusercontent.com/MixedEmotions/MixedEmotions/master/img/me.png) 

![EU](https://raw.githubusercontent.com/MixedEmotions/MixedEmotions/master/img/H2020-Web.png)

 http://ec.europa.eu/research/participants/portal/desktop/en/opportunities/index.html
