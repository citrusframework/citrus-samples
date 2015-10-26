#!/bin/sh
nohup java -DACTIVEMQ_PORT_61616_TCP_ADDR=localhost -DACTIVEMQ_PORT_61616_TCP_PORT=${activemq.server.port} -jar ${project.build.directory}/worker.jar "$@" > ${project.build.directory}/worker.out 2> ${project.build.directory}/worker.err < /dev/null &
