package utilities

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonAST.JNumber
import org.json4s.jackson.Serialization._
import org.slf4j.LoggerFactory

import scala.util.parsing.json.JSON

/**
 * Created by cnavarro on 8/07/16.
 */
object JsonPathsTraversor {
  implicit val formats = DefaultFormats
  val logger = LoggerFactory.getLogger(JsonPathsTraversor.this.getClass)




  def getItemInJsonPath(path:String, itemPath:String, jsonString: String, deleteString:Option[String]=None): Option[Any] = {
    val jacksonObject = parse(jsonString)
    val parent = jacksonPath(path, jacksonObject)
    val items = parent.children.map(x=>getJsonPath(itemPath,compact(render(x)),deleteString).getOrElse(x.values))
    Some(items)
  }

  def jacksonPath(path:String, jObject: JValue): JValue = {
    val pathParts = path.split("\\.")
    //Messing around with dots...
    val key = pathParts.head.replaceAll("_DOT_",".")
    if(key.equals("")) {
      jObject
    } else {
      val remainingPath = pathParts.tail.mkString(".")
      jacksonPath(remainingPath, jObject \ key)
    }
  }

  def getJsonPath(path:String, jsonString: String, deleteString: Option[String]=None): Option[Any] = {
    val jacksonObject = parse(jsonString)
    val jResult = jacksonPath(path,jacksonObject)
    if(jResult.isInstanceOf[JObject] && jResult.children.length>1){
      val values = jResult.children.map(x=>compact(render(x)))

      JSON.parseFull(s"[${values.mkString(",")}]")
    }else if(jResult.isInstanceOf[JString]) {
      val stringResult = compact(jResult).replace(deleteString.getOrElse(""),"")
      JSON.parseFull(s"[${stringResult}]")
    }else if(jResult.isInstanceOf[JNumber]) {
      JSON.parseFull(s"[${compact(jResult)}]")
    }else {
      JSON.parseFull(compact(jResult))

    }

  }

  def getJsonMapPath(mapPath: Map[String, String], jsonString: String, deleteString: Option[String] = None): Map[String,Option[Any]] = {
    mapPath.mapValues(value=>getJsonPath(value, jsonString, deleteString))
  }

  def getJsonFlatMap(mapPath: Map[String, String], arrayPath: String, jsonString: String, deleteString: Option[String]=None): List[Map[String,Option[Any]]] = {
    val possibleList = getJsonPath(arrayPath, jsonString, deleteString)
    if (possibleList.isDefined && possibleList.get.isInstanceOf[List[Any]]) {
      val baseArray: List[Any] = possibleList.get.asInstanceOf[List[Any]]
      //logger.debug(s"baseArray: ${baseArray}")
      val result: List[Map[String, Option[Any]]] = for (entryPoint <- baseArray) yield {
        //logger.debug(s"EntryPoint ${entryPoint}")
        if(entryPoint.isInstanceOf[List[Any]] && entryPoint.asInstanceOf[List[Any]].size==1){
          getJsonMapPath(mapPath, write(entryPoint.asInstanceOf[List[Map[String,Any]]].head), deleteString)
        }else{
          getJsonMapPath(mapPath, write(entryPoint.asInstanceOf[Map[String, Any]]), deleteString)
        }
      }
      result
    }else{
      logger.debug(s"Could not find array path '${arrayPath}'")
      List()
    }

  }


}
