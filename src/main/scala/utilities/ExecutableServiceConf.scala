package utilities

import com.typesafe.config.Config
import scala.collection.JavaConversions._

/**
 * Created by cnavarro on 2/11/16.
 */
case class ExecutableServiceConf(requestUrl: String, method: String, body: Option[String], outputField:String, responsePath: Option[String],
                            responseMap: Option[Map[String,String]], deleteString: Option[String], requestDelayMs: Int, requestTimeoutMs: Int,
                            fileUploadConf: Option[Map[String, String]], responseParseString: Option[String], pivotPath: Option[String],
                                  pivotName: Option[String], pivotId: Option[String], requirementField: Option[String], requirementRegex: Option[String],
                            pollingCondition: Option[String], contentType: String) {

}

object ExecutableServiceConf{
  //Set of params paths that can be defined in a Service configuration file
  val RequestUrlPath = "requestUrl"
  val MethodPath = "method"
  val BodyPath = "body"
  val ContentType = "contentType"
  val OutputFieldPath = "outputField"
  val ResponseJsonPath = "response.json"
  val ResponsePathPath = ResponseJsonPath +".path"
  val ResponseMapPath = ResponseJsonPath + ".map"
  val FileUploadConfPath = "uploadFile"
  val FileNamePath = FileUploadConfPath + ".name"
  val FileMimePath = FileUploadConfPath + ".mime"
  val FileFilePathPath = FileUploadConfPath + ".filePath"
  val DeleteStringPath = "response.json.deleteString"
  val RequestDelayMsPath = "requestDelayMs"
  val RequestTimeoutPath = "requestTimeoutSeconds"
  val ParseStringPath = "response.string.parse"
  val IpPath = "ip"
  val PortPath = "port"
  val ServiceIdPath = "serviceId"
  val PivotNamePath = "pivotName"
  val PivotIdPath = "pivotId"
  val PivotPathPath = "response.json.pivotPath"
  val RequirementFieldPath = "requirementField"
  val RequirementRegexPath = "requirementRegex"
  val PollingCondition = "polling.condition"


  def createServiceConf(conf: Config): ExecutableServiceConf = {
    checkConfiguration(conf)
    val requestUrl = conf.getString(RequestUrlPath)
    val method = conf.getString(MethodPath)
    val body : Option[String] = if(conf.hasPath(BodyPath)) Some(conf.getString(BodyPath)) else None
    val outputField : String = conf.getString(OutputFieldPath)
    val responsePath : Option[String] = if(conf.hasPath(ResponsePathPath)) Some(conf.getString(ResponsePathPath)) else None
    val responseMap: Option[Map[String,String]] = getResponseMap(conf)
    val deleteString: Option[String] = if(conf.hasPath(DeleteStringPath)) Some(conf.getString(DeleteStringPath)) else None
    val requestDelayMs = if(conf.hasPath(RequestDelayMsPath)) conf.getInt(RequestDelayMsPath) else 500
    val requestTimeoutMs = if(conf.hasPath(RequestTimeoutPath)) conf.getInt(RequestTimeoutPath)*1000 else 100000
    val fileUploadConf : Option[Map[String,String]] = getFileUploadConf(conf)
    val responseParseString: Option[String] = if(conf.hasPath(ParseStringPath)) Some(conf.getString(ParseStringPath)) else None
    val pivotName: Option[String] = if(conf.hasPath(PivotNamePath)) Some(conf.getString(PivotNamePath)) else None
    val pivotId: Option[String] = if(conf.hasPath(PivotIdPath)) Some(conf.getString(PivotIdPath)) else None
    val pivotPath: Option[String] = if(conf.hasPath(PivotPathPath)) Some(conf.getString(PivotPathPath)) else None
    val requirementField: Option[String] = if(conf.hasPath(RequirementFieldPath)) Some(conf.getString(RequirementFieldPath)) else None
    val requirementRegex: Option[String] = if(conf.hasPath(RequirementRegexPath)) Some(conf.getString(RequirementRegexPath)) else None
    val pollingCondition: Option[String] = if(conf.hasPath(PollingCondition)) Some(conf.getString(PollingCondition)) else None
    val contentType: String = if(conf.hasPath(ContentType)) conf.getString(ContentType) else "application/json"
    new ExecutableServiceConf(requestUrl, method, body, outputField, responsePath, responseMap, deleteString,
      requestDelayMs, requestTimeoutMs, fileUploadConf, responseParseString, pivotPath,pivotName, pivotId, requirementField,
      requirementRegex, pollingCondition, contentType)
  }

  def checkConfiguration(conf: Config): Boolean = {
    if(conf.hasPath(ResponseJsonPath)){
      if(conf.hasPath(ResponseMapPath) || conf.hasPath(ResponsePathPath)){
        true
      }else{
        throw new Exception(s"ConfigException:No configuration setting found for keys '${ResponsePathPath}' nor '${ResponseMapPath}' nor '${ParseStringPath}'")
      }
    }else if(conf.hasPath(ParseStringPath)){
      true
    }else {
      throw new Exception(s"ConfigException:No configuration setting found for keys '${ResponsePathPath}' nor '${ResponseMapPath}' nor '${ParseStringPath}'")
    }
  }

  def getResponseMap(conf: Config): Option[Map[String,String]] = {
    if(conf.hasPath(ResponseMapPath)){
      Some(conf.getValue(ResponseMapPath).unwrapped().asInstanceOf[java.util.HashMap[String,String]].toMap)
    }else{
      None
    }
  }

  def getFileUploadConf(conf: Config): Option[Map[String,String]] = {
    if(conf.hasPath(FileUploadConfPath)){
      Some(Map(("name",conf.getString(FileNamePath)),("mime",conf.getString(FileMimePath)), ("filePath",conf.getString(FileFilePathPath))))
    }else{
      None
    }
  }

}
