#!/bin/bash
echo "********************************************************";
echo "*Stop runallworker";
echo "********************************************************";
kill `cat /ptmp/wisdom/runallworker.pid`
rm -f /ptmp/wisdom/runallworker.pid
echo "***************"
echo "done."
echo "***************"

