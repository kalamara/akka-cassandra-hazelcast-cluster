package com.example.utilities.sysconf

import com.example.utilities.sysconf.ArgumentConfigParser.ArgsConfig
import com.typesafe.config.{Config, ConfigFactory}


abstract class AkkaConfigBuilder {

  def withRole: AkkaConfigBuilder

  def withSeeds: AkkaConfigBuilder

  def withLocalNodeHostname: AkkaConfigBuilder

  def withLocalNodePort: AkkaConfigBuilder

  def withLogLevel: AkkaConfigBuilder

  def build: Config
}

class AkkaConfiguration(conf: ArgsConfig) extends AkkaConfigBuilder {

  import SystemConfig.akkaClusterName

  var localPort = ""
  var roles = ""
  var localIp = ""
  var seedsStr = ""
  var logLevel = ""
  var profile = ""

  override def withLogLevel: AkkaConfigBuilder = {

    conf.logLevel.isEmpty match {
      case true =>
      case false => logLevel = s"akka.loglevel = ${conf.logLevel}"
    }

    this
  }

  override def withSeeds: AkkaConfigBuilder = {
    val seeds: List[String] = conf.seedNodeIpPort.nonEmpty match {
      case true => conf.seedNodeIpPort.toList.map(str => s"""\"akka.tcp://${akkaClusterName}@$str\"""")
      case false => List()
    }

    seeds.isEmpty match {
      case true =>
      case false => seedsStr = s"akka.cluster.seed-nodes=[${seeds.mkString(",")}]"
    }

    this
  }

  override def withLocalNodeHostname: AkkaConfigBuilder = {
    conf.localNodeIpPort.contains(":") match {
      case true => {
        val ipStr: String = conf.localNodeIpPort.split(":").head
        localIp = "akka.remote.netty.tcp.hostname=" + ipStr
      }
      case false =>
    }

    this
  }

  override def withLocalNodePort: AkkaConfigBuilder = {
    conf.localNodeIpPort.contains(":") match {
      case true => {
        val portStr: String = conf.localNodeIpPort.split(":").tail.head
        localPort = "akka.remote.netty.tcp.port=" + portStr
      }
      case false =>
    }

    this
  }

  override def withRole: AkkaConfigBuilder = {
    conf.role.isEmpty match {
      case true =>
      case false => roles = s"akka.cluster.roles=[${conf.role}]"
    }

    this
  }

  def build: Config =
    ConfigFactory.parseString(localPort).
      withFallback(ConfigFactory.parseString(localIp)).
      withFallback(ConfigFactory.parseString(roles)).
      withFallback(ConfigFactory.parseString(seedsStr)).
      withFallback(ConfigFactory.parseString(logLevel)).
      withFallback(ConfigFactory.load())
}
