#!/bin/bash
DIR="${BASH_SOURCE%/*}"

java -Done-jar.silent=true -jar $DIR/oracle.soa.scanner.jar "$@"
