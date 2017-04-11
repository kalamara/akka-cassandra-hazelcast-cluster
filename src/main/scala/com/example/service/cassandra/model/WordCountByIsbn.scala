package com.example.service.cassandra.model

import com.outworkers.phantom.dsl._

import scala.annotation.tailrec
import scala.concurrent.Future

sealed class WordCountByIsbnTable extends CassandraTable[ConcreteWordCountByIsbnTable, WordCountByIsbnRow] {
  override def tableName: String = "word_count_by_isbn"

  // noinspection ScalaStyle
  /* partition keys */
  object author extends StringColumn(this) with PartitionKey

  object isbn extends LongColumn(this) with PrimaryKey

  /* data */
  object word_count extends IntColumn(this)

  object year extends IntColumn(this)

  object title extends StringColumn(this)

}

abstract class ConcreteWordCountByIsbnTable extends WordCountByIsbnTable with RootConnector {

  val BATCH_SIZE = 100

  def findByAuthorIsbn(author: String, isbn: Long): Future[List[WordCountByIsbnRow]] = {

    lazy val defaultQuery = select.all()
      .where(_.author eqs author)
      .and(_.isbn eqs isbn)

    defaultQuery.fetch
  }

  def store(row: WordCountByIsbnRow): Future[ResultSet] = {
    insert
      .value(_.author, row.author)
      .value(_.isbn, row.isbn)
      .value(_.word_count, row.word_count)
      .value(_.year, row.year)
      .value(_.title, row.title)
      .future()
  }

  def storeBatch(wordCountByIsbnRow: Stream[WordCountByIsbnRow]): Future[ResultSet] = {

    def executeBatch(li: List[WordCountByIsbnRow]): Future[ResultSet] = {

      Batch.logged.add(li.map(row => {
        insert
          .value(_.author, row.author)
          .value(_.isbn, row.isbn)
          .value(_.word_count, row.word_count)
          .value(_.year, row.year)
          .value(_.title, row.title)
      }
      ).toIterator
      ).future()
    }

    @tailrec
    def go(remaining: Stream[WordCountByIsbnRow], counter: Int): Future[ResultSet] = {
      val li: List[WordCountByIsbnRow] = remaining.take(BATCH_SIZE).toList
      val result = executeBatch(li)

      if (remaining.nonEmpty) {
        logger debug "=======> getting next batch  " + counter
        go(remaining.drop(BATCH_SIZE), counter + 1)
      }
      else {
        logger info "=======> getting final batch of " + counter
        result
      }
    }
    go(wordCountByIsbnRow, 1)
  }
}


final case class WordCountByIsbnRow(author: String,
                                    isbn: Long,
                                    word_count: Int,
                                    year: Int,
                                    title: String)

