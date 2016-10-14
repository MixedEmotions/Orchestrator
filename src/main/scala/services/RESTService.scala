package services

import java.io.File

import com.typesafe.config.ConfigFactory
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import org.slf4j.LoggerFactory
import utilities.{JsonPathsTraversor, RequestExecutor, ServiceConfParser}


import scala.util.parsing.json.JSON

/**
 * Created by cnavarro on 4/07/16.
 */
class RESTService(requestUrl: String, method: String, bodyKey: String, ip: String, port:Int, outputField:String, responsePath: String,
                   requestDelayMs: Int, requestTimeoutMs: Int)
  extends Serializable{
  implicit val formats = Serialization.formats(NoTypeHints)



  def executeService(input: Map[String,Any]): Map[String, Any] ={
    val url = ServiceConfParser.completeUrl(ip, port, requestUrl, input)
    println(s"Going to execute service:${url}")
    val bodyContent = if(bodyKey.length>0) input(bodyKey).toString else ""
    val response = RequestExecutor.executeRequest(method, url, body=bodyContent, requestDelay = requestDelayMs, requestTimeout = requestTimeoutMs)
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

object RESTService {
  def restServiceFromConfFile(confPath: String): RESTService ={
    val confFile = new File(confPath)
    val parsedConf = ConfigFactory.parseFile(confFile)
    val conf = ConfigFactory.load(parsedConf)
    val body : String = if(conf.hasPath("body")) conf.getString("body") else ""
    val requestDelay = if(conf.hasPath("requestDelayMs")) conf.getInt("requestDelayMs") else 500
    //val requestTimeout = if(conf.hasPath("requestTimeoutSeconds")) conf.getInt("requestTimeoutSeconds")*1000 else 50000
    val requestTimeout = conf.getInt("requestTimeoutSeconds")*1000
    new RESTService(conf.getString("requestUrl"), conf.getString("method"), body, conf.getString("ip"),
      conf.getInt("port"), conf.getString("outputField"), conf.getString("responsePath"), requestDelay, requestTimeout)

  }


  def main(args: Array[String]) {


    val inputs = Array(
     //"{\"text\": \"I hate western movies with John Wayne\", \"nots\": [\"hola\"], \"lang\": \"en\"}",
     // "{ \"text\": \"Really nice car\", \"nots\": [\"hola\"], \"lang\": \"en\"}",
     // "{ \"text\": \"The new Star Wars film is really nasty. You will not enjoy it anyway\", \"nots\": [\"hola\"], \"lang\": \"en\"}",
     // "{ \"text\": \"Hola ke ace?\", \"nots\": [\"hola\"], \"lang\": \"es\"}",
     // "{ \"text\": \"La nueva de Star Wars está muy bien. Me encantó el robot pelota.\", \"nots\": [\"hola\"], \"lang\": \"es\"}",
     // "{ \"text\": \"El jefe se va a Endesa.\", \"nots\": [\"hola\"], \"lang\": \"es\"}",
      "{ \"text\": \"The new Star Wars film is awesome, but maybe it is just for fans. You will not enjoy it anyway\", \"nots\": [\"hola\"], \"lang\": \"en\"}"
    )


    val confPath = "/home/cnavarro/workspace/mixedemotions/me_extractors/DockerSparkPipeline/src/main/resources/restServices/upm_sentiment.conf"

    println(s"ConfPath:${confPath}")
    val restService = restServiceFromConfFile(confPath)


    for(input<-inputs){
      val inputMap = JSON.parseFull(input).asInstanceOf[Some[Map[String,Any]]].getOrElse(Map[String,Any]())
      val result = restService.executeService(inputMap)
      println(result)
    }

  }

}
