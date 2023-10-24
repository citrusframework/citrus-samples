@echo off
start java -DREPORT_SERVER_PORT=${report.server.port} -jar ${project.build.directory}/worker.jar %* > ${project.build.directory}/worker.out 2> ${project.build.directory}/worker.err
