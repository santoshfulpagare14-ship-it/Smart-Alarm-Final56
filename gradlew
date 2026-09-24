#!/usr/bin/env sh
# Minimal gradlew
set -e
GRADLE_VERSION=8.4
if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then
  echo "Downloading gradle wrapper..."
  mkdir -p gradle/wrapper
  curl -L -o gradle/wrapper/gradle-wrapper.jar https://github.com/gradle/gradle/raw/v${GRADLE_VERSION}.0/gradle/wrapper/gradle-wrapper.jar 2>/dev/null || wget -O gradle/wrapper/gradle-wrapper.jar https://github.com/gradle/gradle/raw/v${GRADLE_VERSION}.0/gradle/wrapper/gradle-wrapper.jar
fi
java -jar gradle/wrapper/gradle-wrapper.jar "$@"
