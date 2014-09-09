#!/bin/bash

#common
LocalPath=`pwd`;
CurrentPath=`readlink -f $0`;
BasePath=`dirname $CurrentPath`;
Lang=ja_JP.UTF-8;
Path=$Java_home/bin:$PATH;
EmbeddedJettyLocal=$BasePath/../../jar/embeddedserver.jar;
EmbeddedJettyPath=`readlink -f $EmbeddedJettyLocal`;
JettyTempPath=/tmp
#rm $JettyTempPath/*.log

#web app context
RunAllWorkersPid=$JettyTempPath/runallworker.pid;

echo "********************************************************";
echo "*RunAllWorkers Loader";
echo "********************************************************";
export JETTY_HOME=$Jetty_Home;
export LANG=$Lang;
export PATH=$Path;

if [ -f $RunAllWorkersPid ];
then
  echo "Already started!";
  exit -1;
fi

java -version

#JavaOPT="-Xms56G -Xmx56G -XX:MaxPermSize=320m -XX:PermSize=128m";
#-verbose:gc -XX:+PrintClassHistogram

java $JavaOPT -jar $EmbeddedJettyPath -json ../server.json -t $JettyTempPath > $JettyTempPath/runallworker.log 2>&1 &

echo $! > $RunAllWorkersPid

echo "********************************************************";
echo "*RunAllWorkers Started";
echo "* stop: use stop_runallworker.sh ";
echo "********************************************************";
