#!/bin/bash
docker run -d --name lod16 -v "$PWD":/project -p 8899:8080 -w /project maven:3.3-jdk-8 mvn assembly:assembly exec:java
