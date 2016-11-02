package services

import scala.util.matching.Regex
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import org.slf4j.LoggerFactory
import utilities.{ExecutableServiceConf, JsonPathsTraversor, RequestExecutor, ServiceConfCompleter}

import scala.util.parsing.json.JSON

/**
 * Created by cnavarro on 14/10/16.
 */
abstract class ExecutableService(serviceConf: ExecutableServiceConf) {

  val logger = LoggerFactory.getLogger(ExecutableService.this.getClass)
  implicit val formats = Serialization.formats(NoTypeHints)
  val requestUrl = serviceConf.requestUrl
  val body = serviceConf.body
  val fileUploadConf = serviceConf.fileUploadConf
  val method = serviceConf.method
  val requestDelayMs = serviceConf.requestDelayMs
  val requestTimeoutMs = serviceConf.requestTimeoutMs
  val responsePath = serviceConf.responsePath
  val responseMap = serviceConf.responseMap
  val responseParseString = serviceConf.responseParseString
  val outputField = serviceConf.outputField
  val deleteString = serviceConf.deleteString

  def getIpAndPort(): (String, Int)

  def executeService(input: Map[String,Any]): Map[String, Any] ={
    val (ip, port) = getIpAndPort()
    val url = ServiceConfCompleter.completeUrl(ip, port, requestUrl, input)
    logger.debug("Executing Service:"+url)
    val bodyContent = if(body.isDefined) Some(ServiceConfCompleter.completeBody(body.get, input)) else None
    val fileUploadData : Option[Map[String, String]] = if(fileUploadConf.isDefined) ServiceConfCompleter.completeFileUploadData(fileUploadConf.get, input) else None
    val response = RequestExecutor.executeRequest(method, url, body=bodyContent, requestDelay = requestDelayMs, requestTimeout = requestTimeoutMs,
                                                  fileUploadData=fileUploadData)
    val selectedResult = parseResponse(response, responsePath, responseMap, responseParseString)
    val result = input + ((outputField,selectedResult))
    result
  }

  def parseResponse(response: String, responsePath: Option[String], responseMap: Option[Map[String,String]], responseParseString: Option[String]) : Any = {
    if(responseParseString.isDefined){
      val pattern = new Regex(responseParseString.get)
      val matchData = pattern.findFirstMatchIn(response)
      if(matchData.isDefined){
        matchData.get.group(1)
      }else{
        logger.debug(s"Pattern ${responseParseString.get} not found in ${response}")
      }
    }else if(responseMap.isDefined){
      JsonPathsTraversor.getJsonMapPath(responseMap.get, response, deleteString)
    }else if(responsePath.isDefined){
      JsonPathsTraversor.getJsonPath(responsePath.get, response, deleteString).getOrElse(List())
    }else{
      throw new Exception("Missing configuration for response")
    }
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



}
