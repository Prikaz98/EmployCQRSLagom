package com.ivan.project.employ.api

import play.api.libs.json.{Format, Json}

object APIEvent {


  sealed trait TopicEvents{
    def id : String
  }

  /**
    * The greeting message class used by the topic stream.
    * Different than [[GreetingMessage]], this message includes the name (id).
    */
  case class GreetingMessageChanged(name: String, message: String) extends TopicEvents {
    override def id: String = name
  }

  object GreetingMessageChanged {
    /**
      * Format for converting greeting messages to and from JSON.
      *
      * This will be picked up by a Lagom implicit conversion from Play's JSON format to Lagom's message serializer.
      */
    implicit val format: Format[GreetingMessageChanged] = Json.format[GreetingMessageChanged]
  }

  case class EmployCreated(_id : String , baseInfo: BaseInfo) extends TopicEvents {
    override def id: String = _id
  }

  object EmployCreated {
    implicit val format: Format[EmployCreated] = Json.format[EmployCreated]
  }


}
