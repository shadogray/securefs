
DIR=securefs-client-test/target/classes/dir1/dir2/

mvn clean install -DskipTests
mkdir -p $DIR
cp ~/media/iso/openSUSE-Leap-42.1-NET-x86_64.iso ${DIR}/bigfile.iso
echo hallo >> ${DIR}/small.txt
mvn -pl securefs-client-test/ exec:exec -P Client -Dexec.async=true -Dexec.threads=20 -Dexec.files=./dir1/dir2/small.txt,./dir1/dir2/bigfile.iso

