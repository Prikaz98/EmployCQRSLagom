package com.ivan.project.employ
import com.ivan.project.employ.impl.PositionImpl.PositionImpl
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json._


package object impl {
  /**
    * This interface defines all the events that the EmployAggregate supports.
    */
  sealed trait EmployEvent extends AggregateEvent[EmployEvent] {
    def aggregateTag: AggregateEventTag[EmployEvent] = EmployEvent.Tag
  }

  object EmployEvent {
    val Tag: AggregateEventTag[EmployEvent] = AggregateEventTag[EmployEvent]
  }

  case class EmployCreated(baseInfo: EmployerBaseInfo)
    extends EmployEvent

  object EmployCreated {
    implicit val format : Format[EmployCreated] = Json.format
  }
  case class PositionChanged(position: PositionImpl)extends EmployEvent
  object PositionChanged{
    implicit val format : Format[PositionChanged] = Json.format
  }

  /*
  /**
    * An event that represents a change in greeting message.
    */
  case class GreetingMessageChanged(message: String) extends EmployEvent

  object GreetingMessageChanged {

    /**
      * Format for the greeting message changed event.
      *
      * Events get stored and loaded from the database, hence a JSON format
      * needs to be declared so that they can be serialized and deserialized.
      */
    implicit val format: Format[GreetingMessageChanged] = Json.format
  }

  /**
    * This is a marker trait for commands.
    * We will serialize them using Akka's Jackson support that is able to deal with the replyTo field.
    * (see application.conf)
    */

   */

  case class EmployerBaseInfo(fullName: FullName, position: PositionImpl, salary: Double)

  object EmployerBaseInfo {
    implicit val format: Format[EmployerBaseInfo] = Json.format
  }

  case class FullName(first: String, last: String, middle: String)

  object FullName {
    implicit val format: Format[FullName] = Json.format
  }

  object PositionImpl extends Enumeration {
    type PositionImpl = Value
    val Developer, Manager, ScramMaster = Value
    implicit val format: Reads[PositionImpl.Value] = Reads.enumNameReads(PositionImpl)
  }


  final case class Greeting(message: String)

  object Greeting {
    implicit val format: Format[Greeting] = Json.format
  }

  sealed trait Confirmation

  case object Confirmation {
    implicit val format: Format[Confirmation] = new Format[Confirmation] {
      override def reads(json: JsValue): JsResult[Confirmation] = {
        if ((json \ "reason").isDefined)
          Json.fromJson[Rejected](json)
        else
          Json.fromJson[Accepted](json)
      }

      override def writes(o: Confirmation): JsValue = {
        o match {
          case acc: Accepted => Json.toJson(acc)
          case rej: Rejected => Json.toJson(rej)
        }
      }
    }
  }

  sealed trait Accepted extends Confirmation

  case object Accepted extends Accepted {
    implicit val format: Format[Accepted] =
      Format(Reads(_ => JsSuccess(Accepted)), Writes(_ => Json.obj()))
  }

  case class Rejected(reason: String) extends Confirmation

  object Rejected {
    implicit val format: Format[Rejected] = Json.format
  }

  /**
    * Akka serialization, used by both persistence and remoting, needs to have
    * serializers registered for every type serialized or deserialized. While it's
    * possible to use any serializer you want for Akka messages, out of the box
    * Lagom provides support for JSON, via this registry abstraction.
    *
    * The serializers are registered here, and then provided to Lagom in the
    * application loader.
    */
  object EmploySerializerRegistry extends JsonSerializerRegistry {
    override def serializers: Seq[JsonSerializer[_]] = Seq(
      // state and events can use play-json, but commands should use jackson because of ActorRef[T] (see application.conf)
//      JsonSerializer[GreetingMessageChanged],
      JsonSerializer[EmployCreated],
      JsonSerializer[PositionChanged],
      JsonSerializer[EmployState],
      JsonSerializer[EmployerBaseInfo],
      // the replies use play-json as well
      JsonSerializer[Greeting],
      JsonSerializer[Confirmation],
      JsonSerializer[Accepted],
      JsonSerializer[Rejected]
    )
  }

}
