import com.typesafe.config.ConfigException
import org.scalamock.scalatest.MockFactory
import services.{ServiceFactory, RESTService}
import utilities.{RequestExecutor, JsonPathsTraversor}

import scala.io.Source
import scala.util.parsing.json.JSON


/**
 * Created by cnavarro on 13/07/16.
 */
class RESTServiceTest extends UnitSpec with MockFactory{

  val asrJsonPath = getClass.getResource("/input/videoResponseWithAsr.json").toString.replaceFirst("file:","")
  val asrJsonString =  Source.fromFile(asrJsonPath).getLines.mkString

  val emotionJsonPath = getClass.getResource("/output/emotions.json").toString.replaceFirst("file:","")
  val emotionJsonString =  Source.fromFile(emotionJsonPath).getLines.mkString

  val sentimentJsonPath = getClass.getResource("/output/sentiment.json").toString.replaceFirst("file:","")
  val sentimentJsonString =  Source.fromFile(sentimentJsonPath).getLines.mkString

  "An empty Set" should "have size 0" in {
    assert(Set.empty.size == 0)
  }

  it should "produce NoSuchElementException when head is invoked" in {
    intercept[NoSuchElementException] {
      Set.empty.head
    }

  }

  "A RESTService" should "be created from a configuration file" in {
    val confPath = getClass.getResource("/restServices/upm_emotion.conf").toString.replaceFirst("file:","")
    val restService = ServiceFactory.restServiceFromConfFile("upm_emotion", confPath)

  }

  "A RESTService with a response.json.path" should "have the expected response, including named paths and substitutions" in {
    assertResult(List(Map("text" -> "The new Star Wars film is awesome.", "lang" -> "en", "emotions" -> Map("emotion" -> Some(List("joy")), "valence" -> Some(List(5.575)))))) {
      val input = "{ \"text\": \"The new Star Wars film is awesome.\", \"lang\": \"en\"}"
      val confPath = getClass.getResource("/restServices/upm_emotion.conf").toString.replaceFirst("file:","")

      val requestExecutor = mock[RequestExecutor]
      val queryExpected = "http://senpy.cluster.gsi.dit.upm.es:80/api/?i=The+new+Star+Wars+film+is+awesome.&lang=en&algo=EmoTextANEW"
      (requestExecutor.executeRequest _).expects("GET", queryExpected, 30000, 500, None, None, "application/json").returning(emotionJsonString)
      val restService = ServiceFactory.restServiceFromConfFile("mock service",confPath, requestExecutor)

      //val restService = ServiceFactory.restServiceFromConfFile("upm_emotion", confPath)
      val inputMap = JSON.parseFull(input).asInstanceOf[Some[Map[String, Any]]].getOrElse(Map[String, Any]())
      restService.executeService(inputMap)

    }

  }

  "Another RESTService with a response.json.path" should "have the expected response" in {
    assertResult(List(Map("text" -> "The new Star Wars film is awesome.", "lang" -> "en", "sentiment"-> List("1")))) {
      val input = "{ \"text\": \"The new Star Wars film is awesome.\", \"lang\": \"en\"}"
      val confPath = getClass.getResource("/restServices/upm_sentiment.conf").toString.replaceFirst("file:","")

      //val restService = ServiceFactory.restServiceFromConfFile("upm_sentiment", confPath)
      val requestExecutor = mock[RequestExecutor]
      val queryExpected = "http://senpy.cluster.gsi.dit.upm.es:80/api/?i=The+new+Star+Wars+film+is+awesome.&lang=en"
      (requestExecutor.executeRequest _).expects("GET", queryExpected, 30000, 500, None, None, "application/json").returning(sentimentJsonString)
      val restService = ServiceFactory.restServiceFromConfFile("mock service",confPath, requestExecutor)


      val inputMap = JSON.parseFull(input).asInstanceOf[Some[Map[String, Any]]].getOrElse(Map[String, Any]())
      restService.executeService(inputMap)
    }
  }

  "A RESTService without outputField" should "Throw a MisconfigurationException"  in {
    intercept[ConfigException] {getClass.getResource("/restServices/string_service.conf").toString.replaceFirst("file:", "")
      val input = "{ \"text\": \"The new Star Wars film is awesome.\", \"lang\": \"en\"}"
      val confPath = getClass.getResource("/restServices/faulty_service.conf").toString.replaceFirst("file:","")

      val restService = ServiceFactory.restServiceFromConfFile("faulty_service", confPath)
      val inputMap = JSON.parseFull(input).asInstanceOf[Some[Map[String, Any]]].getOrElse(Map[String, Any]())
      restService.executeService(inputMap)
    }
  }

  "A RESTService with a result.parse.string" should "parse the correct string"  in {
    assertResult(List(Map("text" -> "The new Star Wars film is awesome.", "lang" -> "en", "analysis_version" -> "EmoTextANEW_0.1"))) {
      val input = "{ \"text\": \"The new Star Wars film is awesome.\", \"lang\": \"en\"}"
      val confPath = getClass.getResource("/restServices/string_service.conf").toString.replaceFirst("file:", "")

      //val restService = ServiceFactory.restServiceFromConfFile("string_service", confPath)
      val requestExecutor = mock[RequestExecutor]
      val queryExpected = "http://senpy.cluster.gsi.dit.upm.es:80/api/?i=The+new+Star+Wars+film+is+awesome.&lang=en&algo=EmoTextANEW"
      (requestExecutor.executeRequest _).expects("GET", queryExpected, 30000, 500, None, None, "application/json").returning(emotionJsonString)
      val restService = ServiceFactory.restServiceFromConfFile("mock service",confPath, requestExecutor)

      val inputMap = JSON.parseFull(input).asInstanceOf[Some[Map[String, Any]]].getOrElse(Map[String, Any]())
      restService.executeService(inputMap)
    }

  }

  "A RESTService with pivot fields" should "return an entry for each of the pivot items with its pivot name and id" in {
    assertResult(List(Map("upload_path" -> "Moto_G4_english_review.mp4", "videoAnalysis" -> Map("valence" -> Some(List(3.597)), "text" -> Some(List("Yeah")), "arousal" -> Some(List(2.575))), "videoId" -> "Moto_G4_english_review.mp4"), Map("upload_path" -> "Moto_G4_english_review.mp4", "videoAnalysis" -> Map("valence" -> Some(List(0.027)), "text" -> Some(List("Guys back to another video you mentioned will so this is basically twenty four hours later with these little g for")), "arousal" -> Some(List(-0.023))), "videoId" -> "Moto_G4_english_review.mp4"), Map("upload_path" -> "Moto_G4_english_review.mp4", "videoAnalysis" -> Map("valence" -> Some(List(0.128)), "text" -> Some(List("Know about this phone yesterday in the morning time and I both my on one to use didn't get a feel for it")), "arousal" -> Some(List(0.008))), "videoId" -> "Moto_G4_english_review.mp4"), Map("upload_path" -> "Moto_G4_english_review.mp4", "videoAnalysis" -> Map("valence" -> Some(List(0.019)), "text" -> Some(List("Um but here I am after the hype with the for now this is what late as a phone service the market is really really New fresh in the same less than thirty days as far as been sold in the us m gonna say probably")), "arousal" -> Some(List(-0.065))), "videoId" -> "Moto_G4_english_review.mp4")))
    {
      val input = "{\"upload_path\":\"Moto_G4_english_review.mp4\"}"
      val confPath = getClass.getResource("/restServices/test_extract_audioinfo.conf").toString.replaceFirst("file:", "")
      val requestExecutor = mock[RequestExecutor]
      val queryExpected = "http://audioservice.com:8080/er/aer/getdims?dims=arousal,valence,sentiment&url=Moto_G4_english_review.mp4&timing=asr"
      (requestExecutor.executeRequest _).expects("GET", queryExpected, 3000000, 500, None, None, "application/json").returning(asrJsonString)
      val restService = ServiceFactory.restServiceFromConfFile("mock service",confPath, requestExecutor)
      val inputMap = JSON.parseFull(input).asInstanceOf[Some[Map[String, Any]]].getOrElse(Map[String, Any]())
      restService.executeService(inputMap)

    }

  }





  }
