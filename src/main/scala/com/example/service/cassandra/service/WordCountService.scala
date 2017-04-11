package com.example.service.cassandra.service

import com.example.service.cassandra.BookStoreDatabase
import com.example.service.cassandra.model.WordCountByIsbnRow
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

trait WordCountService extends BookStoreDatabase {

  def findByIsbn(author: String, isbn: Long): Future[List[WordCountByIsbnRow]]= {

    WordCountTable.findByAuthorIsbn(author, isbn)
  }

  def store(row: WordCountByIsbnRow): Future[ResultSet] = {
    WordCountTable.store(row)
  }
}
