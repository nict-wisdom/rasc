#!/bin/sh

if [ -z "$RASC_VERSION" ]; then
   echo 'Env var "RASC_VERSION" is not set.'
   exit 1
fi

if [ $# -ne 4 ]; then
  echo "Please set [ServiceName] [Port] [In] [Out]."
  exit 2
fi

BASE_URL=https://alaginrc.nict.go.jp/rasc

BASEDIR=`dirname $0`
BASEDIR=`(cd "$BASEDIR"; pwd)`

wget http://chasen.org/~taku/software/TinySVM/src/TinySVM-0.09.tar.gz
tar zxvf TinySVM-0.09.tar.gz

wget https://alaginrc.nict.go.jp/rasc/resources/patch_rasc_TinySVM.diff
patch -p0 < patch_rasc_TinySVM.diff

cd TinySVM-0.09
./configure
make

cd tests
../src/svm_learn  train.svmdata model

SERVICE=$1
PORT=$2
INPUT=$3
OUTPUT=$4
PKG=rasc-core-${RASC_VERSION}
cd ${BASEDIR}/${PKG}

echo $BASEDIR

wget ${BASE_URL}/_downloads/${SERVICE}.xml -P WEB-INF/serviceimpl/

echo sed -i -e "s!___BASE_DIR___!${BASEDIR}/TinySVM-0.09!g" WEB-INF/serviceimpl/${SERVICE}.xml
sed -i -e "s!___BASE_DIR___!${BASEDIR}/TinySVM-0.09!g" WEB-INF/serviceimpl/${SERVICE}.xml
echo sed -i -e "s!___PATH_TO_MODEL_FILE___!${BASEDIR}/TinySVM-0.09/tests/model!g" WEB-INF/serviceimpl/${SERVICE}.xml
sed -i -e "s!___PATH_TO_MODEL_FILE___!${BASEDIR}/TinySVM-0.09/tests/model!g" WEB-INF/serviceimpl/${SERVICE}.xml

sh ./server.sh  "$SERVICE" "$PORT" start
sleep 5
javac -classpath ./lib/*: SampleClient.java

echo java -cp ./lib/*: SampleClient localhost "$PORT" "$INPUT" | grep "$OUTPUT"
java -cp ./lib/*: SampleClient localhost "$PORT" "$INPUT" | grep "$OUTPUT"

sh ./server.sh "${SERVICE}" "${PORT}" stop


