package services

import java.io.File

import com.typesafe.config.{ConfigException, Config, ConfigFactory}
import org.slf4j.LoggerFactory
import utilities._

import scala.collection.JavaConversions._

/**
 * Created by cnavarro on 20/10/16.
 */
object ServiceFactory {
  val logger = LoggerFactory.getLogger(ServiceFactory.getClass)

  val RequestUrlPath = "requestUrl"
  val MethodPath = "method"
  val BodyPath = "body"
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



  def dockerService(dockerName:String, configurationMap: Config): DockerService = {
    val serviceName = dockerName.replace("docker_","")
    val confFolder = configurationMap.getString("docker_conf_folder")
    val confPath = confFolder + serviceName + ".conf"
    val discoveryService = new MarathonDiscoveryService(configurationMap.getString("mesos_dns.ip"), configurationMap.getInt("mesos_dns.port"))
    ServiceFactory.dockerServiceFromConfFile(confPath, discoveryService)
  }


  def restService(restServiceName:String, configurationMap: Config): RESTService = {
    val serviceName = restServiceName.replace("rest_", "")
    val confFolder = configurationMap.getString("rest_conf_folder")
    val confPath = confFolder + serviceName + ".conf"
    ServiceFactory.restServiceFromConfFile(confPath)
  }

  def createAndExecuteService(serviceName: String, configurationMap: Config): String => String = {
    logger.debug(s"Going to create and execute ${serviceName}")
    if(serviceName.startsWith("rest")){
      val service = restService(serviceName.replace("rest_", ""), configurationMap)
      service.executeService
    }else if(serviceName.startsWith("docker")){
      val service = dockerService(serviceName.replace("docker_",""), configurationMap)
      service.executeService
    }else {
      throw new Exception(s"Service name '${serviceName}' starts with an unknown type. Service names should start with 'rest_' or 'docker_'")
    }
  }

  def createAndExecuteListService(serviceName: String, configurationMap: Config): List[String] => List[String] = {
    logger.debug(s"Going to create and execute ${serviceName}")
    if(serviceName.startsWith("rest")){
      val service = restService(serviceName.replace("rest_", ""), configurationMap)
      service.executeServiceAsFlatMap
    }else if(serviceName.startsWith("docker")){
      val service = dockerService(serviceName.replace("docker_",""), configurationMap)
      service.executeServiceAsFlatMap
    }else {
      throw new Exception(s"Service name '${serviceName}' starts with an unknown type. Service names should start with 'rest_' or 'docker_'")
    }
  }



  def restServiceFromConfFile(confPath: String, requestExecutor: RequestExecutor = new HttpRequestExecutor): RESTService = {
    val confFile = new File(confPath)
    if(!confFile.exists() || confFile.isDirectory()) {
      throw new Exception(s"Could not find service configuration file: '${confPath}'")
    }
    val parsedConf = ConfigFactory.parseFile(confFile)
    val conf = ConfigFactory.load(parsedConf)
    val executableServiceConf = createServiceConf(conf)
    new RESTService(conf.getString(IpPath), conf.getInt(PortPath), executableServiceConf, requestExecutor)
  }

  def dockerServiceFromConfFile(confPath: String, discoveryService: DiscoveryService, requestExecutor: RequestExecutor = new HttpRequestExecutor): DockerService ={
    val confFile = new File(confPath)
    if(!confFile.exists() || confFile.isDirectory()) {
      throw new Exception(s"Could not find service configuration file: '${confPath}'")
    }
    val parsedConf = ConfigFactory.parseFile(confFile)
    val conf = ConfigFactory.load(parsedConf)
    val executableServiceConf = createServiceConf(conf)
    new DockerService(conf.getString(ServiceIdPath), discoveryService, executableServiceConf, requestExecutor)
  }

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
    new ExecutableServiceConf(requestUrl, method, body, outputField, responsePath, responseMap, deleteString,
      requestDelayMs, requestTimeoutMs, fileUploadConf, responseParseString, pivotPath,pivotName, pivotId, requirementField, requirementRegex)
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

}
