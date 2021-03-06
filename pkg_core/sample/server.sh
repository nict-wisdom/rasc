#!/bin/sh                                                                                                                                                                                                                             

BASEDIR=`dirname $0`
BASEDIR=`(cd "$BASEDIR"; pwd)`
nowTime=`date '+%Y%m%d-%H%M%S'`

JAVA_OPTS="-Xmx1G -Djava.util.logging.config.file=./logging.properties -Dfile.encoding=UTF-8"

LOGDIR=$BASEDIR/logs
if [ ! -d "$LOGDIR" ]; then
    mkdir "$LOGDIR"
fi

SERVICE=$1
PORT=$2
NAME=${SERVICE}_${PORT}
PID=$BASEDIR/pid_${SERVICE}_${PORT}

start() {
    if [ -e "$PID" ];
    then
	echo "ERROR: "$SERVICE" is already running."
    else
	echo $SERVICE" started."
	LOGFILE=$LOGDIR/$NAME.$nowTime.log
	LOGLINK=$LOGDIR/$NAME.log
	java $JAVA_OPTS -classpath ./lib/*: jp.go.nict.ial.servicecontainer.handler.msgpackrpc.MsgPackRpcServer $SERVICE $PORT > "$LOGFILE" 2>&1 &
	echo $! > "$PID"
	ln -sf "$LOGFILE" "$LOGLINK"
	sleep 1
    fi
}

stop(){
    if [ -e "$PID" ]; then
	ps -p `cat "$PID"` >/dev/null 2>&1
	if [ $? -eq 0 ]; then
		kill `cat "$PID"`
	fi
	rm -f "$PID"
	echo "$SERVICE has been stopped."
    else
	echo "$SERVICE does not seem running: $PID not found."
    fi
}

case "$3" in
    start)
	start ;;
    stop)
	stop ;;
    restart)
	stop
	start ;;
    *)
	echo "Usage: $0 SERVICE_NAME PORT {start | stop}"
	exit 2
esac



