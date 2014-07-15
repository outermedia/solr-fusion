#!/bin/bash
cd /home/solrfusion/tomcat

CATALINA_OPTS=-Dtomcat.home=`pwd`
export CATALINA_OPTS
bin/startup.sh
echo "Press Ctrl-c to exit the log output" 
tail --retry -f logs/catalina.out
