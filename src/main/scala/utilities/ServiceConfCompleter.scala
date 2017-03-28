package utilities

import java.net.URLEncoder

import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization._


import org.slf4j.LoggerFactory

/**
 * Created by cnavarro on 4/07/16.
 */
object ServiceConfCompleter{
  val logger = LoggerFactory.getLogger(ServiceConfCompleter.getClass)
  implicit val formats = DefaultFormats

  def completeUrl(ip: String, port: Int, parameteredUrl: String, inputMap : Map[String, Any] ): String = {
    val substitutedString = completeString(parameteredUrl, inputMap, true)
    val completedUrl = s"http://${ip}:${port}/${substitutedString}"
    completedUrl
  }

  def completeBody(parameteredBody: String, inputMap : Map[String, Any] ): String = {
    completeString(parameteredBody, inputMap, false)
  }

  def completeFileUploadData(fileUploadConf: Map[String,String], inputMap : Map[String, Any] ): Option[Map[String, String]] = {
    val completedFileUploadData = for((key,value)<-fileUploadConf) yield{
      val completedBody = completeString(value, inputMap, false)
      (key, completedBody)
    }
    Some(completedFileUploadData)
  }

  //TODO: Probably the first part of the if(inputMap.contains(key) is redundant with the json traversor stuff
  def completeString(parameteredString: String, inputMap: Map[String, Any], urlEncode: Boolean):String = {
    val parts = parameteredString.split("[$|}]")
    val substitutedParts = parts.map(part=>{
      if(part.startsWith("{")){
        val key = part.replaceFirst("^\\{","")
        logger.trace(s"Searching key: ${key}")
        if(inputMap.contains(key)) {
          val value = {
            if(inputMap(key).isInstanceOf[List[Any]]){
              inputMap(key).asInstanceOf[List[Any]].mkString(",")
            }else{
              inputMap(key)
            }
          }
          logger.debug(s"Found ${value} for key ${key}")
          if (urlEncode) {
            URLEncoder.encode(value.toString, "UTF-8")
          } else {
            value
          }
        }else{
          logger.debug(s"Trying ${key} as path")
          val foundItem = JsonPathsTraversor.getJsonPath(key, write(inputMap), None)
          if(foundItem.isDefined){
            val value = foundItem.get.asInstanceOf[List[Any]].mkString(",")
            if (urlEncode) {
            URLEncoder.encode(value.toString, "UTF-8")
            } else {
              value
            }
          }else{
            val error = s"Unable to find key or path '${key}' when completing string"
            logger.error(error)
            throw new Exception(error)
          }

        }
      }else{
        part
      }
    })
    substitutedParts.mkString("")
  }

  /*def main(args: Array[String]) {
    val url = "/com.opensmile.maven/speechemotionservice/getdims?dims=arousal,valence,gender,age,big5o,big5c,big5e,big5a,big5n&url=${videoUrl.value}&timing=-1,-1"
    val params = Map(("ip","127.0.0.1"),("port",8080),("videoUrl", ("value"-> "asaber")))
    val ip = "127.0.0.1"
    val port = 8080
    val completedUrl = completeUrl(ip, port, url, params)
    println(completedUrl)
  }*/

}
