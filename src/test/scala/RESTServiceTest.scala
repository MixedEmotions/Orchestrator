import services.RESTService
import utilities.JsonPathsTraversor

import scala.io.Source
import scala.util.parsing.json.JSON


/**
 * Created by cnavarro on 13/07/16.
 */
class RESTServiceTest extends UnitSpec{
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

  val jsonString3 =
    """

      {
        "@context": "http://reed.gsi.dit.upm.es:4000/api/contexts/Results.jsonld",
        "@id": "Results_1476190731.14",
        "analysis": [
          {
            "@id": "EmoTextANEW_0.1",
            "@type": "marl:SentimentAnalysis",
            "anew_path_en": "/Dictionary/ANEW2010All.txt",
            "anew_path_es": "/Dictionary/Redondo(2007).csv",
            "author": "@icorcuera",
            "centroids": {
              "anger": {
                "A": 6.95,
                "D": 5.1,
                "V": 2.7
              },
              "disgust": {
                "A": 5.3,
                "D": 8.05,
                "V": 2.7
              },
              "fear": {
                "A": 6.5,
                "D": 3.6,
                "V": 3.2
              },
              "joy": {
                "A": 7.22,
                "D": 6.28,
                "V": 8.6
              },
              "sadness": {
                "A": 5.21,
                "D": 2.82,
                "V": 2.21
              }
            },
            "description": "Emotion classifier using rule-based classification.",
            "emotions_ontology": {
              "anger": "http://gsi.dit.upm.es/ontologies/wnaffect/ns#anger",
              "disgust": "http://gsi.dit.upm.es/ontologies/wnaffect/ns#disgust",
              "fear": "http://gsi.dit.upm.es/ontologies/wnaffect/ns#negative-fear",
              "joy": "http://gsi.dit.upm.es/ontologies/wnaffect/ns#joy",
              "neutral": "http://gsi.dit.upm.es/ontologies/wnaffect/ns#neutral-emotion",
              "sadness": "http://gsi.dit.upm.es/ontologies/wnaffect/ns#sadness"
            },
            "extra_params": {
              "language": {
                "aliases": [
                  "language",
                  "l"
                ],
                "default": "en",
                "options": [
                  "es",
                  "en"
                ],
                "required": true
              }
            },
            "is_activated": true,
            "maxPolarityValue": 1.0,
            "minPolarityValue": 0.0,
            "module": "emotextANEW",
            "name": "EmoTextANEW",
            "requirements": {},
            "version": "0.1"
          }
        ],
        "entries": [
          {
            "@id": "Entry_1476190731.14",
            "emotions": [
              {
                "@id": "Emotions0",
                "onyx:hasEmotion": [
                  {
                    "@id": "Emotion0",
                    "http://www.gsi.dit.upm.es/ontologies/onyx/vocabularies/anew/ns#arousal": 5.75,
                    "http://www.gsi.dit.upm.es/ontologies/onyx/vocabularies/anew/ns#dominance": 3.04,
                    "http://www.gsi.dit.upm.es/ontologies/onyx/vocabularies/anew/ns#valence": 2.28,
                    "onyx:hasEmotionCategory": "http://gsi.dit.upm.es/ontologies/wnaffect/ns#sadness"
                 }
                ]
              }
            ],
            "language": "en",
            "nif:isString": "esto es horrible"
          }
        ]
      }
    """.stripMargin

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
    val restService = RESTService.restServiceFromConfFile(confPath)

  }

  "A RESTService" should "have the expected response" in {
    assertResult(Map("text" -> "The new Star Wars film is awesome.", "lang" -> "en", "emotions" -> Map("emotion" -> Some(List("joy")), "valence" -> Some(List(5.575))))) {
      val input = "{ \"text\": \"The new Star Wars film is awesome.\", \"lang\": \"en\"}"
      val confPath = getClass.getResource("/restServices/upm_emotion.conf").toString.replaceFirst("file:","")

      val restService = RESTService.restServiceFromConfFile(confPath)
      val inputMap = JSON.parseFull(input).asInstanceOf[Some[Map[String, Any]]].getOrElse(Map[String, Any]())
      restService.executeService(inputMap)
    }

  }





  }
