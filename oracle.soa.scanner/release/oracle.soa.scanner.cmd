@echo off
set DIR=%~dp0

java -Done-jar.verbose=false -Done-jar.silent=true -jar %DIR%/oracle.soa.scanner.jar %*
