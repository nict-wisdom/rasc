EmbeddedJettyPath=../lib/embeddedserver.jar
JettyTempPath=/tmp
ServiceSettingsPath=./settings.json

$JAVA_HOME/bin/java -Xms1G -Xmx1G -jar $EmbeddedJettyPath -json $ServiceSettingsPath -t $JettyTempPath > $JettyTempPath/rasc_service.log 2>&1 &
