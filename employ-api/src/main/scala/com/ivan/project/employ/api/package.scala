package com.ivan.project.employ

import com.ivan.project.employ.api.PositionApi.Position
import play.api.libs.json.{Format, Json, Reads}


package object api {
  case class BaseInfo(fullName: FullName, position: Position, salary: Double)
  case class newPosition(position: Position)

  case class FullName(first: String, last: String, middle: String){
    override def toString: String = s"$first $last $middle"
  }

  object PositionApi extends Enumeration {
    type Position = Value
    val Developer, Manager, ScramMaster = Value
  }


  implicit lazy val formatEmployBaseInfo: Format[BaseInfo] = Json.format[BaseInfo]
  implicit lazy val formatFullName: Format[FullName] = Json.format[FullName]
  implicit lazy val formatNewPosition: Format[newPosition] = Json.format[newPosition]
  implicit lazy val formatPosition: Reads[PositionApi.Value] = Reads.enumNameReads(PositionApi)
}