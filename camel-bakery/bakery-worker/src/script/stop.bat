@echo off
wmic process where (commandline like "%%worker.jar%%" and not name="wmic.exe") delete