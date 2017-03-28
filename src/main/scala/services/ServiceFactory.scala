package services

import java.io.File

import com.typesafe.config.{ConfigException, Config, ConfigFactory}
import org.slf4j.LoggerFactory
import utilities._



/**
 * Created by cnavarro on 20/10/16.
 */
object ServiceFactory {
  val logger = LoggerFactory.getLogger(ServiceFactory.getClass)

  /**
   * Principal method that creates and executes every service.
   * Needs service name and configuration map to find the service configuration
   * @return a list of json encoded string that has not to be of the same length as the list to be applied to.
   */
  def createAndExecuteListService(serviceName: String, configurationMap: Config): List[String] => List[String] = {
    logger.debug(s"Going to create and execute ${serviceName}")
    if(serviceName.startsWith("rest")){
      val service = restService(serviceName.replace("rest_", ""), configurationMap)
      service.executeServiceAsFlatMap
    }else if(serviceName.startsWith("docker")){
      val service = dockerService(serviceName.replace("docker_",""), configurationMap)
      service.executeServiceAsFlatMap
    } else {
      throw new Exception(s"Service name '${serviceName}' starts with an unknown type. Service names should start with 'rest_' or 'docker_'")
    }
  }


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


  /**
   * Generates a RestService from a complete filepath
   * @param confPath service configuration filepath
   * @param requestExecutor is an optional param, only intended to be defined in tests, leave blank otherwise.
   * @return created RESTService
   */
  def restServiceFromConfFile(confPath: String, requestExecutor: RequestExecutor = new HttpRequestExecutor): RESTService = {
    val confFile = new File(confPath)
    if(!confFile.exists() || confFile.isDirectory()) {
      throw new Exception(s"Could not find service configuration file: '${confPath}'")
    }
    val parsedConf = ConfigFactory.parseFile(confFile)
    val conf = ConfigFactory.load(parsedConf)
    val executableServiceConf = ExecutableServiceConf.createServiceConf(conf)
    new RESTService(conf.getString(ExecutableServiceConf.IpPath), conf.getInt(ExecutableServiceConf.PortPath), executableServiceConf, requestExecutor)
  }

  /**
   * Generates a DockerService from a complete filepath
   * @param confPath service configuration filepath
   * @param discoveryService usually a marathon discovery service
   * @param requestExecutor is an optional param, only intended to be defined in tests, leave blank otherwise.
   * @return created RESTService
   */
  def dockerServiceFromConfFile(confPath: String, discoveryService: DiscoveryService, requestExecutor: RequestExecutor = new HttpRequestExecutor): DockerService ={
    val confFile = new File(confPath)
    if(!confFile.exists() || confFile.isDirectory()) {
      throw new Exception(s"Could not find service configuration file: '${confPath}'")
    }
    val parsedConf = ConfigFactory.parseFile(confFile)
    val conf = ConfigFactory.load(parsedConf)
    val executableServiceConf = ExecutableServiceConf.createServiceConf(conf)
    new DockerService(conf.getString(ExecutableServiceConf.ServiceIdPath), discoveryService, executableServiceConf, requestExecutor)
  }







}
