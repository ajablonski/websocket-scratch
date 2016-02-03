name := "websocket-scratch"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= {
  val akkaHttpVersion = "2.0.2"
  val kafkaVersion = "0.8.2.2"
  Seq(
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-core-experimental" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaHttpVersion,
    "org.apache.kafka" % "kafka_2.11" % kafkaVersion exclude(org = "org.slf4j", name = "slf4j-log4j12"),
    "com.softwaremill.reactivekafka" % "reactive-kafka-core_2.11" % "0.8.4" exclude(org = "org.slf4j", name = "slf4j-log4j12"),
    "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.6.3"
  )
}

fork in run := true