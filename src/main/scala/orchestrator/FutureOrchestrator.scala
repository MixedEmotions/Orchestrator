package orchestrator

import java.io.{BufferedWriter, File, FileWriter}

import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory
import services.{NotsFilter, DockerService, RESTService}
import utilities.{ElasticsearchPersistor, MarathonServiceDiscovery}


import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.concurrent._
import scala.concurrent.duration._
import concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.{Failure, Success}


object FutureOrchestrator {
  //val defaultConfFilePath = "/home/cnavarro/workspace/mixedemotions/me_extractors/DockerSparkPipeline/src/main/resources/dockerProject.conf"
  //val confFilePath = "/home/cnavarro/projectManager/conf/docker.conf"
  val logger = LoggerFactory.getLogger(FutureOrchestrator.getClass)


  def createConfigurationMap(confFilePath: String) : Config = {
    println(s"ConfFile: ${confFilePath}")
    val confFile = new File(confFilePath)
    val parsedConf = ConfigFactory.parseFile(confFile)
    ConfigFactory.load(parsedConf)
  }


  def findMixEmModule(modName: String)(implicit configurationMap: Config): String => String = {
    modName.trim match {
      case s if s.startsWith("docker") => dockerService(s, configurationMap)
      case s if s.startsWith("rest") => restService(s, configurationMap)
      case _ => throw new Exception("Module names should start by 'docker' or 'rest' in the 'modules' setting of the project configuration file")
    }
  }

  def dockerService(dockerName:String, configurationMap: Config): String => String = {
    val serviceName = dockerName.replace("docker_","")
    val confFolder = configurationMap.getString("docker_conf_folder")
    val confPath = confFolder + serviceName + ".conf"
    println(s"Docker conf path: ${confPath}")
    val discoveryService = new MarathonServiceDiscovery(configurationMap.getString("mesos_dns.ip"), configurationMap.getInt("mesos_dns.port"))
    val service = DockerService.dockerServiceFromConfFile(confPath, discoveryService)
    service.executeService
  }


  def restService(restServiceName:String, configurationMap: Config): String => String = {
    val serviceName = restServiceName.replace("rest_", "")
    val confFolder = configurationMap.getString("rest_conf_folder")
    val confPath = confFolder + serviceName + ".conf"
    println(s"Rest conf path: ${confPath}")

    val service = RESTService.restServiceFromConfFile(confPath)
    service.executeService

  }


  def saveToFile(input: List[Future[String]], outputPath:String, processingTimeOut: Duration): Unit ={
    println(s"Writing into ${outputPath} ")
    val file = new File(outputPath)
    val bw = new BufferedWriter(new FileWriter(file))
    for(line<-input){
      line.onComplete {
        case Success(value)=>{
          bw.write(s"${value}\n")
        }
        case Failure(e) => {
          bw.write(s"Error: ${e.getMessage}")
        }
      }

    }
    Await.ready(Future.sequence(input), processingTimeOut )
    bw.close()
  }

  def usage(): Unit = {
    println(s"Missing params. There should be 2 params: {confFilePath} {inputFilePath} ")
    sys.exit(1)
  }

  def saveToElasticsearch(input : List[Future[String]], configurationMap: Config): Unit =  {

    val esIP = configurationMap.getString("elasticsearch.ip")
    val esPort = configurationMap.getString("elasticsearch.port").toInt
    val esClusterName = configurationMap.getString("elasticsearch.clusterName")
    val indexName = configurationMap.getString("elasticsearch.indexName")
    val documentType = configurationMap.getString("elasticsearch.documentType")
    var badPracticeResults = new scala.collection.mutable.MutableList[String]()
    for(result<-input){
      result.onComplete{
        case Success(value)=>{
          badPracticeResults.+=(value)
        }
        case Failure(e) => {
          logger.error(s"Error: ${e.getMessage}")
        }
      }
    }


    println(s"Going to persist ${badPracticeResults.length}")



    ElasticsearchPersistor.persistWithoutFormatting(badPracticeResults.toList, esIP, esPort , esClusterName, indexName, documentType)


  }

  def extractTweet(input: Future[String]): Option[String] = {
    input.onComplete {
        case Success(value)=>{
          return Some(value)
        }
        case Failure(e) => {
          println(s"Error: ${e.getMessage}")
          return None
        }
    }
    return None
  }







  def main (args: Array[String]) {

    if(args.length!=2){
      usage
    }
    val confFilePath = args(0)
    val inputPath = args(1)
    implicit val configurationMap = createConfigurationMap(confFilePath)



    // Pipeline configuration
    val mods : List[String] = configurationMap.getStringList("modules").toList.reverse
    //mods.foreach(mod=>println("\n\n--------mod: " + mod + " -----------\n\n"))
    mods.foreach(mod=>logger.info("--------Loading mod: " + mod + " -----------\n"))
    //val mods = Array("persistor")


    // Loading data

    //println("\nLoading data  -------\n")
    logger.info("Starting  -------\n")



    val initData = Source.fromFile(inputPath).getLines()
    //val data = initData.union(addData)
    val data : List[String] = initData.toList

    println("\nTotal number of raw data to process: " + data.length + "\n")

    // The NOT filter is initially applied to the data
    //TODO This should really be in parallel also
    //val filteredData = NotsFilter.filterText(data)
    //println(s"\nNumber of items after initial filtering: ${filteredData.length}\n")



    // The name of the modules to be applied are stored in an array
    val funcArray = mods.map(findMixEmModule)

    // Getting the function that results from the composition of the selected modules/functions
    val dummyFunc: (String => String) = {x => x}
    val compFunc = funcArray.foldLeft(dummyFunc)(_.compose(_))
    println(s"Functions num: ${funcArray.length}")

    //funcArray.reduce(data)


    val futureResults = for(datum<-data) yield Future{compFunc(datum)}



    // Data are processed by the selected modules (composed function)
    println(s"\nNumber of items after processing (resultJSON): ${futureResults.length}\n")

    saveToFile(futureResults,configurationMap.getString("outputFilePath"), configurationMap.getInt("executionTimeoutSeconds") seconds)
    saveToElasticsearch(futureResults, configurationMap)




    println("-Finished-")

  }


}
