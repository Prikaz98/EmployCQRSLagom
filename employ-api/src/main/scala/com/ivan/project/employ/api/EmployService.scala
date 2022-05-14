package com.ivan.project.employ.api

import julienrf.json.derived
import akka.Done
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.libs.json.{Format, Json, Reads, Writes, __}

object EmployService {
  val TOPIC_NAME = "employerEvent"
}

/**
  * The employ service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the EmployService.
  */
trait EmployService extends Service {


  /*
  http://localhost:9000/api/employ/02/crtemplr
  {
    "fullName" : {"first":"Vasya", "last":"Vasilev", "middle":"Vlasenko"},
    "position" : "Developer",
    "salary" : 80000
  }
   */

  /**
    * создание агрегата "работника"
    *
    * @param id id агрегата
    * @return
    */
  def createEmployer(id: String): ServiceCall[BaseInfo, Done]

  /*
  http://localhost:9000/api/employ/01/chngpos
  {"position" : "Manager"}
   */
  /**
    * изменение должности работника
    *
    * @param id
    * @return
    */
  def changePosition(id: String): ServiceCall[newPosition, Done]

  /*
  /**
    * Example: curl http://localhost:9000/api/hello/Alice
    */
  def hello(id: String): ServiceCall[NotUsed, String]

  /**
    * Example: curl -H "Content-Type: application/json" -X POST -d '{"message":
    * "Hi"}' http://localhost:9000/api/hello/Alice
    */
  def useGreeting(id: String): ServiceCall[GreetingMessage, Done]
   */

  /**
    * This gets published to Kafka.
    *
    */
  def employTopic: Topic[TopicEvents]

  override final def descriptor: Descriptor = {
    import Service._
    // @formatter:off
    import TopicEventsJsonFormats.format
    named("employ")
      .withCalls(
        pathCall("/api/employ/:id/crtemplr", createEmployer _),
        pathCall("/api/employ/:id/chngpos", changePosition _)
      )
      .withTopics(
        // Kafka partitions messages, messages within the same partition will
        // be delivered in order, to ensure that all messages for the same user
        // go to the same partition (and hence are delivered in order with respect
        // to that user), we configure a partition key strategy that extracts the
        // name as the partition key.
        topic(EmployService.TOPIC_NAME, employTopic).addProperty(
          KafkaProperties.partitionKeyStrategy,
          PartitionKeyStrategy[TopicEvents](_.aggId)
        )
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}

sealed trait TopicEvents {
  def aggId: String
}
object TopicEventsJsonFormats {
  implicit val format: Format[TopicEvents] = {
    val baseRead: Reads[TopicEvents] = derived.flat.reads((__ \ "type").read[String])
    val read: Reads[TopicEvents]     = baseRead.orElse(UnsupportedEvent.rUnsupportedEvent)
    val write: Writes[TopicEvents]   = derived.flat.owrites((__ \ "type").write[String])
    Format(read, write)
  }
}
case class UnsupportedEvent(tpe: String, aggId: String = "") extends TopicEvents
object UnsupportedEvent {
  implicit val rUnsupportedEvent: Reads[TopicEvents] = (__ \ "type").read[String].map(UnsupportedEvent(_))
}

case class EmployCreated(aggId: String, baseInfo: BaseInfo) extends TopicEvents
object EmployCreated {
  implicit val format: Format[EmployCreated] = Json.format[EmployCreated]
}
case class EmployerChangePosition(aggId : String , positionApi: PositionApi.Position) extends TopicEvents
object EmployerChangePosition{
  implicit val format : Format[EmployerChangePosition] = Json.format[EmployerChangePosition]
}
