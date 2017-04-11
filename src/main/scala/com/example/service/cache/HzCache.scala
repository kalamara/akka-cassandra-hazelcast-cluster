package com.example.service.cache

import com.example.model.Author
import com.example.utilities.sysconf.SystemConfig._
import com.hazelcast.Scala.client._
import com.hazelcast.Scala.serialization
import com.hazelcast.client.config.{ClientConfig, XmlClientConfigBuilder}
import com.hazelcast.core.IMap
import com.typesafe.scalalogging.StrictLogging

import scala.collection.JavaConverters._

class HzCache extends CacheService[String, Author] with StrictLogging {

  val profileName = Option(System.getProperty("run-profile")).getOrElse("production")

  val resource = this
    .getClass
    .getClassLoader
    .getResource("hazelcast-client.xml")

  val conf: ClientConfig = new XmlClientConfigBuilder(resource).build()

  /* Configure the cluster seed members by profile */
  hazelcastCulsterSeeds.map(ipAddress => conf.getNetworkConfig.getAddresses.add(ipAddress))

  /* For development only we change the group name so we won't form cluster with the other developers. */
  profileName match {
    case ProfilesConfig.DEVELOPMENT => conf.
      getGroupConfig.
      setName(hazelcastGroupName.toString)
    case _ =>
  }

  serialization.Defaults.register(conf.getSerializationConfig)

  val hzClient = conf.newClient()
  logger info conf.toString

  val authorCache: IMap[String, Author] = fillAuthors

  logger info "===========> author map size: " + authorCache.size()


  override def getById(id: String): Option[Author] = Some(authorCache.get(id))

  override def keys: List[String] = authorCache.keySet().asScala.toList

  override def shutdown: Unit = hzClient.shutdown()

  private def fillAuthors: IMap[String, Author] = {
    val ac: IMap[String, Author] = hzClient.getMap("authors")

    ac.isEmpty match {
      case true => {
        val books = CacheFiller.createBooks
        books.map{
          b => ac.put(b.author, Author(b.author, List(b)))
        }

      }
      case false => // do nothing
    }
    ac
  }

}


