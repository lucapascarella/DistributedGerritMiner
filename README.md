 
 Base template for distributed customizable computation 
 ======================
 
 Introduction
-----------
This is a base template project aimed at helping a distributed computational mining for software repositories

Requirements
-----------
Computational distribution relies to a message broker for Java language, specifically, Java Message Service (JMS).
In practice this template uses [Apache ActiveMQ](http://activemq.apache.org/ "Apache ActiveMQ")

1. Download wget http://mirror.nohup.it/apache//activemq/5.15.2/apache-activemq-5.15.2-bin.tar.gz
2. unpack download archive
3. run activeMQ server with ./bin/activemq star

Configuration
-----------
The following logistic example explains the roles of hosts and clients in the basic configuration

Local Client ====> Java RMI registry 
192.168.1.10			 192.168.1.10

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