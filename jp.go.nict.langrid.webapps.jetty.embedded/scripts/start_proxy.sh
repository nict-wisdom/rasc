#!/bin/bash

#common
LocalPath=`pwd`;
UseJdk=jdk1.7.0_05;
#UseJdk=jdk1.6.0_27;
JdkLocale=/opt/JAVA/;
CurrentPath=`readlink -f $0`;
BasePath=`dirname $CurrentPath`;
JdkPath=`readlink -f $JdkLocale`;
JdkPath=$JdkPath/$UseJdk;
Jetty_Home=$BasePath;
Java_home=$JdkPath;
ClassPath=.:$Java_home/lib/tools.jar:$Jave_home/lib/dt.jar:;
Lang=ja_JP.UTF-8;
Path=$Java_home/bin:$PATH;
EmbeddedJettyLocal=$BasePath/../jar/embeddedserver.jar;
EmbeddedJettyPath=`readlink -f $EmbeddedJettyLocal`;
JettyTempPath=/ptmp/wisdom;
umask 002

#web app context
ProxyServerPid=$JettyTempPath/proxy.pid;

echo "********************************************************";
echo "*ProxyServer Loader";
echo "********************************************************";
echo "**environment**";
echo HOST        : $HOSTNAME;
echo Jdk         : $JdkPath;
echo CurrentPath : $CurrentPath;
echo BasePath    : $BasePath;
echo Jetty_Home  : $Jetty_Home;
echo Java_home   : $Java_home;
echo ClassPath   : $ClassPath;
echo Lang        : $Lang;
echo Path        : $Path
echo "***************"
echo "** web context **";
echo "***************"

export JETTY_HOME=$Jetty_Home;
export JAVA_HOME=$Java_home;
export CLASSPATH=$ClassPath;
export LANG=$Lang;
export PATH=$Path;

if [ -f $ProxyServerPid ];
then
  echo "Already started!";
  exit -1;
fi

java -version

$JAVA_HOME/bin/java -XX:MaxPermSize=320m -XX:PermSize=128m -jar $EmbeddedJettyPath  -t $JettyTempPath > $JettyTempPath/proxy.log 2>&1 &

echo $! > $ProxyServerPid

echo "********************************************************";
echo "*Proxy Started";
echo "* stop: use stop_proxy.sh ";
echo "********************************************************";


