#!/bin/bash

#common
LocalPath=`pwd`;
CurrentPath=`readlink -f $0`;
BasePath=`dirname $CurrentPath`;
Jetty_Home=$BasePath;
Lang=ja_JP.UTF-8;
Path=$Java_home/bin:$PATH;
EmbeddedJettyLocal=$BasePath/../jar/embeddedserver.jar;
EmbeddedJettyPath=`readlink -f $EmbeddedJettyLocal`;
JettyTempPath=/tmp
umask 002

#web app context
ProxyServerPid=/tmp/proxy.pid;

echo "********************************************************";
echo "*ProxyServer Loader";
echo "********************************************************";

export JETTY_HOME=$Jetty_Home;
export LANG=$Lang;
export PATH=$Path;

if [ -f $ProxyServerPid ];
then
  echo "Already started!";
  exit -1;
fi

java -version

#JavaOPT="-Xms56G -Xmx56G -XX:MaxPermSize=320m -XX:PermSize=128m";
#-verbose:gc -XX:+PrintClassHistogram

java $JavaOPT -jar $EmbeddedJettyPath  -t $JettyTempPath > $JettyTempPath/proxy.log 2>&1 &

echo $! > $ProxyServerPid

echo "********************************************************";
echo "*Proxy Started";
echo "* stop: use stop_proxy.sh ";
echo "********************************************************";


