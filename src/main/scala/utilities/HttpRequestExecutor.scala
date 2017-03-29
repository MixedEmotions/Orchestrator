package utilities

import java.nio.file.{Files, Paths}

import org.slf4j.LoggerFactory

import scalaj.http.{HttpResponse, _}

import utilities.ServiceConfCompleter.completeBody


class HttpRequestExecutor extends RequestExecutor{

 def executeRequest(method: String, query: String, requestTimeout: Int = 50000, requestDelay: Int = 500, body: Option[String],
                     fileUploadData: Option[Map[String,String]]=None, contentType: String): String = {
    HttpRequestExecutor.executeRequest(method, query, requestTimeout, requestDelay, body, fileUploadData, contentType)
  }


}

object HttpRequestExecutor {
  val logger = LoggerFactory.getLogger(HttpRequestExecutor.getClass)


  def executeRequest(method: String, query: String, requestTimeout: Int = 50000, requestDelay: Int = 500, body: Option[String],
                       fileUploadData: Option[Map[String,String]], contentType: String): String ={
    if(method=="POST"){
      executePostRequest(query, body, requestTimeout, requestDelay, fileUploadData, contentType)
    }else{
      executeGetRequest(query, requestTimeout, requestDelay, contentType)
    }
  }



  // Each query is delivered to the service and the response is stored
  def executeGetRequest(query: String, requestTimeoutMs: Int, requestDelayMs: Int, contentType: String): String = {
    // The REST service is queried and the response (JSON format) is obtained
    logger.debug(s"Waiting ${requestDelayMs}ms")
    Thread.sleep(requestDelayMs)
    try {
      logger.debug(s"Executing query ${query}")
      logger.trace(s"Waiting response for ${requestTimeoutMs} ms")
      val response: HttpResponse[String] = Http(query).header("content-type",contentType).timeout(connTimeoutMs = 10000, readTimeoutMs = requestTimeoutMs).asString
      logger.trace(s"Got response")
      if (response.isError) {
        logger.error(s"HttpError: $query . ${response.body} ${response.code}")
        "{}"
      }
      val body = response.body
      body
    }catch{
      case e: Exception => {
        logger.error("Unexpected error executing get request")
        logger.error(s"Error: ${e.getMessage}\n")
        //println(e.getStackTrace.mkString("\n"))
        "{}"
      }
    }

  }

  def executePostRequest(query: String, postBody:Option[String], requestTimeoutMs: Int, requestDelayMs: Int, fileUploadData: Option[Map[String,String]],
                          contentType: String): String = {
    // The REST service is queried and the response (JSON format) is obtained
    logger.debug(s"Waiting for ${requestDelayMs}ms")
    Thread.sleep(requestDelayMs)
    try {
      logger.debug(s"Waiting response for ${requestTimeoutMs} ms")
      val response: HttpResponse[String] = sendPost(query, postBody, requestTimeoutMs, fileUploadData, contentType)
      logger.debug(s"Response: ${response}")
      if (response.isError) {
        logger.error(s"HttpError: $query . ${response.body} ${response.code}")
        //Map()
        "{}"
      }
      val body = response.body
      body
    }catch{
      case e: Exception => {
        logger.error(s"Unexpected error executing post request: ${e.getMessage} ")
        //Map()
        "{}"
      }
    }
  }

  def sendPost(query: String, postBody: Option[String], requestTimeoutMs: Int, fileUploadDataOption: Option[Map[String, String]],
               contentType: String ): HttpResponse[String] = {
    if(fileUploadDataOption.isDefined) {
      val fileUploadData = fileUploadDataOption.get
      logger.debug("Going to try file upload")
      logger.debug(s"fileUploadData: ${fileUploadData}")
      var data :Array[Byte]= null
      val filepath = fileUploadData("filePath")
      val path = Paths.get(filepath)
      val filename = path.getFileName.toString
      try {

        data = Files.readAllBytes(Paths.get(filepath))
      } catch {
        case e: java.nio.file.NoSuchFileException =>{
          logger.error(s"Error retrieving file: ${e.getMessage}")
          throw new Exception("java.nio.file.NoSuchFileException Error retrieving file: ${e.getMessage}")
        }
      }
      val multi = MultiPart(fileUploadData("name"), filename, fileUploadData("mime"), data )
      logger.debug("Going to upload file")
      logger.debug(s"Query: ${query}")
      Http(query).postMulti(multi).timeout(connTimeoutMs = 100000, readTimeoutMs = requestTimeoutMs).asString
    }else {
      Http(query).postData(postBody.getOrElse("")).header("content-type", contentType).timeout(connTimeoutMs = 10000, readTimeoutMs = requestTimeoutMs).asString
    }
  }

}
