package com.example.usecase.booksearch.actor

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, OneForOneStrategy}
import com.example.model._
import com.example.service.cache.CacheService
import com.example.service.cache.filter.BusinessFilters


class CacheWorker[C <: CacheService[String, Author]](cache: C) extends Actor
  with ActorLogging
  with BusinessFilters {

  import scala.language.postfixOps


  override val supervisorStrategy = OneForOneStrategy() {
    case _ => Restart
  }


  override def receive: Receive = {
    case "cache" => sender ! "I got the cache msg"
    case x: String => sender ! x
    case _ => log info "Wrong input to the HotelActor!"

  }

}

