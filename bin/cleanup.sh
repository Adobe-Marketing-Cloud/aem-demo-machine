#!/bin/sh
date
cd /opt/demomachine
rm -rf nohup.out
nohup /opt/demomachine/ant/bin/ant uninstall &
date
