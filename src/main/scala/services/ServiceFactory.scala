package services

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}
import utilities.{DiscoveryService, MarathonDiscoveryService}

import scala.collection.JavaConversions._

/**
 * Created by cnavarro on 20/10/16.
 */
object ServiceFactory {

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
    val body : String = if(conf.hasPath("body")) conf.getString("body") else ""
    val responseMap: Map[String,String] = getResponseMap(conf)
    val deleteString: String = if(conf.hasPath("resultDeleteString")) conf.getString("resultDeleteString") else ""
    val requestDelay = if(conf.hasPath("requestDelayMs")) conf.getInt("requestDelayMs") else 500
    val requestTimeout = conf.getInt("requestTimeoutSeconds")*1000
    new RESTService(conf.getString("requestUrl"), conf.getString("method"), body, conf.getString("ip"),
      conf.getInt("port"), conf.getString("outputField"), conf.getString("responsePath"), responseMap, deleteString,
      requestDelay, requestTimeout)

  }

  def dockerServiceFromConfFile(confPath: String, discoveryService: DiscoveryService): DockerService ={
    val confFile = new File(confPath)
    val parsedConf = ConfigFactory.parseFile(confFile)
    val conf = ConfigFactory.load(parsedConf)
    val body : String = if(conf.hasPath("body")) conf.getString("body") else ""
    val responseMap: Map[String,String] = getResponseMap(conf)
    val deleteString: String = if(conf.hasPath("result_delete_string")) conf.getString("result_delete_string") else ""
    val requestDelay = if(conf.hasPath("requestDelayMs")) conf.getInt("requestDelayMs") else 500
    val requestTimeout = if(conf.hasPath("requestTimeoutSeconds")) conf.getInt("requestTimeoutSeconds")*1000 else 50000
    new DockerService(conf.getString("serviceId"), conf.getString("requestUrl"), conf.getString("outputField"),
      discoveryService, conf.getString("method"), body, conf.getString("responsePath"), responseMap, deleteString, requestDelay,
      requestTimeout)

  }


  def getResponseMap(conf: Config): Map[String,String] = {
    if(conf.hasPath("responseMap")){
      conf.getValue("responseMap").unwrapped().asInstanceOf[java.util.HashMap[String,String]].toMap
    }else{
      Map()
    }
  }

}
