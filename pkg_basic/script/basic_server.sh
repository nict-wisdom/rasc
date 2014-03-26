#!/bin/sh                                                                                                                                                                                                                             

BASEDIR=`dirname $0`
BASEDIR=`(cd "$BASEDIR"; pwd)`
nowTime=`date '+%Y%m%d-%H%M%S'`

EmbeddedJettyPath=$BASEDIR/../lib/embeddedserver.jar
ServiceSettingsPath=$BASEDIR/settings.json

JettyTempPath=$BASEDIR/../tmp
if [ ! -d "$JettyTempPath" ]; then
    mkdir "$JettyTempPath"
fi

LOGDIR=$BASEDIR/logs
if [ ! -d "$LOGDIR" ]; then
    mkdir "$LOGDIR"
fi

PID=$BASEDIR/pid_rasc_server

start() {
    if [ -e "$PID" ];
    then
	echo "ERROR: RaSC server is already running."
    else
	echo "RaSC server started."
	LOGFILE=$LOGDIR/rasc-server.$nowTime.log
	LOGLINK=$LOGDIR/rasc-server.log
	"$JAVA_HOME/bin/java" -Xms1G -Xmx1G -jar "$EmbeddedJettyPath" -json "$ServiceSettingsPath" -t "$JettyTempPath" > "$LOGFILE" 2>&1 &

#	java -Djava.util.logging.config.file=./logger.property -classpath ./lib/*: jp.go.nict.ial.servicecontainer.msgpackrpc.util.MsgPackRpcServerInitializeStarter $SERVICE $PORT > "$LOGFILE" 2>&1 &
	echo $! > "$PID"
	ln -sf "$LOGFILE" "$LOGLINK"
	sleep 1
    fi
}

stop(){
    if [ -e "$PID" ]; then
	kill `cat "$PID"`
	rm -f "$PID"
	echo "RaSC service has been stopped."
    else
	echo "RaSC service does not seem running: $PID not found."
    fi
}

case "$1" in
    start)
	start ;;
    stop)
	stop ;;
    restart)
	stop
	start ;;
    *)
	echo "Usage: $0 PORT {start | stop}"
	exit 2
esac

