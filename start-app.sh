#!/bin/bash

echo "Starting Banking System..."

docker compose up -d

echo "-----------------------------------"
echo "Banking System Started Successfully"
echo "Frontend Dashboard   : http://localhost:8081"
echo "Spring Boot REST API : http://localhost:8080"
echo "MySQL Database Port  : 3306"
echo "Redis Cache Port     : 6379"
echo "-----------------------------------"