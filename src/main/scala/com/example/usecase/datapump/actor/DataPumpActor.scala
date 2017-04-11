package com.example.usecase.datapump.actor

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.example.model.Author
import com.example.service.RowGenerator
import com.example.service.cache.CacheService
import akka.pattern.{ask, pipe}
import com.example.utilities.sysconf.SystemConfig._

import scala.concurrent.Future

class DataPumpActor[C <: CacheService[String, Author]](cache: C,
                                                       databaseRouter: ActorRef)
  extends Actor with ActorLogging
    with RowGenerator {

  import scala.language.postfixOps
  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {

    case tableName: String => {
        val response : List[Future[String]] =
        cache.keys.map(k=> (databaseRouter ? k).mapTo[String])
        pipe(Future.sequence(response)).to(sender)
      }

    case _ => log error "Wrong input to DataPumpActor!"
  }


}
