package conf

import java.util.Map.Entry
import java.util.Properties
import java.util.function.Consumer


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

  lazy val topic = {
    properties.getProperty("topic")
  }
}
