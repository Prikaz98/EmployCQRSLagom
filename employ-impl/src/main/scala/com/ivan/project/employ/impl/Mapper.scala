package com.ivan.project.employ.impl

import com.ivan.project.employ.api
import com.ivan.project.employ.api.PositionApi


object Mapper {

  def employBaseInfoTo(info: api.BaseInfo): EmployerBaseInfo = {
    EmployerBaseInfo(
      fullName = fullNameTo(info.fullName),
      position = positionApiToImpl(info.position),
      salary = info.salary
    )
  }
  def employBaseInfoTo(info: EmployerBaseInfo): api.BaseInfo = {
    api.BaseInfo(
      fullName = fullNameTo(info.fullName),
      position = positionImplToApi(info.position),
      salary = info.salary
    )
  }

  def positionApiToImpl(pos: api.PositionApi.Position): PositionImpl.PositionImpl = {
    pos match {
      case api.PositionApi.Developer => PositionImpl.Developer
      case api.PositionApi.Manager => PositionImpl.Manager
      case api.PositionApi.ScramMaster => PositionImpl.ScramMaster
    }
  }
  def positionImplToApi(pos: PositionImpl.PositionImpl): api.PositionApi.Position = {
    pos match {
      case PositionImpl.Developer => PositionApi.Developer
      case PositionImpl.Manager => PositionApi.Manager
      case PositionImpl.ScramMaster => PositionApi.ScramMaster
    }
  }

  def fullNameTo(fullName: api.FullName): FullName = {
    FullName(
      first = fullName.first,
      last = fullName.last,
      middle = fullName.middle
    )
  }

  def fullNameTo(fullName: FullName): api.FullName = {
    api.FullName(
      first = fullName.first,
      last = fullName.last,
      middle = fullName.middle
    )
  }
}
