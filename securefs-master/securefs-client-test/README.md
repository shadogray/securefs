#SecureFS Client Test

## Creation of WebService Client Stack

1. Deploy and start SecureFS Service
2. Change to Client Test Directory and run WSImport
```
cd securefs-client-test
wsimport -Xnocompile -d src/main/java -p at.tfr.securefs.client.ws http://localhost:8080/securefs/FileService?wsdl
```


