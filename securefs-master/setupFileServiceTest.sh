#mvn clean install -DskipTests
mkdir -p /tmp/test
mkdir -p securefs-client-test/target/classes/input/dir1/dir2/
echo hallo >> securefs-client-test/target/classes/input/dir1/dir2/small.txt
files=./input/dir1/dir2/small.txt

if [ -f ""$1 ] ; then 
    file=`basename $1`
    cp $1 securefs-client-test/target/classes/input/dir1/dir2/${file};
    files=${files},./input/dir1/dir2/${file} 
fi

mvn -pl securefs-client-test/ exec:exec -P FileServiceClient -Dexec.files=${files}

