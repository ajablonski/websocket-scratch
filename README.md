# Setup

## Mac
### Install Kafka
1. `brew install kafka`

### Start Kafka
1. Start Zookeeper
`zookeeper-server-start.sh /usr/local/opt/kafka/libexec/config/zookeeper.properties`
2. Start Kafka
`kafka-server-start.sh -daemon /usr/local/opt/kafka/libexec/config/server.properties`

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
