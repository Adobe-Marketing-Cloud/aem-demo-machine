#!/bin/sh
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
