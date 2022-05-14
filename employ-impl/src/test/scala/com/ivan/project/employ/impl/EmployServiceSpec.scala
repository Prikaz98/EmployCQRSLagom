package com.ivan.project.employ.impl

import akka.Done
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import com.ivan.project.employ.api._

class EmployServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new EmployApplication(ctx) with LocalServiceLocator
  }

  val client: EmployService = server.serviceClient.implement[EmployService]

  override protected def afterAll(): Unit = server.stop()

  "employ service" should {
/*
    "say hello" in {
      client.hello("Alice").invoke().map { answer =>
        answer should ===("Hello, Alice!")
      }
    }

    "allow responding with a custom message" in {
      for {
        _ <- client.useGreeting("Bob").invoke(GreetingMessage("Hi"))
        answer <- client.hello("Bob").invoke()
      } yield {
        answer should ===("Hi, Bob!")
      }
    }

 */
    "create Employer" in {
      client.createEmployer("01").invoke(BaseInfo(FullName("Vasya", "Ivanov", "Petrovich"), PositionApi.Manager, 21000)).map {
        answer => answer should ===(Done)
      }
    }
  }
}
