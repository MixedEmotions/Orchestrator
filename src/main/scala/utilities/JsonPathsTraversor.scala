package utilities

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonAST.JNumber

import scala.util.parsing.json.JSON

/**
 * Created by cnavarro on 8/07/16.
 */
object JsonPathsTraversor {
  implicit val formats = DefaultFormats

  /*def getMapInJsonPath(path:String, validKeys: List[String], jsonString:String): Option[Any] = {
    val jacksonObject = parse(jsonString)
    val entries = jacksonPath(path, jacksonObject)
    println(s"Entries: ${entries}")
    val parsedEntries = JSON.parseFull(compact(entries)).getOrElse(Map()).asInstanceOf[Map[String,List[Map[String,Any]]]]
    println(parsedEntries)
    println(parsedEntries.keySet)
    println()


    /*for(map <- maps) {
      properMap = parseMapToObtainProperMap
      resultMap
      traverse keys {
        if key in validKeys
          resultMap[key] = properMap[key]
      }
      yield resultMap
    }*/
    None


  }*/


  def getItemInJsonPath(path:String, itemPath:String, jsonString: String): Option[Any] = {
    val jacksonObject = parse(jsonString)
    val parent = jacksonPath(path, jacksonObject)
    val items = parent.children.map(x=>getJsonPath(itemPath,compact(render(x))).getOrElse(x.values))
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

  def getJsonPath(path:String, jsonString: String): Option[Any] = {
    val jacksonObject = parse(jsonString)
    val jResult = jacksonPath(path,jacksonObject)
    if(jResult.isInstanceOf[JObject] && jResult.children.length>1){
      val values = jResult.children.map(x=>compact(render(x)))

      JSON.parseFull(s"[${values.mkString(",")}]")
    }else if(jResult.isInstanceOf[JString]) {
      JSON.parseFull(s"[${compact(jResult)}]")
    }else if(jResult.isInstanceOf[JNumber]) {
      JSON.parseFull(s"[${compact(jResult)}]")
    }else {
      JSON.parseFull(compact(jResult))

    }

  }

  def getJsonMapPath(mapPath: Map[String, String], jsonString: String): Map[String,Option[Any]] = {
    mapPath.mapValues(value=>getJsonPath(value, jsonString))
  }


  def main(args: Array[String]) {
    //val path = "root.children.grandchildren.item"


    val items1 = List(Map("item1"->"value1"),Map("item2"->"value2"))
    val items2 = List(Map("item21"->"value21"),Map("item22"->"value22"))
    val grandchildren = List(Map("items"->items1),Map("items"->items2))
    val grandchildren2 = List(Map("items"->List(Map("items"->List()))))
    val children = List(Map("grandchildren"->List(grandchildren,grandchildren2)))
    val root = Map("root"->Map("children"->children))

    val jsonString2 = """
                       {
                         "@context": "http://reed.gsi.dit.upm.es:4000/api/contexts/Results.jsonld",
                         "@id": "Results_1467965773.59",
                         "analysis": [
                           {
                             "@id": "sentiText",
                             "@type": "marl:SentimentAnalysis",
                             "author": "@icorcuera",
                             "corpus_en": "/senpy-plugins/enterprise/sentiText/Corpus/sentiment140-dataset.txt",
                             "corpus_es": "/senpy-plugins/enterprise/sentiText/Corpus/general-tweets-train-tagged.json",
                             "description": "Sentiment classifier using rule-based classification based on English and Spanish",
                             "dictionary_en": "/senpy-plugins/enterprise/sentiText/Dictionaries/SentiWordNet_3.0.0_20100705.txt",
                             "dictionary_es": "/senpy-plugins/enterprise/sentiText/Dictionaries/ElhPolar_esV1.lex.txt",
                             "emoticons": "/senpy-plugins/enterprise/sentiText/Dictionaries/EmoticonSentimentLexicon.txt",
                             "extra_params": {
                               "language": {
                                 "aliases": [
                                   "language",
                                   "l",
                                   "lang"
                                 ],
                                 "default": "en",
                                 "options": [
                                   "es",
                                   "en"
                                 ],
                                 "required": true
                               }
                             },
                             "info": {
                               "author": "@icorcuera",
                               "corpus_en": "/Corpus/sentiment140-dataset.txt",
                               "corpus_es": "/Corpus/general-tweets-train-tagged.json",
                               "description": "Sentiment classifier using rule-based classification based on English and Spanish",
                               "dictionary_es": "/Dictionaries/ElhPolar_esV1.lex.txt",
                               "emoticons": "/Dictionaries/EmoticonSentimentLexicon.txt",
                               "extra_params": {
                                 "language": {
                                   "aliases": [
                                     "language",
                                     "l",
                                     "lang"
                                   ],
                                   "default": "en",
                                   "options": [
                                     "es",
                                     "en"
                                   ],
                                   "required": true
                                 }
                               },
                               "module": "sentiText",
                               "name": "sentiText",
                               "requirements": {},
                               "sentiwordnet_en": "/Dictionaries/SentiWordNet_3.0.0_20100705.txt",
                               "stopwords_en": "/Dictionaries/ENG/stopwords.txt",
                               "stopwords_es": "/Dictionaries/SPA/stopwords.txt",
                               "version": "0.1"
                            },
                             "is_activated": true,
                             "local_path": "/senpy-plugins/enterprise/sentiText",
                             "maxPolarityValue": 1.0,
                            "minPolarityValue": 0.0,
                             "module": "sentiText",
                             "name": "sentiText",
                             "requirements": {},
                            "sentiwordnet_en": "/Dictionaries/SentiWordNet_3.0.0_20100705.txt",
                             "stopwords_en": "/senpy-plugins/enterprise/sentiText/Dictionaries/ENG/stopwords.txt",
                             "stopwords_es": "/senpy-plugins/enterprise/sentiText/Dictionaries/SPA/stopwords.txt",
                             "version": "0.1"
                           }
                         ],
                         "entries": [
                           {
                             "@id": "Entry0",
                             "nif_isString": "Really nice car sure",
                             "sentiments": [
                               {
                                 "@id": "Opinion0",
                                 "marl:hasPolarity": "marl:Positive",
                                 "marl:polarityValue": "1",
                                 "prov:wasGeneratedBy": "sentiText"
                               }
                             ]
                           },
                           {
                             "@id": "Entry0",
                             "nif_isString": "Really nice car sure",
                             "sentiments": [
                             {
                               "@id": "Opinion0",
                               "marl:hasPolarity": "marl:Positive",
                               "marl:polarityValue": "2",
                               "prov:wasGeneratedBy": "sentiText"
                             },
                             {
                               "@id": "Opinion0",
                               "marl:hasPolarity": "marl:Positive",
                               "marl:polarityValue": "1",
                               "prov:wasGeneratedBy": "sentiText"
                             }
                             ]
                           }
                         ]
                       }"""

    val jsonString = """{
        "@context": "http://reed.gsi.dit.upm.es:4000/api/contexts/Results.jsonld",
        "@id": "Results_1468239762.55",
        "analysis": [
          {
            "@id": "sentiText",
           "@type": "marl:SentimentAnalysis",
            "author": "@icorcuera",
            "corpus_en": "/senpy-plugins/enterprise/sentiText/Corpus/sentiment140-dataset.txt",
            "corpus_es": "/senpy-plugins/enterprise/sentiText/Corpus/general-tweets-train-tagged.json",
            "description": "Sentiment classifier using rule-based classification based on English and Spanish",
            "dictionary_en": "/senpy-plugins/enterprise/sentiText/Dictionaries/SentiWordNet_3.0.0_20100705.txt",
            "dictionary_es": "/senpy-plugins/enterprise/sentiText/Dictionaries/ElhPolar_esV1.lex.txt",
            "emoticons": "/senpy-plugins/enterprise/sentiText/Dictionaries/EmoticonSentimentLexicon.txt",
            "extra_params": {
              "language": {
                "aliases": [
                  "language",
                  "l",
                  "lang"
                ],
                "default": "en",
                "options": [
                  "es",
                  "en"
                ],
                "required": true
              }
            },
            "info": {
              "author": "@icorcuera",
              "corpus_en": "/Corpus/sentiment140-dataset.txt",
              "corpus_es": "/Corpus/general-tweets-train-tagged.json",
              "description": "Sentiment classifier using rule-based classification based on English and Spanish",
              "dictionary_es": "/Dictionaries/ElhPolar_esV1.lex.txt",
              "emoticons": "/Dictionaries/EmoticonSentimentLexicon.txt",
              "extra_params": {
                "language": {
                  "aliases": [
                    "language",
                    "l",
                    "lang"
                  ],
                  "default": "en",
                  "options": [
                    "es",
                    "en"
                  ],
                  "required": true
                }
              },
              "module": "sentiText",
              "name": "sentiText",
              "requirements": {},
              "sentiwordnet_en": "/Dictionaries/SentiWordNet_3.0.0_20100705.txt",
              "stopwords_en": "/Dictionaries/ENG/stopwords.txt",
              "stopwords_es": "/Dictionaries/SPA/stopwords.txt",
              "version": "0.1"
            },
            "is_activated": true,
            "local_path": "/senpy-plugins/enterprise/sentiText",
            "maxPolarityValue": 1.0,
            "minPolarityValue": 0.0,
            "module": "sentiText",
            "name": "sentiText",
            "requirements": {},
            "sentiwordnet_en": "/Dictionaries/SentiWordNet_3.0.0_20100705.txt",
            "stopwords_en": "/senpy-plugins/enterprise/sentiText/Dictionaries/ENG/stopwords.txt",
            "stopwords_es": "/senpy-plugins/enterprise/sentiText/Dictionaries/SPA/stopwords.txt",
            "version": "0.1"
          }
        ],
        "entries": [
          {
            "@id": "Entry0",
            "nif_isString": "The new Star Wars film is awesome, but maybe it is just for fans. You will not enjoy it anyway",
            "sentiments": [
              {
                "@id": "Opinion0",
                "marl:hasPolarity": "marl:Positive",
                "marl:polarityValue": "1",
                "prov:wasGeneratedBy": "sentiText"
              },
              {
                "@id": "Opinion0",
                "marl:hasPolarity": "marl:Positive",
                "marl:polarityValue": "1",
                "prov:wasGeneratedBy": "sentiText"
              }
           ]
         }
        ]
      }
      """

    /*val path = "entries.sentiments.marl:polarityValue"
    //val path = "entries.sentiments"

    val jacksonObject = parse(jsonString)
    println(jacksonObject)

    println("JResult------")
    val jResult = jacksonPath(path,jacksonObject)
    println(render(jResult))
    println(compact(render(jResult)).getClass)

    println("Something is amiss")
    implicit val formats = DefaultFormats
    println(jResult.children)
    println(compact(jResult))
    println(JSON.parseFull(compact(jResult)).get)



    println("Expected result")
    println(jacksonObject \\ "entries" \\ "sentiments" \\ "marl:polarityValue")

    println("Obtained result")
    println(getJsonPath(path, jsonString2))
    */
    println(getItemInJsonPath("entries.sentiments", "marl:polarityValue",jsonString2))


    //println(getMapInJsonPath("entries.sentiments", List("marl:hasPolarity","marl:polarityValue"),jsonString2))


  }

}
