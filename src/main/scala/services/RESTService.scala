package services

import java.nio.file.{Files, Paths}

import org.slf4j.LoggerFactory
import utilities.{RequestExecutor, ExecutableServiceConf}

import scala.util.parsing.json.JSON
import scalaj.http.{Http, HttpResponse, MultiPart,_}

/**
 * Created by cnavarro on 4/07/16.
 */
class RESTService(ip: String, port:Int, serviceConf: ExecutableServiceConf, requestExecutor: RequestExecutor)
  extends ExecutableService(serviceConf, requestExecutor){

  def getIpAndPort(): (String, Int) = {
    (ip, port)
  }

}

object RESTService {
   val logger = LoggerFactory.getLogger(RESTService.getClass)



  def main(args: Array[String]) {


    /*val inputs = Array(
      "{ \"text\": \"The new Star Wars film is awesome, but maybe it is just for fans. You will not enjoy it anyway\", \"nots\": [\"hola\"], \"lang\": \"en\"}"
    )

    val confPath = "/home/cnavarro/workspace/mixedemotions/MixedEmotions/orchestrator/src/main/resources/restServices/upm_emotion.conf"

    val restService = ServiceFactory.restServiceFromConfFile(confPath)


    for(input<-inputs){
      val inputMap = JSON.parseFull(input).asInstanceOf[Some[Map[String,Any]]].getOrElse(Map[String,Any]())
      val result = restService.executeService(inputMap)
      println(result)
    }*/


    val inputs = Array(
      //"{ \"video_path\": \"/home/cnavarro/workspace/mixedemotions/videoScripts/videos/Moto_G4_english_review.mp4\"}"
      "{\"upload_path\":\"/webserverfiles/downloadedFiles/Moto_G4_english_review.mp4\"}"
    )
    /*val data = Files.readAllBytes(Paths.get("/home/cnavarro/workspace/mixedemotions/videoScripts/videos/Moto_G4_english_review.mp4"))


    val multi = MultiPart("file", "Moto_G4_english_review.mp4", "video/quicktime", data )




    val query = "http://mixedemotions.fim.uni-passau.de:8080/er/aer/upload"
    //val query = "http://localhost:32768/com.opensmile.maven/speechemotionservice/upload"

    logger.debug("Start uploading")
    val response: HttpResponse[String] = Http(query).postMulti(multi).asString
    logger.debug("Finished uploading")


    println(s"Response: ${response.body}")
    */

    //val confPath = "/home/cnavarro/workspace/mixedemotions/MixedEmotions/orchestrator/src/main/resources/restServices/upload_to_audioextraction.conf"
    val confPath = "/home/cnavarro/workspace/mixedemotions/MixedEmotions/orchestrator/src/main/resources/restServices/extract_audioinfo.conf"

    val restService = ServiceFactory.restServiceFromConfFile(confPath)


    for(input<-inputs){
      val inputMap = JSON.parseFull(input).asInstanceOf[Some[Map[String,Any]]].getOrElse(Map[String,Any]())
      val result = restService.executeService(inputMap)
      println(result)
    }
    //val confPath = "/home/cnavarro/workspace/mixedemotions/MixedEmotions/orchestrator/src/main/resources/restServices/upm_emotion.conf"

    //val restService = ServiceFactory.restServiceFromConfFile(confPath)
    //val result = restService.parseResponse("File uploaded to : /webserverfiles/downloadedFiles/Moto_G4_english_review.mp4", None, None, Some("File uploaded to : (\\/.*)$"))
    //println(s"ParsedResult:${result.toString}")



  }

}
