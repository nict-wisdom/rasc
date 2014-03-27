#!/bin/sh                                                                                                                                                                                                                             

BASE_URL=https://alaginrc.nict.go.jp/rasc

BASEDIR=`dirname $0`
BASEDIR=`(cd "$BASEDIR"; pwd)`

cd ${BASEDIR}

if [ -z "$RASC_VERSION" ]; then
   echo 'Env var "RASC_VERSION" is not set.'
   exit 1
fi

if [ $# -ne 1 ]; then
  echo "Package type is missing."
  echo "Usage: $0 [ core | basic ]"
  exit 2
fi

PKG_TYPE=$1
PKG=rasc-${PKG_TYPE}-${RASC_VERSION}
PKG_FILE=${PKG}.zip

wget ${BASE_URL}/resources/${PKG_FILE}
unzip ${PKG_FILE}

