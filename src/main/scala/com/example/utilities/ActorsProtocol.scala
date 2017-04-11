package com.example.utilities

object ActorsProtocol {

  sealed trait ActorsProtocol

  sealed trait ActorRequest extends ActorsProtocol

  sealed trait ActorResponse extends ActorsProtocol

  final case class DataPumpHazelcastRequest(collectionName: String) extends ActorRequest

  final case class DataPumpCassandraRequest(tableName: String) extends ActorRequest

  final case class FillTableRequest(tableName: String, hotelId: Long) extends ActorRequest

  final case class FillCollectionRequest(collectionName: String, hotelId: Long) extends ActorRequest

  final case class BookPayload(author: String,
                               country: String,
                               language: String,
                               link: String,
                               pages: Int,
                               title: String,
                               year: Int)

  final case class BookList(books: List[BookPayload])

  final case class WordCountRequestAdapter(author: String,
                                           tag: String)

}
