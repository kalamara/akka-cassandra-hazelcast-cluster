package com.example.service.cache

import com.example.model.Author
import com.typesafe.scalalogging.StrictLogging

class MemCache extends CacheService[String, Author]
  with StrictLogging
  /* with PicklerHelper */ {


  override def keys: List[String] = ???

  override def shutdown: Unit = {}

  override def getById(id: String): Option[Author] = ???

}
