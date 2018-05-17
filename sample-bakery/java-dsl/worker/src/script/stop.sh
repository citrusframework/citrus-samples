#!/bin/sh
ps ax | grep -i 'worker.jar' | grep -v grep | awk '{print $1}' | xargs kill -SIGTERM
