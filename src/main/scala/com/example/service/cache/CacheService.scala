package com.example.service.cache

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait CacheService[A, B] {

  def getById(id: A): Option[B]

  def getFutureById(id: A): Future[Option[B]] = Future(getById(id))

  def keys: List[A]

  def shutdown: Unit
}

