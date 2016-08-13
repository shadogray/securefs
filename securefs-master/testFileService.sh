# -u: ${exec.fileServiceUrl}
# -a: ${exec.async}
# -t: ${exec.threads}
# -f: ${exec.files}
# -i: ${exec.identity}
# -p: ${exec.password}

# Files in lib: 
#	commons-cli-1.3.1.jar  
#	commons-io-2.4.jar  
#	commons-lang-2.6.jar  
#	joda-time-2.9.1.jar  
#	securefs-client-1.3.0-SNAPSHOT.jar  
#	securefs-client-test-1.3.0-SNAPSHOT.jar
# example: -Dexec.fileServiceUrl='http://localhost:8080/securefs/FileService' -Dexec.identity='user' -Dexec.password='User0815!' 

# Properties for SSL Connectivity
#JAVA_OPTS="${JAVA_OPTS} -Djavax.net.ssl.trustStore=lib/keystore"
#JAVA_OPTS="${JAVA_OPTS} -Djavax.net.debug=ssl:all"

cp securefs-client/target/securefs-client.jar lib/
cp securefs-client-test/target/securefs-client-test.jar lib/

java ${JAVA_OPTS} -cp 'lib/*' at.tfr.securefs.client.SecurefsFileServiceClient $*
