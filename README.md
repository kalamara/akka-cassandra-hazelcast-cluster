# akka-cassandra-hazelcast-cluster
akka activator template for high performance akka cassandra hazelcast cluster

Use this template to design horizontally scalable clusters. Akka-cluster is used for distributed data processing, cassandra for a distributed persistance layer and hazelcast as a distributed entity cache. 

The cluster is high performance. No reflection is used anywhere. Phantom is used as a high end cassandra driver, and protobuf for message serialization.

A simple use case of "word count as a web service" is presented with akka-http as a front end.


