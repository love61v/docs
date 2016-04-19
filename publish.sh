#!/bin/bash
#defined default
TOMCAT_HOME="/usr/local/apache-tomcat-7.0.59"
TOMCAT_PORT=8080

PROJECT="$1"
#param validate
if [ "$2" != "" ]; then
   TOMCAT_PORT=$2
fi

if [ "$3" != "" ]; then
   TOMCAT_HOME="$3"
fi

#shutdown tomcat
TOMCAT_PROCESS=$(ps -aef | grep ${TOMCAT_HOME}/conf | grep -v grep |awk '{print $2}')
echo now process is $TOMCAT_PROCESS

kill $TOMCAT_PROCESS
echo kill tomcat process[$TOMCAT_PROCESS]

#check tomcat process
tomcat_pid=`/usr/sbin/lsof -n -P -t -i :$TOMCAT_PORT`
echo "current :" $tomcat_pid

while [ -n "$tomcat_pid" ]
do
 sleep 5
 tomcat_pid=`/usr/sbin/lsof -n -P -t -i :$TOMCAT_PORT`
 echo "scan tomcat pid :" $tomcat_pid
done
 
#bak project
if [ -f ${TOMCAT_HOME}/webapps/${PROJECT}.war ]; then
	BAK_DIR=${TOMCAT_HOME}/bak/$PROJECT/`date +%Y%m%d%H%M%S`
	mkdir -p "$BAK_DIR"
	mv ${TOMCAT_HOME}/webapps/$PROJECT.war "$BAK_DIR"/"$PROJECT"_`date +%Y%m%d%H%M%S`.war
	echo "bak finished........"
fi

#remove previous ziped project
rm -rf ${TOMCAT_HOME}/webapps/${PROJECT} 

#deploy to webapps dir
mv ${TOMCAT_HOME}"/"${PROJECT}.war ${TOMCAT_HOME}/webapps/$PROJECT.war

#start tomcat
"$TOMCAT_HOME"/bin/startup.sh
#operate log
echo "tail -f ${TOMCAT_HOME}/logs/catalina.out   to see log info"
