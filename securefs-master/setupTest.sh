mkdir -p /tmp/test
mkdir -p securefs-client-test/target/dir1/dir2/
cp ~/media/iso/openSUSE-Leap-42.1-NET-x86_64.iso securefs-client-test/target/dir1/dir2/bigfile.iso
echo hallo >> securefs-client-test/target/dir1/dir2/small.txt
mvn -pl securefs-client-test/ exec:exec -P Client -Dexec.async=false -Dexec.threads=1 -Dexec.files=./dir1/dir2/small.txt,./dir1/dir2/bigfile.iso

