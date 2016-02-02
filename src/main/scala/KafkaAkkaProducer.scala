import conf.KafkaConfiguration
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

class KafkaAkkaProducer {
  lazy val producer = new KafkaProducer[Integer, String](KafkaConfiguration.properties)

  def send(message : String) : Unit = {
    producer.send(new ProducerRecord[Integer, String](KafkaConfiguration.topic, message))
  }
}
