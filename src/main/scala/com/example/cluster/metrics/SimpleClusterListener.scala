package com.example.cluster.metrics

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

import scala.collection.JavaConversions._

class SimpleClusterListener extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  // subscribe to cluster changes, re-subscribe when restart
  override def preStart(): Unit = {

    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])

  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = {
    case MemberUp(member) =>
      log.info("-------------> Member is Up: {}", member.address)
      cluster.state.getMembers.toList.foreach(member => log.info("===========> Total members (after up) : {} with roles : {}",
        member.address, member.getRoles.mkString(",")))

    case UnreachableMember(member) =>
      log.info("-------------> Member detected as unreachable: {}", member)

    case MemberRemoved(member, previousStatus) =>
      log.info("-------------> Member is Removed: {} after {} with roles {} ",
        member.address, previousStatus, member.getRoles.mkString(","))

      cluster.state.getMembers.toList.foreach(member => log.info("===========> Total members (after up) : {} with roles : {}",
        member.address, member.getRoles.mkString(",")))

    case _: MemberEvent => log.info("-------------> Member event : ")
  }
}
