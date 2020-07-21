#!/bin/bash

SCRIPT_DIR=$(dirname $(readlink -f $BASH_SOURCE[0]))

java -Dlogback.configurationFile="$SCRIPT_DIR/../conf/logback.xml" -Ddk.kb.applicationConfig="$SCRIPT_DIR/../conf/doms-pidregistration.properties" -jar doms-pidregistration.jar "$@"

