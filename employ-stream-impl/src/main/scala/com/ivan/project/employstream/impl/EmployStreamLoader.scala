package com.ivan.project.employstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.ivan.project.employstream.api.EmployStreamService
import com.ivan.project.employ.api.EmployService
import com.softwaremill.macwire._

class EmployStreamLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new EmployStreamApplication(context) {
      override def serviceLocator: NoServiceLocator.type = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new EmployStreamApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[EmployStreamService])
}

abstract class EmployStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[EmployStreamService](wire[EmployStreamServiceImpl])

  // Bind the EmployService client
  lazy val employService: EmployService = serviceClient.implement[EmployService]
}
