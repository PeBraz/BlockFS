# BlockFS

### File integrity focused filesystem

This project consist of three modules:

 - **blockfs-server** - the remote server responsible to store the filesystem blocks;
 - **blockfs-client** - the client library that provide the necessary functions to write to the remote server;
 - **blockfs-example** - an example command-line app that uses the client library.

## Installation
Since during the build/package process the tests are run, and the client library tests depend on the server being executed, the following execution order is recommended.

### blockfs-server
```
cd blockfs-server
mvn package
java -jar target/blockfs-server-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### blockfs-client

Install the pteidlibj to the Maven repository
```
cd blockfs-client
mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file -Dfile=./lib/pteidlibj.jar
```
Since this is a Maven project, the blockfs-example app will require this library, so is necessary to install it to the local Maven repository.
```
mvn package
mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file -Dfile=./target/blockfs-client-1.0-SNAPSHOT.jar
```

### blockfs-example
```
cd blockfs-example
mvn package
```

## BlockFS Example App Usage

### Save an entire file
```
java -jar target/blockfs-example-1.0-SNAPSHOT-jar-with-dependencies.jar put --user=bob --p hello.txt
```

### Save some user input with a given offset
```
java -jar target/blockfs-example-1.0-SNAPSHOT-jar-with-dependencies.jar put --user=alice --p --start=1
<user-input>
```

### Get entire file
```
java -jar target/blockfs-example-1.0-SNAPSHOT-jar-with-dependencies.jar get --out=received.txt <hash>
```

### Get subset of data
```
java -jar target/blockfs-example-1.0-SNAPSHOT-jar-with-dependencies.jar get --start=0 --size=200 <hash>
```

## BlockFS Integrity Tests

### Receive invalid hash on put_h
The client calls the server with **put_h** to insert a new block remotely. The server will respond with an invalid identifier, as if the block's data was changed. The client needs to invalidate the write.

```
cd blockfs-server
mvn exec:java -Dexec.mainClass="com.blockfs.server.BadServerBot" -Dexec.args="-WPHASH"

cd ../blockfs-client
mvn -Dtest=IntegrityTests#testWriteDataBlockInvalid test
```


### Receive invalid data on get
The client calls the server with **get** to access a remote data block. This block's data been changed and it won't correspond with the hash given by the client.

```
cd blockfs-server
mvn exec:java -Dexec.mainClass="com.blockfs.server.BadServerBot" -Dexec.args="-WGDBHASH"

cd ../blockfs-client
mvn -Dtest=IntegrityTests#testReadDataBlockInvalid test
```


### Receive invalid hash on put_k
The client attempts to access a remote public key block. This block's public key has been changed, and won't correspond with the
client's public key hash.

```
cd blockfs-server
mvn exec:java -Dexec.mainClass="com.blockfs.server.BadServerBot" -Dexec.args="-WGPKHASH"

cd ../blockfs-client
mvn -Dtest=IntegrityTests#testReadPKBlockInvalid test
```


### Receive 400 while reading a public key block
The server receives a **put_k** request with an invalid signature. The server must return an 400 status code.

```
cd blockfs-server
mvn exec:java -Dexec.mainClass="com.blockfs.server.BadServerBot" -Dexec.args="-WCSIG"

cd ../blockfs-client
mvn -Dtest=IntegrityTests#testReadPKBInvalidSignatureAtServer test
```


### Find a wrong signature while reading a public key block
A client reads a public key block from the server. The data has been changed and the signature will be invalid.

```
cd blockfs-server
mvn exec:java -Dexec.mainClass="com.blockfs.server.BadServerBot" -Dexec.args="-WSSIG"

cd ../blockfs-client
mvn -Dtest=IntegrityTests#testReadPKBInvalidSignatureAtClient test
```

### (Part 2) Server invalid certificates in the FS_list() command
```
cd blockfs-server
mvn exec:java -Dexec.mainClass="com.blockfs.server.BadServerBot" -Dexec.args="-WGLISTCERT"

cd ../blockfs-client
mvn -Dtest=ReplayAttackServerToClientTest#testWrongCertificatesReturned test
```

### (Part 2) Simulates a replay attack to the client
```
cd blockfs-server
mvn exec:java -Dexec.mainClass="com.blockfs.server.BadServerBot" -Dexec.args="-WGREPLAYATTACK"

cd ../blockfs-client
mvn -Dtest=ReplayAttackServerToClientTest#testReplayAttackFromServer test
```
