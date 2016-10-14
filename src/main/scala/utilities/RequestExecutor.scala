package utilities

import scala.util.parsing.json.JSON
import scalaj.http.{HttpResponse, _}

class RequestExecutor {

  // Each query is delivered to the service and the response is stored
  def executeRestRequest(query: Iterator[String]): Iterator[String] = {

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
  }

}

object RequestExecutor {


  def executeRequest(method: String, query: String, requestTimeout: Int = 50000, requestDelay: Int = 500, body: String = ""): String ={
    if(method=="POST"){
      executePostRequest(query, body, requestTimeout, requestDelay)
    }else{
      executeGetRequest(query, requestTimeout, requestDelay)
    }
  }



  // Each query is delivered to the service and the response is stored
  def executeGetRequest(query: String, requestTimeoutMs: Int, requestDelayMs: Int): String = {
    // The REST service is queried and the response (JSON format) is obtained
    println(s"Waiting ${requestDelayMs}")
    Thread.sleep(requestDelayMs)
    try {
      println(s"Executing query ${query}")
      println(s"Waiting response for ${requestTimeoutMs} ms")
      val response: HttpResponse[String] = Http(query).timeout(connTimeoutMs = 10000, readTimeoutMs = requestTimeoutMs).asString
      if (response.isError) {
        println(s"HttpError: $query . ${response.body} ${response.code}")
        "{}"
      }
      val body = response.body
      body
    }catch{
      case e: Exception => {
        println("Unexpected error executing get request")
        println(s"Error: ${e.getMessage}\n")
        //println(e.getStackTrace.mkString("\n"))
        "{}"
      }
    }

  }

  def executePostRequest(query: String, postBody:String, requestTimeoutMs: Int, requestDelayMs: Int): String = {
    // The REST service is queried and the response (JSON format) is obtained
    println("Waiting")
    Thread.sleep(requestDelayMs)
    try {
      val response: HttpResponse[String] = Http(query).postData(postBody).timeout(connTimeoutMs = 10000, readTimeoutMs = requestTimeoutMs).asString
      if (response.isError) {
        println(s"HttpError: $query . ${response.body} ${response.code}")
        //Map()
        "{}"
      }
      val body = response.body
      body
    }catch{
      case e: Exception => {
        println("Unexpected error executing post request")
        //Map()
        "{}"
      }
    }
  }



}
