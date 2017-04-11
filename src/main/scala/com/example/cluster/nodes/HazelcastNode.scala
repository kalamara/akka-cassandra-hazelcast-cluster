package com.example.cluster.nodes

import com.example.utilities.sysconf.SystemConfig
import com.example.utilities.sysconf.SystemConfig.ProfilesConfig
import com.hazelcast.Scala._
import com.hazelcast.config.{Config, XmlConfigBuilder}
import com.typesafe.scalalogging.StrictLogging


object HazelcastNode extends StrictLogging {

  def main(args: Array[String]): Unit = {
    startup()
  }

  def startup(): Unit = {

    val profileName = Option(System.getProperty("run-profile")).getOrElse("production")

    val resource = this
      .getClass
      .getClassLoader
      .getResource("hazelcast.xml")

    val conf: Config = new XmlConfigBuilder(resource).build()

    /* For development only we change the group name so we won't form cluster with the other developers. */
    profileName match {
      case ProfilesConfig.DEVELOPMENT => conf.
        getGroupConfig.
        setName(SystemConfig.hazelcastGroupName.toString)
      case ProfilesConfig.PRODUCTION =>
    }

    conf.newInstance()
    logger info conf.toString

  }

}
