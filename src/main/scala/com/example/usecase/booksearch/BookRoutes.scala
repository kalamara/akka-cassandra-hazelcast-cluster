package com.example.usecase.booksearch


import akka.actor.ActorRef
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope
import akka.util.Timeout
import com.example.messages._
import com.example.utilities.ActorsProtocol.WordCountRequestAdapter
import com.example.utilities.serialization.{JsonSupport, ScalaPBMarshalling}

import scala.language.postfixOps

class BookRoutes(requestCoordinatorRouter: ActorRef)(implicit val timeout: Timeout) extends Directives
  with JsonSupport
  with ScalaPBMarshalling {

  def wordCountRoutes: Route = pathPrefix("book") {
    path("wordcount") {
      get {
        parameters(
          'author.as[String],
          'tag.as[String]
        ).as(WordCountRequestAdapter) {
          w =>
            complete((requestCoordinatorRouter ?   ConsistentHashableEnvelope(
              WordCountRequestMsg(w.author, w.tag),
                System.currentTimeMillis())).mapTo[WordCountListResponseMsg])
        }
      }
    }
  }
}
