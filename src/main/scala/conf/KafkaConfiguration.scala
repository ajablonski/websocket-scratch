package conf

import java.util.Map.Entry
import java.util.Properties
import java.util.function.Consumer

import com.softwaremill.react.kafka.{ConsumerProperties, ProducerProperties}
import kafka.serializer.{StringDecoder, StringEncoder}


object KafkaConfiguration {
  lazy val properties = {
    var loadedProperties = new Properties()

    loadedProperties.load(getClass.getClassLoader.getResourceAsStream("application.properties"))
    val properties = new Properties()

    loadedProperties.entrySet().forEach {
      new Consumer[Entry[AnyRef, AnyRef]] {
        override def accept(t: Entry[AnyRef, AnyRef]): Unit = {
          t.getKey match {
            case s: String if s.startsWith("kafka")
            => properties.put(s.replaceFirst("kafka.", ""), t.getValue)
            case default: Any =>
          }
        }
      }
    }
    properties
  }

  lazy val consumerProperties = (groupId: String) => ConsumerProperties[String](
    brokerList = properties.getProperty("bootstrap.servers"),
    zooKeeperHost = properties.getProperty("zookeeper.host"),
    topic = properties.getProperty("topic"),
    groupId = groupId,
    decoder = new StringDecoder()
  )

  lazy val producerProperties = ProducerProperties[String](
    brokerList = properties.getProperty("bootstrap.servers"),
    topic = properties.getProperty("topic"),
    encoder = new StringEncoder()
  )


  lazy val topic = {
    properties.getProperty("topic")
  }
}
