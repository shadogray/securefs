# securefs
A client/server filesystem based on EE7 EnterpriseBeans to provide authenticated access to encrypted files 

#Sample Usage
### Streaming API
For a simple example how to use this file system see 
* code of [SecurefsClient](securefs-master/securefs-client-test/src/main/java/at/tfr/securefs/client/SecurefsClient.java "at.tfr.securefs.client.SecurefsClient.java") in project [securefs-client-test](securefs-master/securefs-client-test/)
* invoke SecurefsClient 
```
usage: SecureFile
 -a <arg>   Asynchronous tests
 -b <arg>   Base Directory of Server FileSystem
 -f <arg>   Files to run to/from Server, comma separated list
 -t <arg>   Number of concurrent Threads
```
### SecureFS FileService WebService 
For a simple example how to use this file system see 
* code of [SecurefsFileServiceClient](securefs-master/securefs-client-test/src/main/java/at/tfr/securefs/client/SecurefsFileServiceClient.java "at.tfr.securefs.client.SecurefsFileServiceClient.java") in project [securefs-client-test](securefs-master/securefs-client-test/)

## Sample Script [setupTest.sh](securefs-master/setupTest.sh)
This script simply demonstrates usage of the maven goals to run the SecurefsClient easily.
```
Sending file: 2015-11-21T12:26:09.936+01:00 : ./input/dir1/dir2/small.txt
Reading file: 2015-11-21T12:26:10.740+01:00 : ./input/dir1/dir2/small.txt.out
Checked Checksums: 2015-11-21T12:26:10.776+01:00 : 3283785440 / 3283785440
Sending file: 2015-11-21T12:26:09.936+01:00 : ./input/dir1/dir2/bigfile.iso
Reading file: 2015-11-21T12:26:14.831+01:00 : ./input/dir1/dir2/bigfile.iso.out
Checked Checksums: 2015-11-21T12:26:19.294+01:00 : 4135937012 / 4135937012
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
``` 

# Setup
### Setup-Prerequisite: Download and unpack Wildfly

    cd /work/java/
    wget http://download.jboss.org/wildfly/9.0.2.Final/wildfly-9.0.2.Final.zip 
    unzip wildfly-9.0.2.Final.zip

## Setup SecureFS Service

###1. Clone this repo to local path and switch to it

###2. Build the WebApplication
First w/o activated tests, since some tests rely on deployed and running server

    cd securefs/securefs-master/
    mvn clean install -DskipTests
    cp cp securefs/target/securefs.war /work/java/wildfly-9.0.2.Final/standalone/deployments/

###3. Start Widlfly Cluster Node (HA)

    cd /work/java/wildfly-9.0.2.Final/
    bin/standalone.sh -c standalone-ha.xml

####3.1. Check startup of Securefs webapp

```
2015-11-21 11:24:04,146 INFO  [org.jboss.as.ejb3.deployment.processors.EjbJndiBindingsDeploymentUnitProcessor] (MSC service thread 1-4) JNDI bindings for sessio
n bean named SecureFileBean in deployment unit deployment "securefs.war" are as follows:

        java:global/securefs/SecureFileBean!at.tfr.securefs.api.SecureRemoteFile
...

2015-11-21 11:24:04,147 INFO  [org.jboss.as.ejb3.deployment.processors.EjbJndiBindingsDeploymentUnitProcessor] (MSC service thread 1-4) JNDI bindings for session bean named SecureFileSystemBean in deployment unit deployment "securefs.war" are as follows:

        java:global/securefs/SecureFileSystemBean!at.tfr.securefs.api.SecureFileSystemItf
...

2015-11-21 11:24:04,480 INFO  [org.jboss.ws.cxf.metadata] (MSC service thread 1-2) JBWS024061: Adding service endpoint metadata: id=at.tfr.securefs.FileServiceBean
 address=http://localhost:8080/securefs/FileService
```

###4. Rerun full maven build

```
mvn clean install
```

####4.1. check result of build

```
[INFO] securefs-api ...................................... SUCCESS [  1.665 s]
[INFO] securefs-common ................................... SUCCESS [  2.512 s]
[INFO] securefs .......................................... SUCCESS [  2.481 s]
[INFO] securefs-client ................................... SUCCESS [  0.582 s]
[INFO] securefs-json ..................................... SUCCESS [  2.732 s]
[INFO] securefs-client-test .............................. SUCCESS [  2.593 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

##5. External Dependencies

For a complete build Ikarus libraries are needed and have to be supplied by eternal process.

```
mvn install:install-file -Dfile=../doc/lib/scan-1.0.0.jar -DgroupId=at.ikarus -DartifactId=scan -Dversion=1.0.0 -Dpackaging=jar
```
