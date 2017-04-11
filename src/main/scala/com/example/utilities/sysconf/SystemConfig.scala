package com.example.utilities.sysconf

import java.net.NetworkInterface

import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.language.postfixOps


object SystemConfig {

  import scala.collection.JavaConversions._

  val profileName = Option(System.getProperty("run-profile")).getOrElse("production")

  private val config = ConfigFactory.load()
  private val configWithProfile = config.getConfig(s"profile.$profileName")

  /* Akka HTTP config */
  val httpConfig = configWithProfile.getConfig("http")
  val httpInterface = httpConfig.getString("interface")
  val httpPort = httpConfig.getInt("port")

  /* cassandra config */
  val cassandraConfig = configWithProfile.getConfig("db")
  val cassandraPort = cassandraConfig.getInt("cassandra.port")
  val cassandraHosts = cassandraConfig.getStringList("cassandra.hosts").toList
  val cassandraKeyspace = cassandraConfig.getString("cassandra.keyspace")

  /* Akka cluster config */
  val akkaConfig = configWithProfile.getConfig("akka-info")
  val akkaClusterName = akkaConfig.getString("cluster-name")
  val actorTimeout = akkaConfig.getString("actor-timeout").toInt
  implicit val timeout = Timeout(actorTimeout seconds)

  val actorTotalInstances = akkaConfig.getString("actor-total-instances").toInt
  val wordCountActorTotalInstances = akkaConfig.getString("wordcount-request-actor-total-instances").toInt
  val dbDataPumpCoordinatorActorTotalInstances = akkaConfig.getString("datapump-coordinator-actor-total-instances").toInt

  /* number of round robin actor pools */
  val dbActorPoolSize = akkaConfig.getString("db-actor-total-instances").toInt
  val cacheActorsPoolSize = akkaConfig.getString("cache-actor-total-instances").toInt
  val pumpActorsPoolSize = akkaConfig.getString("datapump-actor-total-instances").toInt

  val roleBackend: String = akkaConfig.getString("node-backend")
  val roleFrontend: String = akkaConfig.getString("node-frontend")
  val roleCache: String = akkaConfig.getString("node-cache")

  /* Hazelcast cluster config */
  val hazelcastConfig = configWithProfile.getConfig("hazelcast")
  val hazelcastCulsterSeeds: List[String] =  hazelcastConfig.getStringList("seeds").toList

  val hazelcastGroupName = profileName match {
    case ProfilesConfig.DEVELOPMENT => NetworkInterface.
      getNetworkInterfaces.
      filter(x => !x.isLoopback).
      map(_.getInetAddresses.toList.head.getHostAddress)
      .toList.head
    case ProfilesConfig.PRODUCTION => /* is already defined in hazelcast.xml */
  }

  object ProfilesConfig {
    val DEVELOPMENT = "development"
    val PRODUCTION = "production"
  }
  // hazelcast ip/ cluster names
}


object RouteesPathGenerator {

  import SystemConfig._

  private def generateNames(s: String, size: Int): List[String] = {
    List.range(1, size).map(x => s + x)
  }

  private def generatePaths(names: List[String]): List[String] = {
    names.map(x => "/user/" + x)
  }

  val bookStoreRequestNames = generateNames("bookStoreRequestRoutee", wordCountActorTotalInstances)
  val bookStoreRequestPaths = generatePaths(bookStoreRequestNames)

  val dbDataPumpCoordinatorNames = generateNames("dbCoordinatorRoutee", dbDataPumpCoordinatorActorTotalInstances)
  val dbDataPumpCoordinatorPaths = generatePaths(dbDataPumpCoordinatorNames)

}
