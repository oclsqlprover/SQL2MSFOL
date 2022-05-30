#!/bin/bash

# Installs JAR files to make them available to Maven

GROUP_ID=com.uni.se
	
mvn install:install-file \
    -DgroupId="$GROUP_ID" \
    -DartifactId=datamodel \
    -Dversion=1.0.0 \
    -Dpackaging=jar \
    -Dfile=packages/datamodel-v1.0.jar
