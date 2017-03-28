import com.typesafe.config.ConfigException
import org.scalamock.scalatest.MockFactory
import services.{ServiceFactory, DockerService}
import utilities.{RequestExecutor, DiscoveryService, MarathonDiscoveryService}

import scala.util.parsing.json.JSON


/**
 * Created by cnavarro on 13/07/16.
 */
class DockerServiceTest extends UnitSpec with MockFactory{


  "A DockerService" should "be created from a configuration file" in {

    val confPath = getClass.getResource("/dockerServices/spanish_topic_service.conf").toString.replaceFirst("file:","")
    val discovery = mock[DiscoveryService]
    (discovery.getIpAndPort _).expects("topic-container").returning(("localhost", 32770))
    val dRes= discovery.getIpAndPort("topic-container")
    val restService = ServiceFactory.dockerServiceFromConfFile(confPath, discovery)

  }

  "A DockerService " should "have the expected response, including named paths and substitutions" in {
    assertResult(Map("text" -> "jefe", "lang" -> "es", "topics"->List("DIRECTIVOS"))) {
      val input = "{ \"text\": \"jefe\", \"lang\": \"es\"}"
      val confPath = getClass.getResource("/dockerServices/spanish_topic_service.conf").toString.replaceFirst("file:","")
      val discovery = mock[DiscoveryService]
      (discovery.getIpAndPort _).expects("topic-container").returning(("localhost", 32770))
      val requestExecutor = mock[RequestExecutor]
      val queryExpected = "http://localhost:32770/?text=jefe"
      (requestExecutor.executeRequest _).expects("GET", queryExpected, 100000, 500, None, None, "application/json").returning("[\"DIRECTIVOS\"]")
      val dockerService = ServiceFactory.dockerServiceFromConfFile(confPath, discovery, requestExecutor)
      val inputMap = JSON.parseFull(input).asInstanceOf[Some[Map[String, Any]]].getOrElse(Map[String, Any]())
      dockerService.executeService(inputMap)
    }

  }

  }
