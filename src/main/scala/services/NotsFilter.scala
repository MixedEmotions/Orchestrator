package services


import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._

import scala.util.parsing.json.JSON


object NotsFilter {

  def validText(input: String): Boolean = {
    val x = JSON.parseFull(input).asInstanceOf[Some[Map[String, Any]]].getOrElse(Map[String, Any]())

    val nots = x.get("nots").asInstanceOf[Some[List[String]]].getOrElse(List[String]()).map(x=>x.toLowerCase())
    val text = x.get("text").asInstanceOf[Some[String]].getOrElse("")
    val resultArray = nots.map(x => {
      !(text.toLowerCase.contains(x))
    })
    val result = resultArray.foldLeft(true)(_ & _)
    result
  }

  //TODO: Redo this as Futurething
  /*def filterText(input: List[String]): List[String] = input.map(x => JSON.parseFull(x)
    .asInstanceOf[Option[Map[String, Any]]].getOrElse(Map[String, Any]())).filter(x => {
    val nots = x.get("nots").asInstanceOf[Option[List[String]]].getOrElse(List[String]()).map(x=>x.toLowerCase())
    val text = x.get("text").asInstanceOf[Option[String]].getOrElse("")
    val resultArray = nots.map(x => {
      !(text.toLowerCase.contains(x))
    })
    val result = resultArray.foldLeft(true)(_ & _)
    result
  }).map(x => {

    implicit val formats = Serialization.formats(NoTypeHints)
    write(x)

  })*/


}
