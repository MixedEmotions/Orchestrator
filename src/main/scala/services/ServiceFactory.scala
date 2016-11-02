package services

import java.io.File

import com.typesafe.config.{ConfigException, Config, ConfigFactory}
import utilities.{ExecutableServiceConf, DiscoveryService, MarathonDiscoveryService}

import scala.collection.JavaConversions._

/**
 * Created by cnavarro on 20/10/16.
 */
object ServiceFactory {

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



  def dockerService(dockerName:String, configurationMap: Config): String => String = {
    val serviceName = dockerName.replace("docker_","")
    val confFolder = configurationMap.getString("docker_conf_folder")
    val confPath = confFolder + serviceName + ".conf"
    val discoveryService = new MarathonDiscoveryService(configurationMap.getString("mesos_dns.ip"), configurationMap.getInt("mesos_dns.port"))
    val service = ServiceFactory.dockerServiceFromConfFile(confPath, discoveryService)
    service.executeService

  }


  def restService(restServiceName:String, configurationMap: Config): String => String = {
    val serviceName = restServiceName.replace("rest_", "")
    val confFolder = configurationMap.getString("rest_conf_folder")
    val confPath = confFolder + serviceName + ".conf"
    val service = ServiceFactory.restServiceFromConfFile(confPath)
    service.executeService
  }

  def createAndExecuteService(serviceName: String, configurationMap: Config): String => String = {
    if(serviceName.startsWith("rest")){
      restService(serviceName.replace("rest_", ""), configurationMap)
    }else if(serviceName.startsWith("docker")){
      dockerService(serviceName.replace("docker_",""), configurationMap)
    }else {
      throw new Exception(s"Service name '${serviceName}' starts with an unknown type. Service names should start with 'rest_' or 'docker_'")
    }
  }



  def restServiceFromConfFile(confPath: String): RESTService = {
    val confFile = new File(confPath)
    val parsedConf = ConfigFactory.parseFile(confFile)
    val conf = ConfigFactory.load(parsedConf)
    val executableServiceConf = createServiceConf(conf)
    new RESTService(conf.getString(IpPath), conf.getInt(PortPath), executableServiceConf)
  }

  def dockerServiceFromConfFile(confPath: String, discoveryService: DiscoveryService): DockerService ={
    val confFile = new File(confPath)
    val parsedConf = ConfigFactory.parseFile(confFile)
    val conf = ConfigFactory.load(parsedConf)
    val executableServiceConf = createServiceConf(conf)
    new DockerService(conf.getString(ServiceIdPath), discoveryService, executableServiceConf)
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
    new ExecutableServiceConf(requestUrl, method, body, outputField, responsePath, responseMap, deleteString,
      requestDelayMs, requestTimeoutMs, fileUploadConf, responseParseString)
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
