 
 Base template for distributed customizable computation 
 ======================
 
 Introduction
-----------
This is a base template project aimed at helping a distributed computational mining for software repositories

Pre-requirements
-----------

Topics
-----------
In JMS a Topic implements publish and subscribe semantics. When you publish a message it goes to all the subscribers who are interested - so zero to many subscribers will receive a copy of the message. Only subscribers who had an active subscription at the time the broker receives the message will get a copy of the message.

Queues
-----------
A JMS Queue implements load balancer semantics. A single message will be received by exactly one consumer. If there are no consumers available at the time the message is sent it will be kept until a consumer is available that can process the message. If a consumer receives a message and does not acknowledge it before closing then the message will be redelivered to another consumer. A queue can have many consumers with messages load balanced across the available consumers.

So Queues implement a reliable load balancer in JMS.

Requirements
-----------
Computational distribution relies to a message broker for Java language, specifically, Java Message Service (JMS).
In practice this template uses [Apache ActiveMQ](http://activemq.apache.org/ "Apache ActiveMQ")

1. Download Apache ActiveMQ executable package: `wget http://it.apache.contactlab.it//activemq/5.15.8/apache-activemq-5.15.8-bin.tar.gz`
2. unpack download archive: `tar -xzvf apache-activemq-5.15.8-bin.tar.gz`
3. run activeMQ server with `./bin/activemq start`

Configuration
-----------
The following logistic example explains the roles of hosts and clients in the basic configuration

Local Client (producer) ====>  Message broker <==== (Remote Clients) Consumers
192.168.1.10					 192.168.1.10			

#### Apache AcriveMQ installation

Usage
-----------

#### Startup sequence
1. start rmiregistry (ie. )
2. start RMIServer (Remote methods executor)
3. start RMIClient (Local dispatcher client)
 
 * startup procedure:
 * 1 - JMSProvider (ActiveMQ)
 * 2 - Repliche
 * 3 - rmiregistry
 * 4 - Server
 * 5 - Client