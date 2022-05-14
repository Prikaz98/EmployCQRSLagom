package com.ivan.project.employstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.ivan.project.employstream.api.EmployStreamService
import com.ivan.project.employ.api.EmployService

import scala.concurrent.Future

/**
  * Implementation of the EmployStreamService.
  */
class EmployStreamServiceImpl(employService: EmployService) extends EmployStreamService {
  def stream = ???
//  ServiceCall { hellos =>
////    Future.successful(hellos.mapAsync(8)(employService.hello(_).invoke()))
//  }
}
