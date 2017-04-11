package com.example.usecase.booksearch.actor

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy}
import akka.pattern.ask
import com.example.messages._
import com.example.model._
import com.example.service.cache.CacheService
import com.example.service.cassandra.model.WordCountByIsbnRow
import com.example.utilities.sysconf.SystemConfig._

import scala.concurrent.Future


class RequestCoordinator[C <: CacheService[String, Author]](cache: C,
                                                            cacheRouter: ActorRef,
                                                            dbRouter: ActorRef
                                                           ) extends Actor with ActorLogging {

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.language.postfixOps

  override val supervisorStrategy = OneForOneStrategy() {
    case _ => Restart
  }

  override def receive: Receive = {

    case x@WordCountRequestMsg(author, tag) => {

      val origSender = sender

      /* run cache query */
      val cachedBooks: List[Book] = getDataFromCache(x)

      val dbResult: List[Future[Future[List[WordCountByIsbnRow]]]] = {
        cachedBooks.map(b => (dbRouter ? b).mapTo[Future[List[WordCountByIsbnRow]]])
      }

      val cassandraData: Future[List[Future[List[WordCountByIsbnRow]]]] = Future.sequence(dbResult)

      for {
        data: List[Future[List[WordCountByIsbnRow]]] <- cassandraData
      } yield {
        val booksF: Future[List[List[WordCountByIsbnRow]]] = Future.sequence(data)
        val response: Future[WordCountListResponseMsg] = booksF.map { bb =>
          val responses = bb.flatten.map(x => WordCountResponseMsg(x.author, x.isbn, x.word_count, x.year, x.title))
          WordCountListResponseMsg(responses)
        }
        response.map(f => origSender ! f)

      }
    }

    /* Case Else */
    case _ => log info "Wrong input to the RequestCoordinatorActor!"

  }

  def getDataFromCache(request: WordCountRequestMsg): List[Book] = {

    val author: Option[Author] = cache.getById(request.author)
    author.isEmpty match {
      case true => {
        log error "gamithikes"
        List()
      }
      case false =>
        author.get
              .books
              .filter(book =>
                book.title
                    .toLowerCase
                    .contains(request.tag.toLowerCase()))
    }

  }

}



