mvn -pl securefs-client-test clean package -DskipTests
mkdir -p /tmp/test
mkdir -p securefs-client-test/target/classes/input/dir1/dir2/
echo hallo >> securefs-client-test/target/classes/input/dir1/dir2/small.txt
mvn -X -pl securefs-client-test/ exec:exec -P FileServiceClient -Dexec.files=./input/dir1/dir2/small.txt

