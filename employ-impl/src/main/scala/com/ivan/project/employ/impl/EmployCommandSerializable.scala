package com.ivan.project.employ.impl

import akka.actor.typed.ActorRef
import com.ivan.project.employ.impl.PositionImpl.PositionImpl

trait EmployCommandSerializable

/**
  * This interface defines all the commands that the EmployAggregate supports.
  */
sealed trait EmployCommand
  extends EmployCommandSerializable{
  def replyTo : ActorRef[Confirmation]
}
/*
/**
  * A command to switch the greeting message.
  *
  * It has a reply type of [[Confirmation]], which is sent back to the caller
  * when all the events emitted by this command are successfully persisted.
  */
case class UseGreetingMessage(message: String, replyTo: ActorRef[Confirmation])
  extends EmployCommand

/**
  * A command to say hello to someone using the current greeting message.
  *
  * The reply type is String, and will contain the message to say to that
  * person.
  */
case class Hello(name: String, replyTo: ActorRef[Greeting])
  extends EmployCommand
 */

case class CreateNewEmploy(baseInfo: EmployerBaseInfo, replyTo: ActorRef[Confirmation])
  extends EmployCommand
case class ChangePosition(positionImpl: PositionImpl , replyTo : ActorRef[Confirmation])
  extends EmployCommand

