package com.example

import com.example.cluster.nodes.{BackendNode, FrontendNode, HazelcastNode}
import com.example.utilities.sysconf.ArgumentConfigParser.ArgsConfig
import com.example.utilities.sysconf.{AkkaConfiguration, ArgumentConfigParser, SystemConfig}
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging

import scala.language.postfixOps

object Main extends App with StrictLogging {

  val argsConfig: ArgsConfig = ArgumentConfigParser.parser.parse(args, ArgsConfig()) match {
    case Some(conf) => {
      logger info  "-------->: role " + conf.role + " seed-nodes" + conf.seedNodeIpPort + " local node: " + conf.localNodeIpPort
      conf
    }
    case None =>
      // if arguments are wrong, error message will have been displayed
      new ArgsConfig()
  }

  val config: Config = new AkkaConfiguration(argsConfig)
    .withRole
    .withLocalNodeHostname
    .withLocalNodePort
    .withSeeds
    .withLogLevel
    .build


  argsConfig.role match {
    case SystemConfig.roleFrontend => FrontendNode.startup(config)
    case SystemConfig.roleBackend => BackendNode.startup(config)
    case SystemConfig.roleCache => HazelcastNode.startup()
    case _ => {
      logger error "There is no such role"
      System.exit(1)
    }
  }
}