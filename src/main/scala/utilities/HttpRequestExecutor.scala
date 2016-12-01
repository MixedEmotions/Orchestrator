package utilities

import java.nio.file.{Files, Paths}

import org.slf4j.LoggerFactory

import scalaj.http.{HttpResponse, _}

import utilities.ServiceConfCompleter.completeBody


class HttpRequestExecutor extends RequestExecutor{

  // Each query is delivered to the service and the response is stored
  /*def executeRestRequest(query: Iterator[String]): Iterator[String] = {

    var queryResponse = List[String]()
    while (query.hasNext) {
      // The REST service is queried and the response (JSON format) is obtained
      val response: HttpResponse[String] = Http(query.next()).timeout(connTimeoutMs = 10000, readTimeoutMs = 50000)
        .asString
      // The response in JSON format is processed
      if (response.isNotError)
        queryResponse .::= (response.body)
    }
    queryResponse.iterator
  }*/
  def executeRequest(method: String, query: String, requestTimeout: Int = 50000, requestDelay: Int = 500, body: Option[String],
                     fileUploadData: Option[Map[String,String]]=None): String = {
    HttpRequestExecutor.executeRequest(method, query, requestTimeout, requestDelay, body, fileUploadData)
  }


}

object HttpRequestExecutor {
  val logger = LoggerFactory.getLogger(HttpRequestExecutor.getClass)


  def executeRequest(method: String, query: String, requestTimeout: Int = 50000, requestDelay: Int = 500, body: Option[String],
                      fileUploadData: Option[Map[String,String]]=None): String ={
    if(method=="POST"){
      executePostRequest(query, body, requestTimeout, requestDelay, fileUploadData)
    }else{
      executeGetRequest(query, requestTimeout, requestDelay)
    }
  }



  // Each query is delivered to the service and the response is stored
  def executeGetRequest(query: String, requestTimeoutMs: Int, requestDelayMs: Int): String = {
    // The REST service is queried and the response (JSON format) is obtained
    logger.debug(s"Waiting ${requestDelayMs}ms")
    Thread.sleep(requestDelayMs)
    try {
      logger.debug(s"Executing query ${query}")
      logger.debug(s"Waiting response for ${requestTimeoutMs} ms")
      val response: HttpResponse[String] = Http(query).timeout(connTimeoutMs = 10000, readTimeoutMs = requestTimeoutMs).asString
      logger.debug(s"Got response")
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

  def executePostRequest(query: String, postBody:Option[String], requestTimeoutMs: Int, requestDelayMs: Int, fileUploadData: Option[Map[String,String]]): String = {
    // The REST service is queried and the response (JSON format) is obtained
    logger.debug(s"Waiting for ${requestDelayMs}ms")
    Thread.sleep(requestDelayMs)
    try {
      logger.debug(s"Waiting response for ${requestTimeoutMs} ms")
      val response: HttpResponse[String] = sendPost(query, postBody, requestTimeoutMs, fileUploadData)
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

  def sendPost(query: String, postBody: Option[String], requestTimeoutMs: Int, fileUploadDataOption: Option[Map[String, String]]): HttpResponse[String] = {
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
      Http(query).postData(postBody.getOrElse("")).timeout(connTimeoutMs = 10000, readTimeoutMs = requestTimeoutMs).asString
    }
  }

}
