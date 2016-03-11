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
```
cd blockfs-client
mvn package
```
Since this is a Maven project, the blockfs-example app will require this library, so is necessary to install it to the local Maven repository.
```
mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file -Dfile=./target/blockfs-client-1.0-SNAPSHOT.jar
```

### blockfs-example
```
cd blockfs-client
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
