name := "MixedEmotionsOrchestrator"

version := "0.19"

scalaVersion := "2.11.5"

libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.0.0"

libraryDependencies += "com.sksamuel.elastic4s" %% "elastic4s-streams" % "2.4.0"

libraryDependencies += "org.json4s" % "json4s-jackson_2.11" % "3.2.11"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "3.0.0" % "test"

libraryDependencies += "org.scalamock" %% "scalamock-scalatest-support" % "3.3.0" % "test"

libraryDependencies ++= Seq("com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
"org.slf4j" % "slf4j-api" % "1.7.1",
"org.slf4j" % "log4j-over-slf4j" % "1.7.1",  // for any java classes looking for this
"ch.qos.logback" % "logback-classic" % "1.0.3")

mainClass in Compile := Some("orchestrator.Orchestrator")

mainClass in assembly := Some("orchestrator.Orchestrator")

assemblyMergeStrategy in assembly := {
  //case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}
    