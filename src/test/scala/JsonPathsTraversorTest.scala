import org.scalatest.Assertions._

import utilities.JsonPathsTraversor



/**
 * Created by cnavarro on 13/07/16.
 */
class JsonPathsTraversorTest extends UnitSpec{
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

  "Parsing json until last element" should "Be a list of 1 and 1" in {

    assertResult(Some(List("1","1"))){
      JsonPathsTraversor.getJsonPath("entries.sentiments.marl:polarityValue", jsonString)
    }
  }

  "Parsing json until previous to last element" should "Be a the map with the values" in {

    assertResult(Some(List(Map("@id" -> "Opinion0", "marl:hasPolarity" -> "marl:Positive", "marl:polarityValue" -> "1", "prov:wasGeneratedBy" -> "sentiText"), Map("@id" -> "Opinion0", "marl:hasPolarity" -> "marl:Positive", "marl:polarityValue" -> "1", "prov:wasGeneratedBy" -> "sentiText")))){
      JsonPathsTraversor.getJsonPath("entries.sentiments", jsonString)
    }
  }

  "Parsing json with nonexistent path" should "Be None" in {
    assertResult(None){
      JsonPathsTraversor.getJsonPath("notpath", jsonString)
    }
  }

  "Parsing json with invalid path" should "Be None" in {
    assertResult(None){
      JsonPathsTraversor.getJsonPath("notpath....////\\\\--", jsonString)
    }
  }

  "Parsing json with empty path" should "Be the complete map" in {
    assertResult(Some(List("http://reed.gsi.dit.upm.es:4000/api/contexts/Results.jsonld", "Results_1468239762.55", List(Map("stopwords_es" -> "/senpy-plugins/enterprise/sentiText/Dictionaries/SPA/stopwords.txt", "author" -> "@icorcuera", "module" -> "sentiText", "name" -> "sentiText", "minPolarityValue" -> 0.0, "dictionary_es" -> "/senpy-plugins/enterprise/sentiText/Dictionaries/ElhPolar_esV1.lex.txt", "description" -> "Sentiment classifier using rule-based classification based on English and Spanish", "is_activated" -> true, "extra_params" -> Map("language" -> Map("aliases" -> List("language", "l", "lang"), "default" -> "en", "options" -> List("es", "en"), "required" -> true)), "sentiwordnet_en" -> "/Dictionaries/SentiWordNet_3.0.0_20100705.txt", "local_path" -> "/senpy-plugins/enterprise/sentiText", "info" -> Map("stopwords_es" -> "/Dictionaries/SPA/stopwords.txt", "author" -> "@icorcuera", "module" -> "sentiText", "name" -> "sentiText", "dictionary_es" -> "/Dictionaries/ElhPolar_esV1.lex.txt", "description" -> "Sentiment classifier using rule-based classification based on English and Spanish", "extra_params" -> Map("language" -> Map("aliases" -> List("language", "l", "lang"), "default" -> "en", "options" -> List("es", "en"), "required" -> true)), "sentiwordnet_en" -> "/Dictionaries/SentiWordNet_3.0.0_20100705.txt", "version" -> "0.1", "requirements" -> Map(), "corpus_es" -> "/Corpus/general-tweets-train-tagged.json", "stopwords_en" -> "/Dictionaries/ENG/stopwords.txt", "corpus_en" -> "/Corpus/sentiment140-dataset.txt", "emoticons" -> "/Dictionaries/EmoticonSentimentLexicon.txt"), "version" -> "0.1", "requirements" -> Map(), "@type" -> "marl:SentimentAnalysis", "dictionary_en" -> "/senpy-plugins/enterprise/sentiText/Dictionaries/SentiWordNet_3.0.0_20100705.txt", "corpus_es" -> "/senpy-plugins/enterprise/sentiText/Corpus/general-tweets-train-tagged.json", "stopwords_en" -> "/senpy-plugins/enterprise/sentiText/Dictionaries/ENG/stopwords.txt", "maxPolarityValue" -> 1.0, "corpus_en" -> "/senpy-plugins/enterprise/sentiText/Corpus/sentiment140-dataset.txt", "emoticons" -> "/senpy-plugins/enterprise/sentiText/Dictionaries/EmoticonSentimentLexicon.txt", "@id" -> "sentiText")), List(Map("@id" -> "Entry0", "nif_isString" -> "The new Star Wars film is awesome, but maybe it is just for fans. You will not enjoy it anyway", "sentiments" -> List(Map("@id" -> "Opinion0", "marl:hasPolarity" -> "marl:Positive", "marl:polarityValue" -> "1", "prov:wasGeneratedBy" -> "sentiText"), Map("@id" -> "Opinion0", "marl:hasPolarity" -> "marl:Positive", "marl:polarityValue" -> "1", "prov:wasGeneratedBy" -> "sentiText"))))))){
      JsonPathsTraversor.getJsonPath("", jsonString)
    }
  }

  "Parsing json with @id path" should "return a list with the id" in {
    assertResult(Some(List("Results_1468239762.55"))){
      JsonPathsTraversor.getJsonPath("@id", jsonString)
    }
  }

  "Parsing string list with empty path" should "be the string list" in {
    assertResult(Some(List("an string"))){
      JsonPathsTraversor.getJsonPath("","[\"an string\"]")
    }
  }

  "Getting item in json" should "return a list of lists" in {
    assertResult(Some(List(List("1"),List("2","1")))){
      JsonPathsTraversor.getItemInJsonPath("entries.sentiments", "marl:polarityValue",jsonString2)
    }
  }

  "Getting item in json with emotions" should "return a list of lists" in {
    assertResult(Some(List(List("http://gsi.dit.upm.es/ontologies/wnaffect/ns#sadness")))){
      JsonPathsTraversor.getItemInJsonPath("entries.emotions.onyx:hasEmotion", "onyx:hasEmotionCategory",jsonString3)
    }
  }

  "Parsing json with a complex path that includes colon" should "return the expected item" in {
    assertResult(Some(List("http://gsi.dit.upm.es/ontologies/wnaffect/ns#sadness"))){
      JsonPathsTraversor.getJsonPath("entries.emotions.onyx:hasEmotion.onyx:hasEmotionCategory", jsonString3)
    }
  }

  "Parsing json with a path that includes _DOT_" should "return the expected item" in {
    assertResult(Some(List(5.75))) {
      JsonPathsTraversor.getJsonPath("entries.emotions.onyx:hasEmotion.http://www_DOT_gsi_DOT_dit_DOT_upm_DOT_es/ontologies/onyx/vocabularies/anew/ns#arousal", jsonString3)
    }
  }

  "Using getJsonMap" should "return a map with the defined keys and the expected values" in {
    assertResult(Map("arousal"->Some(List(5.75)), "emotion"->Some(List("http://gsi.dit.upm.es/ontologies/wnaffect/ns#sadness")))) {
      JsonPathsTraversor.getJsonMapPath(Map("arousal"->"entries.emotions.onyx:hasEmotion.http://www_DOT_gsi_DOT_dit_DOT_upm_DOT_es/ontologies/onyx/vocabularies/anew/ns#arousal",
      "emotion"->"entries.emotions.onyx:hasEmotion.onyx:hasEmotionCategory"),jsonString3)
    }
  }








  }
