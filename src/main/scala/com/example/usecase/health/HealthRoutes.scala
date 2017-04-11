package com.example.usecase.health

import java.lang.management.ManagementFactory

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.Cluster
import akka.http.scaladsl.server.Directives
import scala.concurrent.duration._
import scala.collection.JavaConversions._

import scala.concurrent.duration._

class HealthRoutes(clusterListener: ActorRef)(implicit val actorSystem: ActorSystem) extends Directives {

  val cluster = Cluster(actorSystem)

  val healthRoutes = pathPrefix("health") {
    path("ping") {
      get {
        complete("OK")
      }
    } ~ path("uptime") {
      get {
        complete(getUptime.toString + " seconds")
      }
    } ~ path("nodes") {
      complete(cluster.state.getMembers.toList.map(member => member.address).mkString(","))
    }
  }
  private def getUptime = Duration(ManagementFactory.getRuntimeMXBean.getUptime, MILLISECONDS).toSeconds
}
