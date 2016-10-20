package services

import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import org.slf4j.LoggerFactory
import utilities.{JsonPathsTraversor, RequestExecutor, ServiceConfParser}

import scala.util.parsing.json.JSON

/**
 * Created by cnavarro on 14/10/16.
 */
abstract class ExecutableService(requestUrl: String, method: String, bodyKey: String, outputField:String, responsePath: String,
                                 responseMap: Map[String,String], deleteString: String, requestDelayMs: Int, requestTimeoutMs: Int) {

  val logger = LoggerFactory.getLogger(ExecutableService.this.getClass)
  implicit val formats = Serialization.formats(NoTypeHints)


  def getIpAndPort(): (String, Int)

  def executeService(input: Map[String,Any]): Map[String, Any] ={
    val (ip, port) = getIpAndPort()
    val url = ServiceConfParser.completeUrl(ip, port, requestUrl, input)
    logger.debug("Executing Service:"+url)
    val bodyContent = if(bodyKey.length>0) input(bodyKey).toString else ""
    val response = RequestExecutor.executeRequest(method, url, body=bodyContent, requestDelay = requestDelayMs, requestTimeout = requestTimeoutMs)
    val selectedResult = {
      if(responseMap.keySet.size>0){
        JsonPathsTraversor.getJsonMapPath(responseMap, response, deleteString)
      }else{
        JsonPathsTraversor.getJsonPath(responsePath, response, deleteString).getOrElse(List())
      }
    }
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

  def executeServiceJSONList(input: List[String]) : List[String] = {
    for (entry <- input) yield {
      executeService(entry)
    }
  }

  /*def executeService(input: Iterator[Map[String,Any]]) : Iterator[Map[String,Any]] = {
    for(entry<-input) yield {
      executeService(entry)
    }
  }
  */


}
