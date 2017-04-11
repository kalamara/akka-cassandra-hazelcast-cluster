package com.example.cluster.nodes

import akka.actor.{ActorRef, ActorSystem, Deploy, Props}
import akka.cluster.routing.{ClusterRouterGroup, ClusterRouterGroupSettings}
import akka.routing.{ConsistentHashingGroup, RoundRobinPool}
import akka.stream.ActorMaterializer
import com.example.cluster.metrics.SimpleClusterListener
import com.example.model.Author
import com.example.service.cache.{CacheService, HzCache}
import com.example.service.cassandra.{BookStoreConnector, BookStoreDatabase}
import com.example.usecase.datapump.actor.{DataPumpActor, DatabaseActor}
import com.example.usecase.booksearch.actor.{CacheWorker, DbWorker, RequestCoordinator}
import com.example.utilities.sysconf.{AkkaConfiguration, RouteesPathGenerator}
import com.example.utilities.sysconf.ArgumentConfigParser.ArgsConfig
import com.example.utilities.sysconf.SystemConfig._
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.Await

object BackendNode extends StrictLogging {

  def main(args: Array[String]): Unit = {
    val argsConfig = ArgsConfig(roleBackend, Seq("127.0.0.1:2551"), "127.0.0.1:2553", "DEBUG")

    val conf: Config = new AkkaConfiguration(argsConfig)
      .withRole
      .withLocalNodeHostname
      .withLocalNodePort
      .withSeeds
      .withLogLevel
      .build

    startup(conf)
  }

  def startup(config: Config): Unit = {
    val cacheService = new HzCache()

    val dbService = new BookStoreDatabase(BookStoreConnector.connector)
    // Create an Akka system
    implicit val actorSystem = ActorSystem(akkaClusterName, config)
    implicit val executor = actorSystem.dispatcher
    implicit val materializer = ActorMaterializer()

    /* Cluster */
    actorSystem.actorOf(Props(classOf[SimpleClusterListener]), "simpleClusterListener")
    // actorSystem.actorOf(Props[MetricsListener], name = "metricsListener")

    initializeDbDataPumpRouter(actorSystem, cacheService)

    initializeBookStoreRequestRouter(actorSystem, cacheService)

    // shutdown Hook
    scala.sys.addShutdownHook {
      logger info "Disconnecting cache..."
      cacheService.shutdown
      logger info "Disconnecting database..."
      dbService.shutdown
      actorSystem.terminate
      Await.result(actorSystem.whenTerminated, 30 seconds)
      logger info "Actor system terminated... Bye"
    }

  }

  def initializeBookStoreRequestRouter[C>:CacheService[String, Author]](actorSystem: ActorSystem,
                                                                        cacheService: C): ActorRef = {

    val cacheRouter: ActorRef =
      actorSystem.actorOf(
        RoundRobinPool(cacheActorsPoolSize).props(
          Props(classOf[CacheWorker[C]],
            cacheService
          )
        ), "cacheRouter")

    val dbRouter: ActorRef =
      actorSystem.actorOf(
        RoundRobinPool(dbActorPoolSize).props(
          Props(classOf[DbWorker])
        ), "dbRouter")

    RouteesPathGenerator.bookStoreRequestNames.map(x =>
      actorSystem.actorOf(
        Props(
          classOf[RequestCoordinator[C]],
          cacheService,
          cacheRouter,
          dbRouter
        ), x
      )
    )

    val bookStoreRequestRouter: ActorRef =
      actorSystem.actorOf(
        ClusterRouterGroup(
          ConsistentHashingGroup(Nil),
          ClusterRouterGroupSettings(
            totalInstances = actorTotalInstances,
            routeesPaths = RouteesPathGenerator.bookStoreRequestPaths,
            allowLocalRoutees = true,
            useRole = Some(roleBackend)
          )
        ).props(), "bookStoreRequestRouter"
      )

    bookStoreRequestRouter
  }


  def initializeDbDataPumpRouter[C>:CacheService[String, Author]](actorSystem: ActorSystem,
                                                               cacheService: C): ActorRef = {

    val dbDataPumpRouter: ActorRef =
      actorSystem.actorOf(
        RoundRobinPool(pumpActorsPoolSize)
          .props(
            Props(classOf[DatabaseActor[C]],
              cacheService
            ).withDeploy(Deploy.local)), "dbDataPumpRouter")

    RouteesPathGenerator.dbDataPumpCoordinatorNames.map(x =>
      actorSystem.actorOf(
        Props(
          classOf[DataPumpActor[C]],
          cacheService,
          dbDataPumpRouter
        ), x
      )
    )

    val dbDataPumpCoordinatorRouter: ActorRef =
      actorSystem.actorOf(
        ClusterRouterGroup(
          ConsistentHashingGroup(Nil),
          ClusterRouterGroupSettings(
            totalInstances = actorTotalInstances,
            routeesPaths = RouteesPathGenerator.dbDataPumpCoordinatorPaths,
            allowLocalRoutees = true,
            useRole = Some(roleBackend)
          )
        ).props(), "dbDataPumpCoordinatorRouter")

    dbDataPumpCoordinatorRouter
  }
}

