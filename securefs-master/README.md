#Projects and Folders

## securefs
This is the EE7 web application representing the SecureFS service.

## securefs-common
This module provides the server side implementation of the SecureFileSystem (see SecureFileSystemBean and SecureFileBean).

### Configuration 
The service is configured by the securefs-server.properties file. A default file is located in the module itself. 

### Wildfly module at.tfr.securefs.configuration
To provide runtime configuration, a sample module is provided in the directory ./wildfly/.
This module directory structure is to be copied to your Wildfly's modules directory and provides full configurability of all properties known to SecureFS.
```
cp -r securefs-master/wildfly/at ${JBOSS_HOME}/modules/
```

#### Check correct loading of properties
See wildfly server.log on startup of the application (e.g. first access to SecureFS service):
``` 
INFO  [at.tfr.securefs.Configuration] (EJB default - 10) loaded from: file:/work/java/wildfly-9.0.2.Final/modules/at/tfr/securefs/configuration/main/../securefs-server.properties
INFO  [at.tfr.securefs.Configuration] (EJB default - 10) KeyAlgorithm = AES
INFO  [at.tfr.securefs.Configuration] (EJB default - 10) CipherAlgorithm = AES/CBC/PKCS5Padding
INFO  [at.tfr.securefs.Configuration] (EJB default - 10) PaddingCipherAlgorithm = AES/CBC/PKCS5Padding
INFO  [at.tfr.securefs.Configuration] (EJB default - 10) Salt = saltsaltsaltsalt
INFO  [at.tfr.securefs.Configuration] (EJB default - 10) Test = true
INFO  [at.tfr.securefs.Configuration] (EJB default - 10) BasePath = /tmp/SECUREFS
```

## securefs-client
The client FS for the SecureFS service. It provides a Java 1.8 compliant filesystem implementation.

## securefs-client-test
This module provides a sample implementation of the Stream API of SecureFS as JavaSE implementation.
Two client implementations are available:
* SecurefsClient - implements Streaming API
* SecurefsFileServiceClient - implements WebService API

Both provide a simple command line and may be run as Maven Exec goal. On any input argument error a usage will be dumped.

### SecurefsClient - Command Line Parameters & Usage

```
usage: SecurefsClient
 -a <arg>   Asynchronous tests
 -b <arg>   Base Directory of Server FileSystem
 -f <arg>   Files to run to/from Server, comma separated list
 -t <arg>   Number of concurrent Threads
```
### Maven Exec Goal - Profile Client
The simplest use case is the execution of the preconfigured Maven Exec goal:
```
	mvn -pl securefs-client-test -P Client exec:exec
```
The following command line parameters are available, analoguos to the command line parameters of SecureFile class:
```
exec.async	default: false
exec.threads	default: 10
exec.baseDir 	default: sec://${basedir}/target/
exec.files 	default: data/test/test.txt,data/test/test_main.txt
```
#### Example: execute the upload/download test in 10 concurrent threads
```
mvn -pl securefs-client-test -P Client exec:exec -Dexec.async=true
```

will produce an output similar to:
```
Thread[pool-1-thread-6,5,main]: Sending file: 2015-11-21T17:29:52.930+01:00 : data/test/test.txt.14
Thread[pool-1-thread-4,5,main]: Sending file: 2015-11-21T17:29:52.930+01:00 : data/test/test.txt.12
...
Thread[pool-1-thread-4,5,main]: Reading file: 2015-11-21T17:29:53.910+01:00 : data/test/test.txt.12.out
Thread[pool-1-thread-2,5,main]: Reading file: 2015-11-21T17:29:53.911+01:00 : data/test/test.txt.10.out
...
Thread[pool-1-thread-2,5,main]: Checked Checksums: 2015-11-21T17:29:54.030+01:00 : 2486928614 / 2486928614
Thread[pool-1-thread-10,5,main]: Checked Checksums: 2015-11-21T17:29:54.031+01:00 : 2486928614 / 2486928614
...
Thread[pool-1-thread-4,5,main]: Sending file: 2015-11-21T17:29:52.930+01:00 : data/test/test_main.txt.12
Thread[pool-1-thread-1,5,main]: Sending file: 2015-11-21T17:29:52.929+01:00 : data/test/test_main.txt.9
...
Thread[pool-1-thread-4,5,main]: Reading file: 2015-11-21T17:29:54.101+01:00 : data/test/test_main.txt.12.out
Thread[pool-1-thread-1,5,main]: Reading file: 2015-11-21T17:29:54.102+01:00 : data/test/test_main.txt.9.out
...
Thread[pool-1-thread-4,5,main]: Checked Checksums: 2015-11-21T17:29:54.203+01:00 : 4109673046 / 4109673046
Thread[pool-1-thread-1,5,main]: Checked Checksums: 2015-11-21T17:29:54.204+01:00 : 4109673046 / 4109673046
...
Nov 21, 2015 5:29:54 PM org.jboss.ejb.client.remoting.ChannelAssociation$ResponseReceiver handleEnd
INFO: EJBCLIENT000016: Channel Channel ID d9f85a49 (outbound) of Remoting connection 287c4407 to localhost/127.0.0.1:8080 can no longer process messages
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### SecurefsFileServiceClient - Command Line Parameters & Usage
This Client implements the SecureFS FileService.
```
usage: SecurefsFileServiceClient
 -a <arg>   Asynchronous tests, default: false
 -f <arg>   Files to run to/from Server, comma separated list
 -t <arg>   Number of concurrent Threads, default: 1
 -u <arg>   Service URL, default:
            http://localhost:8080/securefs/FileService?wsdl
```

### Maven Exec Goal - Profile FileServiceClient
```
mvn -pl securefs-client-test -P FileServiceClient exec:exec
```

The following command line parameters are available:
```
exec.fileServiceUrl    	default: http://localhost:8080/securefs/FileService?wsdl
exec.async      	default: false
exec.threads    	default: 10
exec.files      	default: data/test/test.txt,data/test/test_main.txt
```
will produce an output similar to:
```
Thread[main,5,main]: Sending file: 2015-11-21T18:59:09.520+01:00 : data/test/test.txt
Thread[main,5,main]: Reading file: 2015-11-21T18:59:10.004+01:00 : data/test/test.txt.out
Thread[main,5,main]: Checked Checksums: 2015-11-21T18:59:10.048+01:00 : 2486928614 / 2486928614
Thread[main,5,main]: Sending file: 2015-11-21T18:59:09.520+01:00 : data/test/test_main.txt
Thread[main,5,main]: Reading file: 2015-11-21T18:59:10.103+01:00 : data/test/test_main.txt.out
Thread[main,5,main]: Checked Checksums: 2015-11-21T18:59:10.130+01:00 : 4109673046 / 4109673046
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------

```
