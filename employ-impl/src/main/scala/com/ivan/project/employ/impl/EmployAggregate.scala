package com.ivan.project.employ.impl

import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.scaladsl._
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect}
import com.lightbend.lagom.scaladsl.persistence.AkkaTaggerAdapter
import play.api.libs.json.{Format, Json}

/**
  * This provides an event sourced behavior. It has a state, [[EmployState]], which
  * stores what the greeting should be (eg, "Hello").
  *
  * Event sourced entities are interacted with by sending them commands. This
  * aggregate supports two commands, a [[UseGreetingMessage]] command, which is
  * used to change the greeting, and a [[Hello]] command, which is a read
  * only command which returns a greeting to the name specified by the command.
  *
  * Commands get translated to events, and it's the events that get persisted.
  * Each event will have an event handler registered for it, and an
  * event handler simply applies an event to the current state. This will be done
  * when the event is first created, and it will also be done when the aggregate is
  * loaded from the database - each event will be replayed to recreate the state
  * of the aggregate.
  *
  * This aggregate defines one event, the [[GreetingMessageChanged]] event,
  * which is emitted when a [[UseGreetingMessage]] command is received.
  */
object EmployBehavior {

  /**
    * Given a sharding [[EntityContext]] this function produces an Akka [[Behavior]] for the aggregate.
    */
  def create(entityContext: EntityContext[EmployCommand]): Behavior[EmployCommand] = {
    val persistenceId: PersistenceId = PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId)

    create(persistenceId)
      .withTagger(
        // Using Akka Persistence Typed in Lagom requires tagging your events
        // in Lagom-compatible way so Lagom ReadSideProcessors and TopicProducers
        // can locate and follow the event streams.
        AkkaTaggerAdapter.fromLagom(entityContext, EmployEvent.Tag)
      )

  }

  /*
   * This method is extracted to write unit tests that are completely independendant to Akka Cluster.
   */
  private[impl] def create(persistenceId: PersistenceId): EventSourcedBehavior[EmployCommand, EmployEvent, EmployState] =
    EventSourcedBehavior
      .withEnforcedReplies[EmployCommand, EmployEvent, EmployState](
        persistenceId = persistenceId,
        emptyState = EmployState.initial,
        commandHandler = (cart, cmd) => cart.applyCommand(cmd),
        eventHandler = (cart, evt) => cart.applyEvent(evt)
      )
}

/**
  * The current state of the Aggregate.
  */
case class EmployState(stateOpt: Option[EmployerBaseInfo]) {

  def applyCommand(cmd: EmployCommand): ReplyEffect[EmployEvent, EmployState] = {
    val (events, confirm) = cmd match {
      case x: CreateNewEmploy => onCreateNewEmploy(x)
      case x: ChangePosition => onChangePosition(x)
    }
    confirm match {
      case _: Accepted =>
        Effect.persist[EmployEvent, EmployState](events).thenReply(cmd.replyTo) { _ => confirm }
      case _: Rejected =>
        Effect.reply[Confirmation, EmployEvent, EmployState](cmd.replyTo) {
          confirm
        }
    }
  }

  def applyEvent(evt: EmployEvent): EmployState =
    evt match {
      case ev: EmployCreated => createEmploy(ev)
      case ev: PositionChanged => changePosition(ev)
    }

  private def onChangePosition(cmd: ChangePosition): (Seq[EmployEvent], Confirmation) = {
    if (this.stateOpt.isEmpty) {
      (Seq.empty, Rejected("employer doesn't exists"))
    }
    else if (this.stateOpt.get.position == cmd.positionImpl) {
      (Seq.empty, Rejected("employer ready have this position"))
    }
    else {
      (Seq(PositionChanged(cmd.positionImpl)), Accepted)
    }
  }

  private def onCreateNewEmploy(x: CreateNewEmploy): (Seq[EmployEvent], Confirmation) = {
    this.stateOpt.map { _ =>
      (Seq.empty, Rejected("employ already exists"))
    }.getOrElse {
      (Seq(EmployCreated(x.baseInfo)), Accepted)
    }
  }


  def createEmploy(ev: EmployCreated): EmployState = {
    copy(Some(ev.baseInfo))
  }

  def changePosition(ev: PositionChanged): EmployState = {
    copy(Some(this.stateOpt.get.copy(position = ev.position)))
  }

}

object EmployState {

  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  def initial: EmployState = EmployState(None)

  /**
    * The [[EventSourcedBehavior]] instances (aka Aggregates) run on sharded actors inside the Akka Cluster.
    * When sharding actors and distributing them across the cluster, each aggregate is
    * namespaced under a typekey that specifies a name and also the type of the commands
    * that sharded actor can receive.
    */
  val typeKey: EntityTypeKey[EmployCommand] = EntityTypeKey[EmployCommand]("EmployAggregate")

  /**
    * Format for the hello state.
    *
    * Persisted entities get snapshotted every configured number of events. This
    * means the state gets stored to the database, so that when the aggregate gets
    * loaded, you don't need to replay all the events, just the ones since the
    * snapshot. Hence, a JSON format needs to be declared so that it can be
    * serialized and deserialized when storing to and from the database.
    */
  implicit val format: Format[EmployState] = Json.format
}

