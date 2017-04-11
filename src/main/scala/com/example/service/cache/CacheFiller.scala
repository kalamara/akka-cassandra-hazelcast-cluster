package com.example.service.cache

import com.example.model.Book
import com.example.utilities.ActorsProtocol._
import com.example.utilities.serialization.JsonSupport
import spray.json._

import scala.util.Random


object CacheFiller extends JsonSupport {
  def createBooks: List[Book] = {

    val resource = this
      .getClass
      .getClassLoader
      .getResourceAsStream("books.json")

    val input = scala.io.Source.fromInputStream(resource)("UTF-8").mkString.parseJson
    val bookList = input.convertTo[BookList]

    bookList.books.map(b => Book(author = b.author,
      isbn = Random.nextLong,
      year = b.year,
      title = b.title))
  }

  object Config {


  }

}
