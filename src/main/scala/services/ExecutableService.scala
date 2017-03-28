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
abstract class ExecutableService(serviceConf: ExecutableServiceConf, requestExecutor: RequestExecutor) {

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
  val pivotPath = serviceConf.pivotPath
  val pivotName = serviceConf.pivotName
  val pivotId = serviceConf.pivotId
  val requirementField = serviceConf.requirementField
  val requirementRegex = serviceConf.requirementRegex
  val pollingCondition = serviceConf.pollingCondition
  val contentType = serviceConf.contentType

  def getIpAndPort(): (String, Int)

  def executeService(input: Map[String,Any]): Map[String, Any] ={
    if(requirementMet(input)){
      val selectedResult = performExecution(input)
      val result = input + ((outputField,selectedResult))
      result
    }else{
      logger.debug(s"Requirement not met: ${requirementRegex} in ${requirementField}")
      input
    }

  }

  //this is the real execute
  def executeServiceAndObtainList(input: Map[String, Any]): List[Map[String, Any]] = {
    if (requirementMet(input)) {
      val selectedResult = performExecution(input)
      if (pivotPath.isDefined) {
        multiplyByPivot(selectedResult.asInstanceOf[List[Map[String, Any]]], input, pivotName.get, pivotId.get)
      } else {
        val result = input + ((outputField, selectedResult))
        List(result)
      }
    } else {
      logger.debug(s"Requirement not met: ${requirementRegex} in ${requirementField}")
      List(input)
    }
  }

  //check if polling is defined the status
  def performExecution(input: Map[String, Any]): Any = {
    val (ip, port) = getIpAndPort()
    val url = ServiceConfCompleter.completeUrl(ip, port, requestUrl, input)
    logger.debug("Executing Service:"+url)
    val bodyContent = if(body.isDefined) Some(ServiceConfCompleter.completeBody(body.get, input)) else None
    val fileUploadData : Option[Map[String, String]] = if(fileUploadConf.isDefined) ServiceConfCompleter.completeFileUploadData(fileUploadConf.get, input) else None

    val response = requestExecutor.executeRequest(method, url, body=bodyContent, requestDelay = requestDelayMs, requestTimeout = requestTimeoutMs,
      fileUploadData=fileUploadData, contentType=contentType)
    //logger.debug(s"Response: ${response}")
    val selectedResult = parseResponse(response, responsePath, responseMap, responseParseString, pivotPath, pollingCondition)
    logger.debug(s"SelectedResult: ${selectedResult}")
    if(pollingCondition.isDefined){
      if(!selectedResult.asInstanceOf[Some[List[String]]].get.contains(pollingCondition.get)){
        logger.debug("response service polling: "+selectedResult)
        performExecution(input)
      } else{
        logger.debug("polling condition is DONE: "+selectedResult)
      }
    }

    selectedResult
  }

  def requirementMet(input: Map[String, Any]): Boolean = {
    if(requirementField.isEmpty || requirementRegex.isEmpty){
      true
    }else if(input.keySet.contains(requirementField.get)){
      val value = input.get(requirementField.get).get.toString
      val regex = requirementRegex.get.r
      if(regex.findFirstIn(value).isDefined){
        true
      }else{
        false
      }
    }else{
      false
    }

  }

  def multiplyByPivot(resultList: List[Map[String, Any]], input: Map[String, Any], pivotName:String, pivotId: String): List[Map[String, Any]] = {
    for(itemResult<-resultList) yield{
      val mixedResult = input + ((outputField, itemResult))
      //If for any reason I decide to parse the input to get this
      // val pivotIdValue = JsonPathsTraversor.getJsonPath(pivotId, write(input), None)
      val pivotIdValue = ServiceConfCompleter.completeBody(pivotId, input)
      val mixedResultWithPivotId = mixedResult + ((pivotName, pivotIdValue))
      mixedResultWithPivotId
    }
  }

  def parseResponse(response: String, responsePath: Option[String], responseMap: Option[Map[String,String]],
                    responseParseString: Option[String], pivotPath: Option[String], pollingCondition: Option[String]) : Any = {
    if(responseParseString.isDefined){
      val pattern = new Regex(responseParseString.get)
      val matchData = pattern.findFirstMatchIn(response)
      if(matchData.isDefined){
        matchData.get.group(1)
      }else{
        logger.debug(s"Pattern ${responseParseString.get} not found in ${response}")
      }
    }else if(pivotPath.isDefined) {
      JsonPathsTraversor.getJsonFlatMap(responseMap.get, pivotPath.get, response, deleteString)

    }else if(pollingCondition.isDefined){
      JsonPathsTraversor.getJsonPath(responsePath.get, response, deleteString)
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
        logger.error(s"Error parsing input json, conserving input: '${jsonString}'")
        jsonString
      }
    }
  }

  def executeServiceJSONList(input: List[String]) : List[String] = {
    for (entry <- input) yield {
      executeService(entry)
    }
  }

  def executeServiceAsFlatMap(input: List[String]) : List[String] = {
    input.flatMap(executeServiceAsList(_))

  }

  //this is the real service
  def executeServiceAsList(jsonString: String): List[String] = {
    val temp = JSON.parseFull(jsonString).asInstanceOf[Option[Map[String,Any]]]
    temp match {
      case x: Some[Map[String, Any]] => {
        val results = executeServiceAndObtainList(x.get)
        for(result<-results) yield {
          write(result)
        }
      }
      case None => {
        logger.error(s"Error parsing input json, conserving input: '${jsonString}'")
        List(jsonString)
      }
    }


  }



}
