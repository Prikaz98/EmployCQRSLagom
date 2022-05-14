package com.ivan.project.employ.impl

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.typed.PersistenceId
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.UUID

class EmployAggregateSpec extends ScalaTestWithActorTestKit(s"""
      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
      akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
      akka.persistence.snapshot-store.local.dir = "target/snapshot-${UUID.randomUUID().toString}"
    """) with AnyWordSpecLike with Matchers {

  "employ aggregate" should {

    /*
    "say hello by default" in {
      val probe = createTestProbe[Greeting]()
      val ref = spawn(EmployBehavior.create(PersistenceId("fake-type-hint", "fake-id")))
      ref ! Hello("Alice", probe.ref)
      probe.expectMessage(Greeting("Hello, Alice!"))
    }

    "allow updating the greeting message" in  {
      val ref = spawn(EmployBehavior.create(PersistenceId("fake-type-hint", "fake-id")))

      val probe1 = createTestProbe[Confirmation]()
      ref ! UseGreetingMessage("Hi", probe1.ref)
      probe1.expectMessage(Accepted)

      val probe2 = createTestProbe[Greeting]()
      ref ! Hello("Alice", probe2.ref)
      probe2.expectMessage(Greeting("Hi, Alice!"))
    }
     */

    "Create new Employ" in {
      val ref = spawn(EmployBehavior.create(PersistenceId("typeHint", "011121")))

      val probe1 = createTestProbe[Confirmation]()
      val baseInfo = EmployerBaseInfo(FullName("Vasya", "Ivanov", "Petrovich"), PositionImpl.Manager, 21000)
      ref ! CreateNewEmploy(baseInfo , probe1.ref)
      probe1.expectMessage(Accepted)
    }
  }
}
