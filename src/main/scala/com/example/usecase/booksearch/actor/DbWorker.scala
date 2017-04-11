package com.example.usecase.booksearch.actor

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, OneForOneStrategy}
import com.example.model.Book
import com.example.service.cache.filter.BusinessFilters
import com.example.service.cassandra.service.WordCountService
import com.example.service.cassandra.{BookStoreConnector, BookStoreDatabase}


class DbWorker extends BookStoreDatabase(BookStoreConnector.connector)
  with WordCountService
  with BusinessFilters
  with Actor with ActorLogging {

  import scala.language.postfixOps

  override val supervisorStrategy = OneForOneStrategy() {
    case _ => Restart
  }

  override def receive: Receive = {
    case "cassandra" => sender ! "go the message, yo!"

    case x @ Book(author, title, isbn, year) => sender ! findByIsbn(author, isbn)

    case _ => log info "Wrong input to the HotelActor!"

  }

}
