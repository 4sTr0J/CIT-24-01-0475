#!/bin/bash

echo "Starting Banking System..."

docker compose up -d

echo "-----------------------------------"
echo "Banking System Started"
echo "Database Port : 3306"
echo "Application Container : banking_app"
echo "-----------------------------------"