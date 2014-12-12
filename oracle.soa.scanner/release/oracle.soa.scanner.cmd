@echo off
set DIR=%~dp0

java -Done-jar.silent=true -jar %DIR%/oracle.soa.scanner.jar %*
