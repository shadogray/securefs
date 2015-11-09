mkdir -p /tmp/test
mkdir -p /tmp/source
cp ~/media/iso/openSUSE-Leap-42.1-NET-x86_64.iso /tmp/source/bigfile.iso
echo hallo >> /tmp/source/small.txt
mvn -pl securefs-client-test/ exec:exec -P Client -Dexec.async=true -Dexec.threads=20 -Dexec.files=/tmp/source/small.txt,/tmp/source/bigfile.iso

