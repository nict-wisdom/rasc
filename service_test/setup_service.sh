#!/bin/sh                                                                                                                                                                                                                             

BASE_URL=https://alaginrc.nict.go.jp/rasc

BASEDIR=`dirname $0`
BASEDIR=`(cd "$BASEDIR"; pwd)`

if [ -z "$RASC_VERSION" ]; then
   echo 'Env var "RASC_VERSION" is not set.'
   exit 1
fi

if [ $# -ne 5 ]; then
  echo "Package type is missing."
  echo "Usage: $0 [ core | basic ]"
  exit 2
fi

SERVICE=$1
PORT=$2
CMD_DIR=$3
INPUT=$4
OUTPUT=$5
PKG=rasc-core-${RASC_VERSION}
cd ${BASEDIR}/${PKG}

wget ${BASE_URL}/_downloads/${SERVICE}.xml -P WEB-INF/serviceimpl/
echo sed -i -e "s!___BASE_DIR___!${CMD_DIR}!g" WEB-INF/serviceimpl/${SERVICE}.xml
sed -i -e "s!___BASE_DIR___!${CMD_DIR}!g" WEB-INF/serviceimpl/${SERVICE}.xml


sh ./server.sh  "$SERVICE" "$PORT" start
sleep 5
javac -classpath ./lib/*: SampleClient.java

echo java -cp ./lib/*: SampleClient localhost "$PORT" "$INPUT" | grep "$OUTPUT"
java -cp ./lib/*: SampleClient localhost "$PORT" "$INPUT" | grep "$OUTPUT"

sh ./server.sh "${SERVICE}" "${PORT}" stop

