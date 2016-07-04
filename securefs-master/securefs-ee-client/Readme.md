Test Client for SecureFS EE Client
==================================

This module provides a EE Singleton, that accesses the SecureFS Service via FileSystem implementation.  

# Classloading

At least on Oracle JDK the ClassLoader also tries to load ZipFS classes, so it is necessary to 
add the classpath to ZipFS to this modules dependencies.

Please see directory "wildfly/modules/at/tfr/securefs/ee-client/main/module.xml"

# Configuration

The access to SecureFS Services uses JBoss Remoting. 

## Configuration of JBoss Remoting

### jboss-ejb-client.properties 

### JBoss Remoting using Scoped Context

An important destinction for use of the client SecureFS FileSystem is the possibility to control 
setup and teardown of the remote connection by FileSystem.close().
This is only possible by using the Scoped Context of JBoss Remoting. 
See src/main/resources/securefs.properties for details on how to configure Scoped Context. 