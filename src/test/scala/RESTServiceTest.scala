import com.typesafe.config.ConfigException
import services.{ServiceFactory, RESTService}
import utilities.JsonPathsTraversor

import scala.io.Source
import scala.util.parsing.json.JSON


/**
 * Created by cnavarro on 13/07/16.
 */
class RESTServiceTest extends UnitSpec{

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
    val restService = ServiceFactory.restServiceFromConfFile(confPath)

  }

  "A RESTService with a responseMap" should "have the expected response, including named paths and substitutions" in {
    assertResult(Map("text" -> "The new Star Wars film is awesome.", "lang" -> "en", "emotions" -> Map("emotion" -> Some(List("joy")), "valence" -> Some(List(5.575))))) {
      val input = "{ \"text\": \"The new Star Wars film is awesome.\", \"lang\": \"en\"}"
      val confPath = getClass.getResource("/restServices/upm_emotion.conf").toString.replaceFirst("file:","")

      val restService = ServiceFactory.restServiceFromConfFile(confPath)
      val inputMap = JSON.parseFull(input).asInstanceOf[Some[Map[String, Any]]].getOrElse(Map[String, Any]())
      restService.executeService(inputMap)
    }

  }

  "A RESTService with a response.json.path" should "have the expected response" in {
    assertResult(Map("text" -> "The new Star Wars film is awesome.", "lang" -> "en", "sentiment"-> List("1"))) {
      val input = "{ \"text\": \"The new Star Wars film is awesome.\", \"lang\": \"en\"}"
      val confPath = getClass.getResource("/restServices/upm_sentiment.conf").toString.replaceFirst("file:","")

      val restService = ServiceFactory.restServiceFromConfFile(confPath)
      val inputMap = JSON.parseFull(input).asInstanceOf[Some[Map[String, Any]]].getOrElse(Map[String, Any]())
      restService.executeService(inputMap)
    }
  }

  "A RESTService without outputField" should "Throw a MisconfigurationException"  in {
    intercept[ConfigException] {
      val input = "{ \"text\": \"The new Star Wars film is awesome.\", \"lang\": \"en\"}"
      val confPath = getClass.getResource("/restServices/faulty_service.conf").toString.replaceFirst("file:","")

      val restService = ServiceFactory.restServiceFromConfFile(confPath)
      val inputMap = JSON.parseFull(input).asInstanceOf[Some[Map[String, Any]]].getOrElse(Map[String, Any]())
      restService.executeService(inputMap)
    }
  }

  "A RESTService with a result.parse.string" should "parse the correct string"  in {
      val input = "{ \"text\": \"The new Star Wars film is awesome.\", \"lang\": \"en\"}"
      val confPath = getClass.getResource("/restServices/string_service.conf").toString.replaceFirst("file:","")

      val restService = ServiceFactory.restServiceFromConfFile(confPath)
      val inputMap = JSON.parseFull(input).asInstanceOf[Some[Map[String, Any]]].getOrElse(Map[String, Any]())
      restService.executeService(inputMap)

  }





  }
