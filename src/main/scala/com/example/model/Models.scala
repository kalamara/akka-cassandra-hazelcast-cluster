package com.example.model

final case class Book(author: String,
                      title: String,
                      isbn: Long,
                      year: Int)

final case class Author(name: String, books: List[Book]) //map of books by publication year