package com.example.usecase.datapump.actor

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, OneForOneStrategy}
import com.example.model.Author
import com.example.service.cache.CacheService
import com.example.service.cassandra.TableFiller

class DatabaseActor[C <: CacheService[String, Author]](cache: C)
  extends Actor with ActorLogging {

  override val supervisorStrategy = OneForOneStrategy() {
    case _ => Restart
  }

  override def receive: Receive = {

    case authorName: String => {
      log debug "===========> Author: " + authorName

      val author: Option[Author] = cache.getById(authorName)
      if (author.isEmpty)
        log error "===========> Wrong author."
      else {
        log debug "===========> saving author: " + authorName
        TableFiller.fillWordCountByIsbn(author.get)
        sender ! authorName
      }

    }

    case _ => log error "Wrong input to DatabaseActor!"

  }

}
