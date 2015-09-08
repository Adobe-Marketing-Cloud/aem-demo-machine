#!/bin/bash 
OWN_NAME=setantenv.sh

if [ "$0" == "./$OWN_NAME" ]; then
	echo * Please call as ". ./$OWN_NAME", not ./$OWN_NAME !!!---
	echo * Also please DO NOT set back the executable attribute
	echo * On this file. It was cleared on purpose.
	
	chmod -x ./$OWN_NAME
	exit
fi
PLATFORM_HOME=`pwd`
export -p PLATFORM_HOME
export -p ANT_OPTS="-Xmx200m -XX:MaxPermSize=128M"
export -p ANT_HOME=$PLATFORM_HOME/ant
chmod +x "$ANT_HOME/bin/ant"
export -p PATH=$ANT_HOME/bin:$PATH

