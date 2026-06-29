#!/bin/bash

echo "Preparing Banking System..."

# Build the Java application image
docker compose build

# Create persistent volume
docker volume create bank_data

# Create network (if it doesn't exist)
docker network create banking_network || true

echo "Preparation complete."

chmod +x prepare-app.sh