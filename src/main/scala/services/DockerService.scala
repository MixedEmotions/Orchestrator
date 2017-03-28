package services

import scala.util.parsing.json.JSON

import utilities.{RequestExecutor, ExecutableServiceConf, DiscoveryService, MarathonDiscoveryService}


class DockerService(serviceName: String, serviceId: String, serviceDiscovery: DiscoveryService, serviceConf: ExecutableServiceConf, requestExecutor: RequestExecutor)
extends ExecutableService(serviceId, serviceConf, requestExecutor ){

  def getIpAndPort(): (String, Int) = {
    serviceDiscovery.getIpAndPort(serviceId)
  }

}


object DockerService {

  /*def main(args: Array[String]) {

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

    val discovery = new MarathonDiscoveryService("localhost",32770)
    val confPath = "/home/cnavarro/projectManager/conf/dockerServices/spanish_topic_service.conf"
    //val confPath = "/home/cnavarro/projectManager/conf/dockerServices/audioemotion_service.conf"
    //val confPath = "/home/cnavarro/workspace/mixedemotions/me_extractors/BRMDemoReview/src/main/resources/dockerServices/spanish_topic_service.conf"
    println(s"ConfPath:${confPath}")
    val dockerService = ServiceFactory.dockerServiceFromConfFile(confPath, discovery)


    for(input<-inputs){
      val inputMap = JSON.parseFull(input).asInstanceOf[Some[Map[String,Any]]].getOrElse(Map[String,Any]())
      val result = dockerService.executeService(inputMap)
      println(result)
    }

  }*/


}
