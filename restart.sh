#!/bin/bash

./gradlew clean build -x test
docker-compose down
docker-compose up --build
