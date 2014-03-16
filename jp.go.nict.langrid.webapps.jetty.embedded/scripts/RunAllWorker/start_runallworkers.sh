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
EmbeddedJettyLocal=$BasePath/../../jar/embeddedserver.jar;
EmbeddedJettyPath=`readlink -f $EmbeddedJettyLocal`;
JettyTempPath=/ptmp/wisdom;
umask 002
#rm $JettyTempPath/*.log

#web app context
RunAllWorkersPid=$JettyTempPath/runallworker.pid;

IkkyuWorkerWarLocale=../../../jp.go.nict.isp.webapps.ikkyu.worker/war/jp.go.nict.isp.webapps.ikkyu.worker.war;
IkkyuWorkerWarPath=`readlink -f $BasePath/$IkkyuWorkerWarLocale`;
IkkyuWorkerContextPath=/jp.go.nict.isp.webapps.ikkyu.worker;
IkkyuRcPath=`dirname $IkkyuWorkerWarPath`;
IkkyuRcFile=$IkkyuRcPath/ikkyu.rc;
SentenceWorkerWarLocale=../../../jp.go.nict.isp.webapps.sentencesearch.worker/war/jp.go.nict.isp.webapps.sentencesearch.worker.war;
SentenceWorkerWarPath=`readlink -f $BasePath/$SentenceWorkerWarLocale`;
SentenceWorkerContextPath=/jp.go.nict.isp.webapps.sentencesearch.worker;
SentenceWorkerData=/ptmp/wisdom/sentence/index2;
ContraWorkerWarLocale=../../../jp.go.nict.isp.webapps.contrasearch.worker/war/jp.go.nict.isp.webapps.contrasearch.worker.war;
ContraWorkerWarPath=`readlink -f $BasePath/$ContraWorkerWarLocale`;
ContraWorkerContextPath=/jp.go.nict.isp.webapps.contrasearch.worker;
ContraData=/tmp/notfound;
DefinitionWorkerWarLocale=../../../jp.go.nict.isp.webapps.definitionsearch.worker/war/jp.go.nict.isp.webapps.definitionsearch.worker.war;
DefinitionWorkerWarPath=`readlink -f $BasePath/$DefinitionWorkerWarLocale`;
DefinitionWorkerContextPath=/jp.go.nict.isp.webapps.definitionsearch.worker;
DefinitionData=/ptmp/wisdom/definition/index;
DependencyWorkerWarLocale=../../../jp.go.nict.isp.webapps.dependencysearch.worker/war/jp.go.nict.isp.webapps.dependencysearch.worker.war;
DependencyWorkerWarPath=`readlink -f $BasePath/$DependencyWorkerWarLocale`;
DependencyWorkerContextPath=/jp.go.nict.isp.webapps.dependencysearch.worker;
DependencyData=/ptmp/wisdom/dependency/index3;
PageRankWorkerWarLocale=../../../jp.go.nict.isp.webapps.pageranksearch.worker/war/jp.go.nict.isp.webapps.pageranksearch.worker.war;
PageRankWorkerWarPath=`readlink -f $BasePath/$PageRankWorkerWarLocale`;
PageRankWorkerContextPath=/jp.go.nict.isp.webapps.pageranksearch.worker;
PageRankData=/tmp/notfound;
SentimentWorkerWarLocale=../../../jp.go.nict.isp.webapps.sentimentsearch.worker/war/jp.go.nict.isp.webapps.sentimentsearch.worker.war;
SentimentWorkerWarPath=`readlink -f $BasePath/$SentimentWorkerWarLocale`;
SentimentWorkerContextPath=/jp.go.nict.isp.webapps.sentimentsearch.worker;
SentimentData=/ptmp/wisdom/sentiment/index3;
SenderWorkerWarLocale=../../../jp.go.nict.isp.webapps.sendersearch.worker/war/jp.go.nict.isp.webapps.sendersearch.worker.war;
SenderWorkerWarPath=`readlink -f $BasePath/$SenderWorkerWarLocale`;
SenderWorkerContextPath=/jp.go.nict.isp.webapps.sendersearch.worker;
SenderData=/ptmp/wisdom/sender/index2;


echo "********************************************************";
echo "*RunAllWorkers Loader";
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
echo IkkyuRcFile : $IkkyuRcFile
echo IkkyuWorkerWarPath : $IkkyuWorkerWarPath
echo IkkyuWorkerContextPath : $IkkyuWorkerContextPath
echo SentenceWorkerWarPath : $SentenceWorkerWarPath
echo SentenceWorkerContextPath : $SentenceWorkerContextPath
echo SentenceWorkerData : $SentenceWorkerData
echo ContraWorkerWarPath : $ContraWorkerWarPath
echo ContraWorkerContextPath : $ContraWorkerContextPath
echo DefinitionWorkerWarPath : $DefinitionWorkerWarPath
echo DefinitionWorkerContextPath : $DefinitionWorkerContextPath
echo DependencyWorkerWarPath : $DependencyWorkerWarPath
echo DependencyWorkerContextPath : $DependencyWorkerContextPath
echo PageRankWorkerWarPath : $PageRankWorkerWarPath
echo PageRankWorkerContextPath : $PageRankWorkerContextPath
echo SentimentWorkerWarPath : $SentimentWorkerWarPath
echo SentimentWorkerContextPath : $SentimentWorkerContextPath
echo SenderWorkerWarPath : $SenderWorkerWarPath
echo SenderWorkerContextPath : $SenderWorkerContextPath
echo "***************"

export JETTY_HOME=$Jetty_Home;
export JAVA_HOME=$Java_home;
export CLASSPATH=$ClassPath;
export LANG=$Lang;
export PATH=$Path;

export IKKYU_RCFILE=$IkkyuRcFile;
export SENTENCE_DATA=$SentenceWorkerData;
export CONTRA_DATA=$ContraData;
export DEFINITION_DATA=$DefinitionData;
export DEPENDENCY_DATA=$DependencyData;
export PAGERANK_DATA=$PageRankData;
export SENTIMENT_DATA=$SentimentData;
export SENDER_DATA=$SenderData;

if [ -f $RunAllWorkersPid ];
then
  echo "Already started!";
  exit -1;
fi

java -version

#$JAVA_HOME/bin/java -verbose:gc -XX:+PrintClassHistogram -Xms55G -Xmx55G -jar start.jar > /tmp/runallworker.log 2>&1 &
$JAVA_HOME/bin/java -Xms56G -Xmx56G -XX:MaxPermSize=320m -XX:PermSize=128m -jar $EmbeddedJettyPath -cd $IkkyuWorkerContextPath:$IkkyuWorkerWarPath  -cd $SentenceWorkerContextPath:$SentenceWorkerWarPath -cd $ContraWorkerContextPath:$ContraWorkerWarPath -cd $DefinitionWorkerContextPath:$DefinitionWorkerWarPath -cd $DependencyWorkerContextPath:$DependencyWorkerWarPath -cd $PageRankWorkerContextPath:$PageRankWorkerWarPath -cd $SentimentWorkerContextPath:$SentimentWorkerWarPath -cd $SenderWorkerContextPath:$SenderWorkerWarPath -t $JettyTempPath > $JettyTempPath/runallworker.log 2>&1 &

echo $! > $RunAllWorkersPid

#$JAVA_HOME/bin/java -jar $BasePath/ikkyuworkerquickstart.jar

echo "********************************************************";
echo "*RunAllWorkers Started";
echo "* stop: use stop_runallworker.sh ";
echo "********************************************************";


