#!/bin/sh
nohup java -DACTIVEMQ_BROKER_PORT=${activemq.server.port} -DREPORT_SERVER_PORT=${report.server.port} -jar ${project.build.directory}/worker.jar "$@" > ${project.build.directory}/worker.out 2> ${project.build.directory}/worker.err < /dev/null &
