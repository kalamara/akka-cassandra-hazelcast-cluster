package com.example.usecase.datapump

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives
import akka.pattern.ask
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope
import akka.util.Timeout
import com.example.utilities.ActorsProtocol.DataPumpCassandraRequest
import com.example.utilities.serialization.JsonSupport

import scala.language.postfixOps

class DataPumpRoutes(dataPumpCoordinatorRouter: ActorRef)(implicit val timeout: Timeout)
  extends Directives
    with JsonSupport {


  val dataPumpRoutes = pathPrefix("datapump") {
    path("cassandra") {
      post {
        parameter(
          'table.as[String]
        ).as(DataPumpCassandraRequest) {
          d => complete((dataPumpCoordinatorRouter ?
            ConsistentHashableEnvelope(d.tableName, System.currentTimeMillis()))
            .mapTo[List[String]])
        }
      }
    }
  }
}
