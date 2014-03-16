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

#web app context
RunAllServerPid=/tmp/runallserver.pid;

IkkyuServerWarLocale=../../../jp.go.nict.isp.webapps.ikkyu.server/war/jp.go.nict.isp.webapps.ikkyu.server.war;
IkkyuServerWarPath=`readlink -f $BasePath/$IkkyuServerWarLocale`;
IkkyuServerContextPath=/jp.go.nict.isp.webapps.ikkyu.server;
SentenceServerWarLocale=../../../jp.go.nict.isp.webapps.sentencesearch.server/war//jp.go.nict.isp.webapps.sentencesearch.server.war;
SentenceServerWarPath=`readlink -f $BasePath/$SentenceServerWarLocale`;
SentenceServerContextPath=/jp.go.nict.isp.webapps.sentencesearch.server;
ContraServerWarLocale=../../../jp.go.nict.isp.webapps.contrasearch.server/war/jp.go.nict.isp.webapps.contrasearch.server.war;
ContraServerWarPath=`readlink -f $BasePath/$ContraServerWarLocale`;
ContraServerContextPath=/jp.go.nict.isp.webapps.contrasearch.server;
DefinitionServerWarLocale=../../../jp.go.nict.isp.webapps.definitionsearch.server/war/jp.go.nict.isp.webapps.definitionsearch.server.war;
DefinitionServerWarPath=`readlink -f $BasePath/$DefinitionServerWarLocale`;
DefinitionServerContextPath=/jp.go.nict.isp.webapps.definitionsearch.server;
DependencyServerWarLocale=../../../jp.go.nict.isp.webapps.dependencysearch.server/war/jp.go.nict.isp.webapps.dependencysearch.server.war;
DependencyServerWarPath=`readlink -f $BasePath/$DependencyServerWarLocale`;
DependencyServerContextPath=/jp.go.nict.isp.webapps.dependencysearch.server;
PageRankServerWarLocale=../../../jp.go.nict.isp.webapps.pageranksearch.server/war/jp.go.nict.isp.webapps.pageranksearch.server.war;
PageRankServerWarPath=`readlink -f $BasePath/$PageRankServerWarLocale`;
PageRankServerContextPath=/jp.go.nict.isp.webapps.pageranksearch.server;
SentimentServerWarLocale=../../../jp.go.nict.isp.webapps.sentimentsearch.server/war/jp.go.nict.isp.webapps.sentimentsearch.server.war;
SentimentServerWarPath=`readlink -f $BasePath/$SentimentServerWarLocale`;
SentimentServerContextPath=/jp.go.nict.isp.webapps.sentimentsearch.server;
SenderServerWarLocale=../../../jp.go.nict.isp.webapps.sendersearch.server/war/jp.go.nict.isp.webapps.sendersearch.server.war;
SenderServerWarPath=`readlink -f $BasePath/$SenderServerWarLocale`;
SenderServerContextPath=/jp.go.nict.isp.webapps.sendersearch.server;

FullTextServerWarLocale=../../../jp.go.nict.isp.webapps.fulltextsearch.server/war/jp.go.nict.isp.webapps.fulltextsearch.server.war;
FullTextServerWarPath=`readlink -f $BasePath/$FullTextServerWarLocale`;
FullTextServerContextPath=/jp.go.nict.isp.webapps.fulltextsearch.server;

MetaIkkyuServerWarLocale=../../../jp.go.nict.isp.webapps.metaikkyu/war/jp.go.nict.isp.webapps.metaikkyu.war;
MetaIkkyuServerWarPath=`readlink -f $BasePath/$MetaIkkyuServerWarLocale`;
MetaIkkyuServerContextPath=/jp.go.nict.isp.webapps.metaikkyu;

WhyQAWarLocale=../../../jp.go.nict.isp.webapps.whyqa/war/jp.go.nict.isp.webapps.whyqa.war
WhyQAWarPath=`readlink -f $BasePath/$WhyQAWarLocale`;
WhyQAContextPath=/jp.go.nict.isp.webapps.whyqa;

HypoQAWarLocale=../../../jp.go.nict.isp.webapps.ikkyudou/war/jp.go.nict.isp.webapps.ikkyudou.war
HypoQAWarPath=`readlink -f $BasePath/$HypoQAWarLocale`;
HypoQAContextPath=/jp.go.nict.isp.webapps.ikkyudou;



echo "********************************************************";
echo "*RunAllServer Loader";
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
echo IkkyuServerWarPath : $IkkyuServerWarPath
echo IkkyuServerContextPath : $IkkyuServerContextPath
echo SentenceServerWarPath : $SentenceServerWarPath
echo SentenceServerContextPath : $SentenceServerContextPath
echo ContraServerWarPath : $ContraServerWarPath
echo ContraServerContextPath : $ContraServerContextPath
echo DefinitionServerWarPath : $DefinitionServerWarPath
echo DefinitionServerContextPath : $DefinitionServerContextPath
echo DependencyServerWarPath : $DependencyServerWarPath
echo DependencyServerContextPath : $DependencyServerContextPath
echo PageRankServerWarPath : $PageRankServerWarPath
echo PageRankServerContextPath : $PageRankServerContextPath
echo SentimentServerWarPath : $SentimentServerWarPath
echo SentimentServerContextPath : $SentimentServerContextPath
echo SenderServerWarPath : $SenderServerWarPath
echo SenderServerContextPath : $SenderServerContextPath
echo FullTextServerWarPath : $FullTextServerWarPath
echo FullTextServerContextPath : $FullTextServerContextPath
echo MetaIkkyuServerWarPath : $MetaIkkyuServerWarPath
echo MetaIkkyuServerContextPath : $MetaIkkyuServerContextPath

echo "***************"

export JETTY_HOME=$Jetty_Home;
export JAVA_HOME=$Java_home;
export CLASSPATH=$ClassPath;
export LANG=$Lang;
export PATH=$Path;
export WHYQA_SHELL="/home/mori/repos/work/All/run.sh ";

if [ -f $RunAllServerPid ];
then
  echo "Already started!";
  exit -1;
fi

java -version

#$JAVA_HOME/bin/java -verbose:gc -XX:+PrintClassHistogram -Xms55G -Xmx55G -jar start.jar > /tmp/runallserver.log 2>&1 &
$JAVA_HOME/bin/java -XX:MaxPermSize=320m -XX:PermSize=128m -jar $EmbeddedJettyPath -cd $IkkyuServerContextPath:$IkkyuServerWarPath  -cd $SentenceServerContextPath:$SentenceServerWarPath -cd $ContraServerContextPath:$ContraServerWarPath -cd $DefinitionServerContextPath:$DefinitionServerWarPath -cd $DependencyServerContextPath:$DependencyServerWarPath -cd $PageRankServerContextPath:$PageRankServerWarPath -cd $SentimentServerContextPath:$SentimentServerWarPath -cd $SenderServerContextPath:$SenderServerWarPath -cd $FullTextServerContextPath:$FullTextServerWarPath -cd $MetaIkkyuServerContextPath:$MetaIkkyuServerWarPath -cd $WhyQAContextPath:$WhyQAWarPath -cd $HypoQAContextPath:$HypoQAWarPath  -t $JettyTempPath > $JettyTempPath/runallserver.log 2>&1 &

echo $! > $RunAllServerPid

echo "********************************************************";
echo "*RunAllServer Started";
echo "* stop: use stop_runallserver.sh ";
echo "********************************************************";


