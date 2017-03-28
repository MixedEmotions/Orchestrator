package utilities

import java.text.SimpleDateFormat
import java.util.Calendar

import com.sksamuel.elastic4s.ElasticDsl.{bulk, index, _}
import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri}
import com.typesafe.config.Config
import org.elasticsearch.common.settings.Settings
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import org.slf4j.LoggerFactory

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
import scala.util.parsing.json.JSON

/**
 * Created by cnavarro on 16/02/16.
 */
class ElasticsearchPersistor(val client: ElasticClient, val indexName: String) {
  val logger = LoggerFactory.getLogger(ElasticsearchPersistor.getClass)

  def this(ip: String, port: Int, clusterName: String, indexName: String){

    this(ElasticClient.remote(Settings.settingsBuilder().put("cluster.name", clusterName).build(),
      ElasticsearchClientUri(s"elasticsearch://${ip}:${port}")),     indexName)
      logger.debug(s"Elasticsearch ip: elasticsearch://${ip}:${port}")
  }


 def saveTweets(items: Seq[Map[String,Any]], documentType: String): Unit ={
    logger.debug(s"Saving ${items.size} items in bulk")
     val resp = client.execute {
       bulk(
         for(item<-items) yield {
           if(item.contains("id")) {
             index into indexName / documentType fields (item) id item("id")
           }else{
             index into indexName / documentType fields (item) //id tweet("id")
           }
         }
           )
       }

     }
}

object ElasticsearchPersistor {
  val logger = LoggerFactory.getLogger(ElasticsearchPersistor.getClass)
  import concurrent.ExecutionContext.Implicits.global

  def persistWithoutFormatting(input: List[String], ip: String, port: Int, clusterName: String,
                                indexName: String, documentType: String): Unit = {
    logger.debug(s"~~~~~~~~~~~~~~~~~going to persist in ${ip}:${port.toString} at ${clusterName}")


    val parsedInputs = input.map(x=> JSON.parseFull(x).asInstanceOf[Option[Map[String,Any]]].getOrElse(Map[String,Any]()))
    val filteredInputs = parsedInputs.filter(x=>x.keySet.size>0).map(x=>x+("indextime"->now))


    //val formattedTweets = parsedTweets.map(tweet => formatTweet(tweet))

    val persistor : ElasticsearchPersistor = new ElasticsearchPersistor(ip, port, clusterName, indexName)

    val chunks = filteredInputs.grouped(100)

    for(chunk<-chunks) {
      persistor.saveTweets(chunk, documentType)
    }

  }

  def saveListToElasticsearch(input : List[Future[List[String]]], configurationMap: Config, processingTimeOut: Duration): Unit =  {

    val esIP = configurationMap.getString("elasticsearch.ip")
    val esPort = configurationMap.getString("elasticsearch.port").toInt
    val esClusterName = configurationMap.getString("elasticsearch.clusterName")
    val indexName = configurationMap.getString("elasticsearch.indexName")
    val documentType = configurationMap.getString("elasticsearch.documentType")
    var badPracticeResults = new scala.collection.mutable.MutableList[String]()
    for(result<-input){
      result.onComplete{
        case Success(value)=>{
          for(item<-value){
            badPracticeResults.+=(item)
          }
        }
        case Failure(e) => {
          logger.error(s"Error: ${e.getMessage}")
        }
      }
    }
    logger.info(s"Going to persist ${badPracticeResults.length}")

    //New from 0.19
    Await.ready(Future.sequence(input), processingTimeOut )
    logger.debug(s"Finished. Either it really finished or it hit a I waited ${processingTimeOut}ms timeout")


    ElasticsearchPersistor.persistWithoutFormatting(badPracticeResults.toList, esIP, esPort , esClusterName, indexName, documentType)


  }

  def now(): String = {
    val time = Calendar.getInstance().getTime()
    val formatter = new SimpleDateFormat("yyyyMMddHHmmss")
    formatter.format(time)
  }



  /*def main (args: Array[String]) {
    val filepath = if(args.length>0) args(0) else "/home/cnavarro/workspace/mixedemotions/MixedEmotions/orchestrator/src/test/resources/input/one.txt"
    val ip = "localhost"
    val port = 9300
    val clusterName = "MixedEmotions"
    val indexName = "reviews"
    val documentType = "text_review"


    val persistor : ElasticsearchPersistor = new ElasticsearchPersistor(ip, port, clusterName, indexName)
    val resp = persistor.client.execute {
      index into indexName / documentType fields(

        "brand" -> "test",
        "text" -> "some new text",
        "index_time" -> System.currentTimeMillis()

        ) id "test11211"
    }.await
    val input = io.Source.fromFile(filepath).getLines.toList
    persistWithoutFormatting(input, ip, port, clusterName, indexName, documentType)
    println("Finished, I guess")

  }*/
}




