#!/bin/bash
echo "********************************************************";
echo "*Stop Proxy";
echo "********************************************************";
kill `cat /tmp/proxy.pid`
rm -f /tmp/proxy.pid
echo "***************"
echo "done."
echo "***************"

