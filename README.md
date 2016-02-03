# Setup

## Mac
1. Install Kafka
`brew install kafka`
2. Start Zookeeper
`zookeeper-server-start.sh /usr/local/opt/kafka/libexec/config/zookeeper.properties`
3. Start Kafka
`kafka-server-start.sh -daemon /usr/local/opt/kafka/libexec/config/server.properties`

## Linux
1. Download Kafka 0.8.2.x from http://kafka.apache.org/downloads.html
2. Extract the contents from the .tgz
3. From within the unzipped kafka package, start Zookeeper
`./bin/zookeeper-server-start.sh config/zookeeper.properties`
4. Start Kafka
`./bin/kafka-server-start.sh config/server.properties`

## All platforms

### Install wscat
1. `npm install wscat`

# Using the app

## Connecting to the same server

1. Start up the application using `sbt run`
2. Create a connection Connect to the running server using `wscat -c localhost:8080/chat?name=USERNAME`, replacing USERNAME with the username you want to connect as

You can create as many connections as you want to simulate multiple chat clients. Any text that a client enters should be broadcast to all clients.

## Connecting across servers
1. Start up the application using `sbt run`
2. Change the server.port property in src/main/resources/application.conf to another available port
3. In another process, start another server using `sbt run`
4. Create a connection to the first server with `wscat -c localhost:<first_server_port>/chat?name=server_1_user`
5. Create a connection to the first server with `wscat -c localhost:<second_server_port>/chat?name=server_2_user`

Any messages sent to the first server should also be visible on the second sever, and vice-versa.
