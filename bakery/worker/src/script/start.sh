#!/bin/sh
nohup java -DACTIVEMQ_PORT_61616_TCP_ADDR=${host.name} -DACTIVEMQ_PORT_61616_TCP_PORT=${activemq.server.port} -DREPORT_PORT_8080_TCP_ADDR=${host.name} -DREPORT_PORT_8080_TCP_PORT=${report.server.port} -jar ${project.build.directory}/worker.jar "$@" > ${project.build.directory}/worker.out 2> ${project.build.directory}/worker.err < /dev/null &
