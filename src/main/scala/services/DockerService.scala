package services

import java.io.File
import scala.util.parsing.json.JSON

import com.typesafe.config.ConfigFactory
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import org.slf4j.LoggerFactory

import utilities.{JsonPathsTraversor, MarathonServiceDiscovery, RequestExecutor, ServiceConfParser}




class DockerService(serviceId: String, requestUrl: String, outputField:String, serviceDiscovery: MarathonServiceDiscovery,
                    method:String, bodyKey:String, responsePath: String, requestDelayMs: Int, requestTimeoutMs: Int)
extends Serializable{
  implicit val formats = Serialization.formats(NoTypeHints)



  def executeService(input: Map[String,Any]): Map[String, Any] ={
    val (ip, port) = serviceDiscovery.naiveServiceDiscover(serviceId)
    val url = ServiceConfParser.completeUrl(ip, port, requestUrl, input)
    println(s"Going to execute service:${url}")
    val bodyContent = if(bodyKey.length>0) input(bodyKey).toString else ""
    val response = RequestExecutor.executeRequest(method, url, body=bodyContent, requestDelay = requestDelayMs, requestTimeout = requestTimeoutMs)
    //??? The response might be a single string or an array, not always a map
    val selectedResult = JsonPathsTraversor.getJsonPath(responsePath, response).getOrElse(List())
    val result = input + ((outputField,selectedResult))
    result

  }

  def executeService(jsonString: String): String = {
    val temp = JSON.parseFull(jsonString).asInstanceOf[Option[Map[String,Any]]]
    temp match {
      case x: Some[Map[String, Any]] => {
        write(executeService(x.get.asInstanceOf[Map[String,Any]]))
      }
      case None => {
        jsonString
      }
    }

  }

  def executeService(input: Iterator[Map[String,Any]]) : Iterator[Map[String,Any]] = {
    for(entry<-input) yield {
      executeService(entry)
    }
  }

  def executeServiceJSONList(input: List[String]) : List[String] = {
    for(entry<-input) yield {
      executeService(entry)
    }
  }

}


object DockerService {

  def dockerServiceFromConfFile(confPath: String, serviceDiscovery: MarathonServiceDiscovery): DockerService ={
    val confFile = new File(confPath)
    val parsedConf = ConfigFactory.parseFile(confFile)
    val conf = ConfigFactory.load(parsedConf)
    val body : String = if(conf.hasPath("body")) conf.getString("body") else ""
    val requestDelay = if(conf.hasPath("requestDelayMs")) conf.getInt("requestDelayMs") else 500
    val requestTimeout = if(conf.hasPath("requestTimeoutSeconds")) conf.getInt("requestTimeoutSeconds")*1000 else 50000
    new DockerService(conf.getString("serviceId"), conf.getString("requestUrl"), conf.getString("outputField"), serviceDiscovery, conf.getString("method"), body,
    conf.getString("responsePath"),requestDelay,requestTimeout)

  }




  def main(args: Array[String]) {

    val logger = LoggerFactory.getLogger(DockerService.getClass)
    logger.info("Come ooooooooon")
    val inputs = Array("{\"text\": \"I hate western movies with John Wayne\", \"nots\": [\"hola\"], \"lang\": \"en\"}",
      "{ \"text\": \"Really nice car\", \"nots\": [\"hola\"], \"lang\": \"en\"}",
      "{ \"text\": \"The new Star Wars film is really nasty. You will not enjoy it anyway\", \"nots\": [\"hola\"], \"lang\": \"en\"}",
      "{ \"text\": \"The new Star Wars film is awesome, but maybe it is just for fans. You will not enjoy it anyway\", \"nots\": [\"hola\"], \"lang\": \"en\"}",
      "{ \"text\": \"Hola ke ace?\", \"nots\": [\"hola\"], \"lang\": \"es\"}",
      "{ \"text\": \"La nueva de Star Wars está muy bien. Me encantó el robot pelota.\", \"nots\": [\"hola\"], \"lang\": \"es\"}",
      "{ \"text\": \"El jefe se va a Endesa.\", \"nots\": [\"hola\"], \"lang\": \"es\"}"
    )
    //val inputs = Array("{\"text\": \"I hate western movies with John Wayne\", \"nots\": [\"hola\"], \"lang\": \"en\", \"videoUrl\":\"http://tv-download.dw.com/dwtv_video/flv/wikoe/wikioe20151114_wiruebli_sd_avc.mp4\"}",
    //  "{\"text\": \"I hate western movies with John Wayne\", \"nots\": [\"hola\"], \"lang\": \"en\", \"videoUrl\":\"http://tv-download.dw.com/dwtv_video/flv/wikoe/wikioe20151114_wiruebli_sd_avc.mp4\"}")

    val discovery = new MarathonServiceDiscovery("localhost",8123)
    val confPath = "/home/cnavarro/projectManager/conf/dockerServices/spanish_topic_service.conf"
    //val confPath = "/home/cnavarro/projectManager/conf/dockerServices/audioemotion_service.conf"
    //val confPath = "/home/cnavarro/workspace/mixedemotions/me_extractors/BRMDemoReview/src/main/resources/dockerServices/spanish_topic_service.conf"
    println(s"ConfPath:${confPath}")
    val dockerService = dockerServiceFromConfFile(confPath, discovery)


    for(input<-inputs){
      val inputMap = JSON.parseFull(input).asInstanceOf[Some[Map[String,Any]]].getOrElse(Map[String,Any]())
      val result = dockerService.executeService(inputMap)
      println(result)
      logger.info("Come ooooooooon")
    }

  }


}
