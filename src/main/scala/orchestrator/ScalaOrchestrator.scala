package orchestrator

import java.io.{FileWriter, BufferedWriter, File}
import java.text.Normalizer

import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory
import services.{RESTService, DockerService, NotsFilter}
import utilities.{ElasticsearchPersistor, MarathonServiceDiscovery}

import scala.collection.JavaConversions._
import scala.io.Source
import scala.util.parsing.json.JSON


object ScalaOrchestrator {
  //val confFilePath = "/home/cnavarro/workspace/mixedemotions/me_extractors/DockerSparkPipeline/src/main/resources/dockerProject.conf"
  //val confFilePath = "/home/cnavarro/projectManager/conf/docker.conf"
  val logger = LoggerFactory.getLogger(ScalaOrchestrator.getClass)


  def createConfigurationMap(confFilePath : String) : Config = {
    println(s"ConfFile: ${confFilePath}")
    val confFile = new File(confFilePath)
    val parsedConf = ConfigFactory.parseFile(confFile)
    ConfigFactory.load(parsedConf)
  }

  def findMixEmModule(mod: String)(implicit configurationMap : Config): List[String] => List[String] = {

    mod.trim match {
      case s if s.startsWith("docker") => dockerService(s, configurationMap)

      case s if s.startsWith("rest") => restService(s, configurationMap)

      case _ => throw new Exception("Module names should start by 'docker' or 'rest' in the 'modules' setting of the project configuration file")

    }

  }

  def dockerService(dockerName:String, configurationMap: Config): List[String] => List[String] = {
    val serviceName = dockerName.replace("docker_","")
    val confFolder = configurationMap.getString("docker_conf_folder")
    val confPath = confFolder + serviceName + ".conf"
    println(s"Docker conf path: ${confPath}")
    val discoveryService = new MarathonServiceDiscovery(configurationMap.getString("mesos_dns.ip"), configurationMap.getInt("mesos_dns.port"))
    val service = DockerService.dockerServiceFromConfFile(confPath, discoveryService)
    service.executeServiceJSONList

  }


  def restService(restServiceName:String, configurationMap: Config): List[String] => List[String] = {
    val serviceName = restServiceName.replace("rest_", "")
    val confFolder = configurationMap.getString("rest_conf_folder")
    val confPath = confFolder + serviceName + ".conf"
    println(s"Rest conf path: ${confPath}")

    val service = RESTService.restServiceFromConfFile(confPath)
    service.executeServiceJSONList
  }

  def saveToFile(input: List[String], outputPath:String): Unit ={
    println(s"Writing into ${outputPath} ")
    val file = new File(outputPath)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(input.mkString("\n"))
    bw.close()
  }


  def saveToElasticsearch(items : List[String], configurationMap: Config): Unit =  {

    val esIP = configurationMap.getString("elasticsearch.ip")
    val esPort = configurationMap.getString("elasticsearch.port").toInt
    val esClusterName = configurationMap.getString("elasticsearch.clusterName")
    val indexName = configurationMap.getString("elasticsearch.indexName")
    val documentType = configurationMap.getString("elasticsearch.documentType")


    println(s"Going to persist ${items.length}")



    ElasticsearchPersistor.persistWithoutFormatting(items.toList, esIP, esPort , esClusterName, indexName,documentType)


  }


  def usage(): Unit = {
    println(s"Missing params. There should be 2 params: {confFilePath} {inputFilePath} ")
    sys.exit(1)
  }


  def main (args: Array[String]) {

    if(args.length!=2){
      usage
    }

    val configurationFilePath = args(0)
    val inputPath = args(1)

    implicit val configurationMap = createConfigurationMap(configurationFilePath)


    // Pipeline configuration
    val mods : List[String] = configurationMap.getStringList("modules").toList.reverse
    //mods.foreach(mod=>println("\n\n--------mod: " + mod + " -----------\n\n"))
    mods.foreach(mod=>logger.info("--------Loading mod: " + mod + " -----------\n"))
    //val mods = Array("persistor")


    // Loading data

    //println("\nLoading data  -------\n")
    logger.info("Starting  -------\n")

    val addData = Array("{\"text\": \"I hate western movies with John Wayne\", \"nots\": [\"hola\"], \"lang\": \"en\"}",
      "{ \"text\": \"Really nice car\", \"nots\": [\"hola\"], \"lang\": \"en\"}",
      "{ \"text\": \"La nueva de Star Wars está muy bien. Me encantó el robot pelota.\", \"nots\": [\"hola\"], \"lang\": \"es\"}",
      "{ \"text\": \"El jefe se va a Endesa.\", \"nots\": [\"hola\"], \"lang\": \"es\"}")






    val initData = Source.fromFile(inputPath).getLines()
    //val data = initData.union(addData)
    val data : List[String] = initData.toList

    println("\nTotal number of raw data to process: " + data.length + "\n")

    // The NOT filter is initially applied tot he data
    //TODO: Not filter
    //val mydata = NotsFilter.filterText(data)
    //println("\nNumber of items after initial filtering: " + mydata.count() + "\n")



    // The name of the modules to be applied are stored in an array
    val funcArray = mods.map(findMixEmModule)

    // Getting the function that results from the composition of the selected modules/functions
    val dummyFunc: (List[String] => List[String]) = {x => x}
    val compFunc = funcArray.foldLeft(dummyFunc)(_.compose(_))
    println(s"Functions num: ${funcArray.length}")

    //funcArray.reduce(data)


    val resultJSON = compFunc(data)

    val numResultJSON = resultJSON.length



    println("\nNumber of items after processing (resultJSON): " + numResultJSON + "\n")

    saveToFile(resultJSON,configurationMap.getString("outputFilePath"))
    saveToElasticsearch(resultJSON, configurationMap)




  }

  println("-Finished-")




}
