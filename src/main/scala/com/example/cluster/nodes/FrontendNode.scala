package com.example.cluster.nodes

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.routing.{ClusterRouterGroup, ClusterRouterGroupSettings}
import akka.event.Logging._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.routing.ConsistentHashingGroup
import akka.stream.ActorMaterializer
import com.example.cluster.metrics.SimpleClusterListener
import com.example.usecase.datapump.DataPumpRoutes
import com.example.usecase.booksearch.BookRoutes
import com.example.usecase.health.HealthRoutes
import com.example.utilities.sysconf.ArgumentConfigParser.ArgsConfig
import com.example.utilities.sysconf.{AkkaConfiguration, RouteesPathGenerator, SystemConfig}
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging

import scala.io.StdIn
import scala.language.postfixOps

object FrontendNode extends Directives with StrictLogging {

  import SystemConfig._

  def main(args: Array[String]) {
    val argsConfig = ArgsConfig(roleFrontend, Seq("127.0.0.1:2551"), "127.0.0.1:2551", "DEBUG")
    //    val conf: Config = ConfigFactory.load()

    val akkaTypesafeConf: Config = new AkkaConfiguration(argsConfig)
      .withRole
      .withLocalNodeHostname
      .withLocalNodePort
      .withSeeds
      .withLogLevel
      .build


    startup(akkaTypesafeConf)
  }

  def startup(akkaTypesafeConf: Config): Unit = {

    implicit val actorSystem = ActorSystem(akkaClusterName, akkaTypesafeConf)
    implicit val executor = actorSystem.dispatcher
    implicit val materializer = ActorMaterializer()

    // Cluster
    val clusterListener = actorSystem.actorOf(Props(classOf[SimpleClusterListener]), "simpleClusterListener")
    //   actorSystem.actorOf(Props[MetricsListener], name = "metricsListener")

    val wordCountRequestRouter: ActorRef = initializeBookStoreRequestRouter(actorSystem)

    val dbDataPumpRouter: ActorRef = initializeDbDataPumpRouter(actorSystem)

    /* Create the routees list to pass to the group router */
    val systemHealth = new HealthRoutes(clusterListener)
    val bRoutes = new BookRoutes(wordCountRequestRouter)
    val dataPumpRoutes = new DataPumpRoutes(dbDataPumpRouter)

    /* BookRoutes */
    val routes = logRequestResult("", InfoLevel)(bRoutes.wordCountRoutes
      ~ systemHealth.healthRoutes
      ~ dataPumpRoutes.dataPumpRoutes)

    val bindingFuture =
      Http().bindAndHandle(routes, httpInterface, httpPort)

    logger info s"Server online at http://localhost:${httpPort}/\nPress RETURN to stop..."

    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete { _ =>
      logger info "--------------> shutting down the resources .."
      actorSystem.terminate() // and shutdown when done

    }

  }

  def initializeDbDataPumpRouter(actorSystem: ActorSystem): ActorRef = {

    val dbDataPumpCoordinatorRouter: ActorRef =
      actorSystem.actorOf(
        ClusterRouterGroup(
          ConsistentHashingGroup(Nil),
          ClusterRouterGroupSettings(
            totalInstances = actorTotalInstances,
            routeesPaths = RouteesPathGenerator.dbDataPumpCoordinatorPaths,
            allowLocalRoutees = false,
            useRole = Some(roleBackend)
          )
        ).props(), "dbDataPumpCoordinatorRouter")

    dbDataPumpCoordinatorRouter
  }

  def initializeBookStoreRequestRouter(actorSystem: ActorSystem): ActorRef = {

    val bookStoreRequestRouter: ActorRef =
      actorSystem.actorOf(
        ClusterRouterGroup(
          ConsistentHashingGroup(Nil),
          ClusterRouterGroupSettings(
            totalInstances = actorTotalInstances,
            routeesPaths = RouteesPathGenerator.bookStoreRequestPaths,
            allowLocalRoutees = false,
            useRole = Some(roleBackend)
          )
        ).props(), "bookStoreRequestRouter")

    bookStoreRequestRouter
  }

}
