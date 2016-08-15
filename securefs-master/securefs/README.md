# SecureFS WebApplication

## Services

### Monitoring

### File Management

#### Validation

1. Configuration

By simply resetting the CopyFilesService the default path for Filesystem Validation is set to the default path of SecureFS Configuration.

```
curl -u $USER http://localhost:8080/securefs/rs/services/reset
```

An alternative path may be set by:

```
curl -u $USER -H Content-Type:application/json -d '{"fromRootPath":"/tmp/SECUREFS-Test"}' http://localhost:8080/securefs/rs/services/processFilesData
```

1. Invocation

```
curl -u $USER http://localhost:8080/securefs/rs/services/verify
```

#### Copy FileSystems

1. Configuration

```
curl -u $USER -H Content-Type:application/json \
	-d '{"update":false,"allowOverwriteExisting":true,"validationData":{"threshold":2,"nrOfShares":20,"uiShares":[{"index":1,"share":"63116732139562502"},{"index":2,"share":"120513004896685933"}]},"toRootPath":"/tmp/TESTSEC"}' http://localhost:8080/securefs/rs/services/processFilesData
```

1. Execution

```
curl -u $USER http://localhost:8080/securefs/rs/services/combine

curl -u $USER http://localhost:8080/securefs/rs/services/copyFiles

```

1. Check Status
```
curl -u $USER http://localhost:8080/securefs/rs/internal/processFiles
```

1. Verify Copy Success
```
curl -u $USER http://localhost:8080/securefs/rs/services/verifyCopy
```