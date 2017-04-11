package com.example.service.cassandra

import com.example.service.cassandra.model.ConcreteWordCountByIsbnTable
import com.outworkers.phantom.connectors.ContactPoints
import com.outworkers.phantom.dsl._
import com.example.utilities.sysconf.SystemConfig._

object BookStoreConnector {

  lazy val connector = ContactPoints(cassandraHosts)
    .withClusterBuilder(_.withPort(cassandraPort))
    .keySpace(cassandraKeyspace)

}


class BookStoreDatabase(override val connector: KeySpaceDef) extends Database[BookStoreDatabase](connector) {
  /* here we add all defined tables */
  object WordCountTable extends ConcreteWordCountByIsbnTable with connector.Connector
  WordCountTable.create.ifNotExists().future()

}

object BookStoreDatabase extends BookStoreDatabase(BookStoreConnector.connector)

