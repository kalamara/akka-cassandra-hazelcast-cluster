package com.example.service

import com.example.model.Author
import com.example.service.cache.filter.BusinessFilters
import com.example.service.cassandra.model.WordCountByIsbnRow

import scala.util.Random

object RowGenerator {

}

trait RowGenerator extends BusinessFilters {

  def generateRows(auth: Author): Stream[WordCountByIsbnRow] = {
     auth.books.map(b => WordCountByIsbnRow(
       author = auth.name,
       isbn = b.isbn,
       year = b.year,
       title = b.title,
       word_count = Random.nextInt
     )).toStream
  }

}


