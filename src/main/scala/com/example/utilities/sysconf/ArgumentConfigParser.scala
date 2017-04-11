package com.example.utilities.sysconf

object ArgumentConfigParser {

  case class ArgsConfig(role: String = "",
                        seedNodeIpPort: Seq[String] = Seq(),
                        localNodeIpPort: String = "",
                        logLevel: String = "ERROR")

  val parser = new scopt.OptionParser[ArgsConfig]("scopt") {
    head("Availability API", "0.1.0")

    opt[String]('r', "role")
      .action((x, c) => c.copy(role = x))
      .text("role is a string property which defines the role of this node")

    opt[Seq[String]]('s', "seed-node-addr")
      .valueName(",...")
      .required()
      .action((x, c) => c.copy(seedNodeIpPort = x))
      .text("Seed nodes ip:port list. used by the current node to connect to the cluster. " +
        "e.g. 192.168.1.12:2551. for default port use 0")

    opt[String]('n', "local-node-addr")
      .required()
      .action((x, c) => c.copy(localNodeIpPort = x))
      .text("Current node ip:port address to connect to the cluster " +
        " e.g. 192.168.1.12:2551. for default port use 0")

    opt[String]('l', "log-level")
      .action((x, c) => c.copy(logLevel = x))
      .text("log-level is a string property which defines the log-level for this node." +
        " DEBUG, INFO, ERROR. Default is ERROR")


    //    help("help").text("prints this usage text")

  }
}
