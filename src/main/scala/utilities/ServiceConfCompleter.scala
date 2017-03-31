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

  def completeString(parameteredString: String, inputMap: Map[String, Any], urlEncode: Boolean):String = {
    val substitutedString = parameteredString.replaceAll("\\$\\{","\\$@{")
    val parts = substitutedString.split("\\$")
    val substitutedParts = parts.map(part=>{
      if(part.startsWith("@{")){
        val partialParts = part.split("}")
        val key = partialParts(0).replaceFirst("^@\\{","")
        val rest = partialParts.tail.mkString("}")
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
            URLEncoder.encode(value.toString, "UTF-8") +rest
          } else {
            value + rest
          }
        }else{
          logger.debug(s"Trying ${key} as path")
          val foundItem = JsonPathsTraversor.getJsonPath(key, write(inputMap), None)
          if(foundItem.isDefined){
            val value = foundItem.get.asInstanceOf[List[Any]].mkString(",")
            if (urlEncode) {
            URLEncoder.encode(value.toString, "UTF-8")+rest
            } else {
              value +rest
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
    val result = substitutedParts.mkString("")
    if(parameteredString.startsWith("{") && parameteredString.endsWith("}")){
      result + "}"
    }else{
      result
    }
  }
}
