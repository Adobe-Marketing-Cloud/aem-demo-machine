#!/bin/sh
# ----------------------------------------------------------------------------
# Copyright 2017 Adobe Systems Incorporated.
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
# Kills one to many processed based on a string
# ----------------------------------------------------------------------------
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
	kill -9 $(ps -ef | grep "$param" | grep -v ant | grep -v grep | grep -v kill | awk '{print $2}');
fi
else
echo "No parameter"
fi
