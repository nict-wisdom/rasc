#!/bin/bash
echo "********************************************************";
echo "*Stop runallserver";
echo "********************************************************";
kill `cat /tmp/runallserver.pid`
rm -f /tmp/runallserver.pid
echo "***************"
echo "done."
echo "***************"

