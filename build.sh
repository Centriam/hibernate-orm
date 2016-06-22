#!/bin/bash

cd $(dirname $0)

# Encrypted ext4 volumes don't play well with paths over 255 characters,
# and this Gradle project generates some very long paths.
# Gradle lets you specify a directory to either build it or host it in
# (don't look at me, I don't know how Gradle works!), so we're going to
# use a directory with a shorter path than /home/username/.somedirectory
export GRADLE_USER_HOME=/opt/gradle_home/
mkdir -P /opt/gradle_home
rm -rf /opt/gradle_home/*

# Have to build with Java 7 for some reason
export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64

./gradlew clean build # --stacktrace --debug
