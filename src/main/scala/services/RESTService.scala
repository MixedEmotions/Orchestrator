package services

import scala.util.parsing.json.JSON

/**
 * Created by cnavarro on 4/07/16.
 */
class RESTService(requestUrl: String, method: String, bodyKey: String, ip: String, port:Int, outputField:String, responsePath: String,
                 responseMap: Map[String,String], deleteString: String, requestDelayMs: Int, requestTimeoutMs: Int)
  extends ExecutableService(requestUrl, method, bodyKey, outputField, responsePath, responseMap, deleteString, requestDelayMs, requestTimeoutMs){

  def getIpAndPort(): (String, Int) = {
    (ip, port)
  }

}

object RESTService {



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


    val confPath = "/home/cnavarro/workspace/mixedemotions/MixedEmotions/orchestrator/src/main/resources/restServices/upm_emotion.conf"

    val restService = ServiceFactory.restServiceFromConfFile(confPath)


    for(input<-inputs){
      val inputMap = JSON.parseFull(input).asInstanceOf[Some[Map[String,Any]]].getOrElse(Map[String,Any]())
      val result = restService.executeService(inputMap)
      println(result)
    }

  }

}
