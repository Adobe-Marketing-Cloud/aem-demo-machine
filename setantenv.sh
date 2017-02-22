#!/bin/bash
# ----------------------------------------------------------------------------
# Copyright 2015 Adobe Systems Incorporated.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ----------------------------------------------------------------------------

OWN_NAME=setantenv.sh

if [ "$0" == "./$OWN_NAME" ]; then
	echo \* Please call as ". ./$OWN_NAME", not ./$OWN_NAME !!!---
	echo \* Also please DO NOT set back the executable attribute
	echo \* On this file. It was cleared on purpose.
	
	chmod -x ./$OWN_NAME
	exit
fi
PLATFORM_HOME=`pwd`
export -p PLATFORM_HOME
export -p ANT_OPTS="-Xmx200m -XX:MaxPermSize=128M"
export -p ANT_HOME=$PLATFORM_HOME/ant
chmod +x "$ANT_HOME/bin/ant"
export -p PATH=$ANT_HOME/bin:$PATH

