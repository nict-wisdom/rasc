start(){
 java -jar tam.jar > tam.log 2>&1 &
 echo $! > pid
}

stop(){
kill `cat ./pid`
rm -f ./pid
}

case "$1" in
  start)
    start ;;
  stop)
    stop ;;
  *)
    echo "Usage: $0 {start | stop}"
    exit 2
esac

