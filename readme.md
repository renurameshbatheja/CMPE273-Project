#CMPE273-Class Project : Build a scalable web cache using HRW hashing on top of Redis

##About this Project

This is a class project for building a scalable web cache using HRW hashing
on
top of Redis.

### Redis Clients equipped with HRW (RendezvousHash) hashing.

Highest Random Weight (HRW OR Rendezvous) hashing is an algorithm that
allows
clients to achieve distributed agreement on a set of k options out of a
possible set of n options.

In a given cluster topology, for an input key, a hash is calculated
concatenating key with unique node information (e.g. IP/port or pre-assigned
id). The key is assigned to node with highest hash.

Above algorithm generates consistent view for given key across all nodes.
Also
the number of keys moved while adding/deleting nodes is minimized.

Multiple Redis Clients could be deployed on different Tomcat containers. 
As every Client is equipped with HRW hashing algorithm, there would be 
no SPOF (Single Point Of Failure) in the System.

### Web cache :

The search results obtained through Google Search Engine are stored in Redis
Instances. The key-value pair of 'Query Url' and 'Compressed web page
content'
are saved in the in-memory Redis caches on differnt instances.

### Dynamic addition/removal of Redis Instances.

#### Addition of an Instance :

When entered correct values for Host IP, Port and ID (for any running Redis
Instance which needs to be added), RedisClient takes care of adding that
particular instance in the system and gives that instance its share of
key-value pairs from web cache after performing the rehashing using HRW
algorithm.

For all keys on all the nodes, HRW is calculated with new topology and keys
are distributed accordingly.

#### Removal of an Instance :

When entered correct values for Host IP, Port and ID (for any running Redis
Instance which needs to be taken down), RedisClient takes care of removing
that
particular instance from the system and distributes its share of key-value
pairs among the other Instances after performing the rehashing using HRW
algorithm.

For all keys on the node to be deleted, HRW is calculated with new topology
and
keys are distributed accordingly.

## Building CMPE273-Class Project

```
cd redis

Open resources/config.properties and add your Redis Instances information in the form of IP:Port[,IP:Port]*
(These Redis Instances are the ones which are statically configured with the System at server start-up)
e.g. redisInstances=192.168.1.196:6379,192.168.1.196:10001,localhost:10002

ant war

Deploy target/redisapp.war into Tomcat7 container.
```

## Access RedisWebapp

```
http://localhost:8080/RedisWebapp
```



As seen in the UI, it has three different sections to test
####1. View Redis Server Information for already configured and available
Redis Instances.

This displays all required parameters for 'monitoring' purpose.

####2. Dynamically adding/removing of a Redis Instance
- Addition of an Instance
- Removal of an Instance

####3. Getting Results stored in a Redis Instance

A particular 'Query String' could be queried against the Redis Web cache and
found out if it is already available in the cache or not.
