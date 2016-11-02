package utilities

import java.net.URLEncoder

/**
 * Created by cnavarro on 4/07/16.
 */
object ServiceConfCompleter{

  def completeUrl(ip: String, port: Int, parameteredUrl: String, inputMap : Map[String, Any] ): String = {
    val parts = parameteredUrl.split("[$|}]")
    val substitutedParts = parts.map(part=>{
      if(part.startsWith("{")){
        val key = part.replaceFirst("^\\{","")
        URLEncoder.encode(inputMap(key).toString,"UTF-8")
      }else{
        part
      }
    })
    val completedUrl = s"http://${ip}:${port}/${substitutedParts.mkString("")}"
    completedUrl
  }

  def completeBody(parameteredBody: String, inputMap : Map[String, Any] ): String = {
    val parts = parameteredBody.split("[$|}]")
    val substitutedParts = parts.map(part=>{
      if(part.startsWith("{")){
        val key = part.replaceFirst("^\\{","")
        inputMap(key).toString
      }else{
        part
      }
    })
    val completedBody = substitutedParts.mkString("")
    completedBody
  }

  def completeFileUploadData(fileUploadConf: Map[String,String], inputMap : Map[String, Any] ): Option[Map[String, String]] = {
    val completedFileUploadData = for((key,value)<-fileUploadConf) yield{
      val parts = value.split("[$|}]")
      val substitutedParts = parts.map(part => {
        if (part.startsWith("{")) {
          val key = part.replaceFirst("^\\{", "")
          inputMap(key).toString
        } else {
          part
        }
      })
      val completedBody = substitutedParts.mkString("")
      (key, completedBody)
    }
    Some(completedFileUploadData)
  }

  def main(args: Array[String]) {
    val url = "/com.opensmile.maven/speechemotionservice/getdims?dims=arousal,valence,gender,age,big5o,big5c,big5e,big5a,big5n&url=${videoUrl}&timing=-1,-1"
    val params = Map(("ip","127.0.0.1"),("port",8080),("videoUrl", "asaber"))
    val ip = "127.0.0.1"
    val port = 8080
    val completedUrl = completeUrl(ip, port, url, params)
    println(completedUrl)
  }

}
