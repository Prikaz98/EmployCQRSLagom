package com.ivan.project.employ.impl

import akka.Done
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.util.Timeout
import com.ivan.project.employ.api
import com.ivan.project.employ.api.{EmployService, TopicEvents, newPosition}
import com.ivan.project.employ.impl.Mapper.{employBaseInfoTo, positionApiToImpl}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * Implementation of the EmployService.
  */
class EmployServiceImpl(
                         clusterSharding: ClusterSharding,
                         persistentEntityRegistry: PersistentEntityRegistry
                       )(implicit ec: ExecutionContext)
  extends EmployService {

  /**
    * Looks up the entity for the given ID.
    */
  private def entityRef(id: String): EntityRef[EmployCommand] =
    clusterSharding.entityRefFor(EmployState.typeKey, id)

  implicit val timeout: Timeout = Timeout(5.seconds)

  /**
    * создание агрегата "работника"
    * @param id - идентификатор работника
    * @return
    */
  override def createEmployer(id: String): ServiceCall[api.BaseInfo, Done] = { baseInfo =>
    val ref = entityRef(id)
    val bi = employBaseInfoTo(baseInfo)
    ref.ask[Confirmation] { replyTo =>
      CreateNewEmploy(baseInfo = bi, replyTo = replyTo)
    }
      .map {
        case _: Accepted => Done
        case rej: Rejected => throw BadRequest(rej.reason)
      }
  }

  /**
    * изменение должности работника
    * @param id - идентификатор работника
    * @return
    */
  override def changePosition(id: String): ServiceCall[newPosition, Done] = { newPosition =>
    val ref = entityRef(id)
    val apiPosition = positionApiToImpl(newPosition.position)
    ref.ask[Confirmation] { replyTo =>
      ChangePosition(apiPosition, replyTo)
    }.map {
      case _: Accepted => Done
      case Rejected(reason) => throw BadRequest(reason)
    }
  }

  def abstractTopic[T](mapper: EventStreamElement[EmployEvent] => T): Topic[T] = {
    TopicProducer.singleStreamWithOffset { fromOffset =>
      persistentEntityRegistry
        .eventStream(EmployEvent.Tag, fromOffset)
        .map(ev => (mapper(ev), ev.offset))
    }
  }

  override def employTopic: Topic[TopicEvents] = abstractTopic { streamElement =>
    streamElement.event match {
      case EmployCreated(baseInfo) =>
        api.EmployCreated(streamElement.entityId, Mapper.employBaseInfoTo(baseInfo))
      case PositionChanged(position) =>
        api.EmployerChangePosition(streamElement.entityId, Mapper.positionImplToApi(position))
    }
  }
}
