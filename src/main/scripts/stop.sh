#!/bin/bash
TCD=/home/solrfusion/tomcat/
cd $TCD

bin/shutdown.sh
while true; do
	x=`ps auxw |grep "$TCD/"|grep -v grep|grep -v tomcat/stop.sh|grep -v vim`
	if [ -z "$x" ]; then echo "Tomcat exited"; exit 0; fi
	echo "Waiting for shutdown"
	sleep 1
done

