package services

import java.nio.file.{Files, Paths}

import org.slf4j.LoggerFactory
import utilities.{RequestExecutor, ExecutableServiceConf}

import scala.util.parsing.json.JSON
import scalaj.http.{Http, HttpResponse, MultiPart,_}

/**
 * Created by cnavarro on 4/07/16.
 */
class RESTService(serviceName: String, ip: String, port:Int, serviceConf: ExecutableServiceConf, requestExecutor: RequestExecutor)
  extends ExecutableService(serviceName:String, serviceConf, requestExecutor){

  def getIpAndPort(): (String, Int) = {
    (ip, port)
  }

}
