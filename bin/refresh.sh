#!/bin/sh
date
cd /opt/demomachine
rm -rf logs/*.log
rm -rf nohup.out
nohup /opt/demomachine/ant/bin/ant demo_communities &
date
