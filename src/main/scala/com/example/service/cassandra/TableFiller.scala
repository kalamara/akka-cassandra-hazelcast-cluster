package com.example.service.cassandra

import com.example.model._
import com.example.service.RowGenerator
import com.example.service.cassandra.model.WordCountByIsbnRow


object TableFiller extends BookStoreDatabase(BookStoreConnector.connector)
  with RowGenerator {

  def fillWordCountByIsbn(auth: Author): Unit = {
    val bookRows: Stream[WordCountByIsbnRow] = generateRows(auth)

    WordCountTable.storeBatch(bookRows) /* bookRows.foreach(WordCountTable.store(_))) */

  }

}
