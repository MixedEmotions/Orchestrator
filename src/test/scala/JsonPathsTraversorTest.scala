import org.scalatest.Assertions._

import utilities.JsonPathsTraversor

import scala.io.Source


/**
 * Created by cnavarro on 13/07/16.
 */
class JsonPathsTraversorTest extends UnitSpec{
  val jsonPath = getClass.getResource("/input/jsonString1.json").toString.replaceFirst("file:","")
  val jsonString =  Source.fromFile(jsonPath).getLines.mkString

  val jsonPath2 = getClass.getResource("/input/jsonString2.json").toString.replaceFirst("file:","")
  val jsonString2 =  Source.fromFile(jsonPath2).getLines.mkString


  val jsonPath3 = getClass.getResource("/input/jsonString3.json").toString.replaceFirst("file:","")
  val jsonString3 =  Source.fromFile(jsonPath3).getLines.mkString


  val asrJsonPath = getClass.getResource("/input/videoResponseWithAsr.json").toString.replaceFirst("file:","")
  val asrJsonString =  Source.fromFile(asrJsonPath).getLines.mkString


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

  "A getJsonMap with a delete string" should "make the correct substitution in every field" in {
    assertResult(Map("arousal"->Some(List(5.75)), "emotion"->Some(List("sadness")))) {
      JsonPathsTraversor.getJsonMapPath(Map("arousal"->"entries.emotions.onyx:hasEmotion.http://www_DOT_gsi_DOT_dit_DOT_upm_DOT_es/ontologies/onyx/vocabularies/anew/ns#arousal",
        "emotion"->"entries.emotions.onyx:hasEmotion.onyx:hasEmotionCategory"),jsonString3, Some("http://gsi.dit.upm.es/ontologies/wnaffect/ns#"))
    }
  }

  "Parsing a json with multipath" should "return a List with the map elements" in {

    assertResult(List(Map("arousal" -> Some(List(2.575)), "valence" -> Some(List(3.597))), Map("arousal" -> Some(List(-0.023)), "valence" -> Some(List(0.027))), Map("arousal" -> Some(List(0.008)), "valence" -> Some(List(0.128))), Map("arousal" -> Some(List(-0.065)), "valence" -> Some(List(0.019))))){
      JsonPathsTraversor.getJsonFlatMap(Map("arousal"->"emotions.onyx:hasEmotion.emovoc:arousal", "valence"->"emotions.onyx:hasEmotion.emovoc:valence"),
        "entries", asrJsonString,None)


    }
  }

  "Parsing a json with multipath as empty string" should "return a List with the map elements using the plain list as a base" in {

    assertResult(List(Map("text" -> Some(List("Yeah")), "arousal" -> Some(List(2.575)), "valence" -> Some(List(3.597)), "sentiment" -> Some(List(0.339))), Map("text" -> Some(List("Guys back to another video you mentioned will so this is basically twenty four hours later with these little g for")), "arousal" -> Some(List(-0.023)), "valence" -> Some(List(0.027)), "sentiment" -> Some(List(0.966))), Map("text" -> Some(List("Know about this phone yesterday in the morning time and I both my on one to use didn't get a feel for it")), "arousal" -> Some(List(0.008)), "valence" -> Some(List(0.128)), "sentiment" -> Some(List(0.847))), Map("text" -> Some(List("Um but here I am after the hype with the for now this is what late as a phone service the market is really really New fresh in the same less than thirty days as far as been sold in the us m gonna say probably")), "arousal" -> Some(List(-0.065)), "valence" -> Some(List(0.019)), "sentiment" -> None)))
    {
      JsonPathsTraversor.getJsonFlatMap(Map("text"->"Text","arousal"->"entries.emotions.onyx:hasEmotion.emovoc:arousal", "valence"->"entries.emotions.onyx:hasEmotion.emovoc:valence",
        "sentiment"->"entries.emotions.onyx:hasEmotion.Sentiment"),
        "", asrJsonString,None)


    }
  }








  }
