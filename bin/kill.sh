#!/bin/sh
# Kills one to many processed based on a string
export param="$1";
if [ -n "$param" ]
then
echo "Looking for running processes containg $param";
ids=$(ps -ef | grep "$param" | grep -v ant | grep -v grep | grep -v kill | awk '{print $2}');
if [ -z "$ids" ]
then
	echo "No process to kill";
else
	printf "Now killing these processes:\n$ids";
	kill -9 $(ps -ef | grep "$param" | grep -v grep | grep -v kill | awk '{print $2}');
fi
else
echo "No parameter"
fi
