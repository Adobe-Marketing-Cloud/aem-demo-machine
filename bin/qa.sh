#!/bin/sh
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
date
pwd
rm -rf logs/*.log
pwd
./ant/bin/ant -Ddemo.build=communities_latest -Ddemo.communities.uber=latest -Ddemo.communities.enablement=true demo_communities > communities_latest.log
./ant/bin/ant -Ddemo.build=communities_61 -Ddemo.communities.uber=none -Ddemo.communities.enablement=false demo_communities > communities_61.log
./ant/bin/ant -Ddemo.build=communities_fp1 -Ddemo.communities.uber=aem61-fp1 -Ddemo.communities.enablement=true demo_communities > communities_fp1.log
./ant/bin/ant -Ddemo.build=standard -Ddemo.srp=JSRP demo > standard.log
./ant/bin/ant -Ddemo.build=kitchen -Ddemo.communities.uber=aem61-fp1 -Ddemo.communities.enablement=false demo_kitchensink > kitchensink.log
./ant/bin/ant -Ddemo.build=author -Ddemo.srp=JSRP -Ddemo.type=author demo > author.log
./ant/bin/ant -Ddemo.build=farm -Ddemo.srp=MSRP -Ddemo.type=farm -Ddemo.communities.enablement=false demo_communities > farm.log
./ant/bin/ant -Ddemo.build=aem60 -Ddemo.jar=aem60 demo > aem60.log
./ant/bin/ant -Ddemo.build=cq56 -Ddemo.license=cq5 -Ddemo.jar=cq56 demo > cq56.log
date
