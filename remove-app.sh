#!/bin/bash

echo "Removing Banking System..."

docker compose down

docker volume rm bank_data

docker network rm banking_network

docker image rm docker_app-app || true

echo "Cleanup complete."