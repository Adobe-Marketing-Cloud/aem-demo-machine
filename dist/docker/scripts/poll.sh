#!/bin/bash

time=`date +%s`
if [ -f cookies${time}.txt ]; then
    time=`date +%s`
fi

function doLogin {
    echo "Logging in..."
    curl -c cookies${time}.txt -d "j_username=admin&j_password=admin&j_validate=true" http://localhost:8080/libs/granite/core/content/login.html/j_security_check
    echo "Login successful."
}

doLogin

echo "Starting page polling, press CTRL+C to abort"
while :
do
    timestamp=`date +%s`

    echo "[${timestamp}] Polling page..."
    response=$(curl -b cookies${time}.txt http://localhost:8080/content/we-retail/us/en.html 2>&1)

    if [[ $response == *"App V1"* ]]; then
        echo "[${timestamp}] Getting application version 1"
    elif [[ $response == *"App V2"* ]]; then
        echo "[${timestamp}] Getting application version 2"
    else
        echo "[${timestamp}] Service interrupted!"
        exit
    fi

    sleep 1
done