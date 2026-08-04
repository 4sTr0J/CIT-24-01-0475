# Dockerized Spring Boot Banking Application

This project is a Spring Boot banking application containerized with Docker Compose. It includes:

- A Spring Boot backend service
- A static frontend served by Nginx
- A MySQL database for persistence
- A Redis cache service

The application is designed to run locally using Docker and Docker Compose.

## Project Overview

The stack is defined in [docker-compose.yml](docker-compose.yml) and includes these services:

- `frontend` - serves the user interface on port `8081`
- `app` - Spring Boot backend API on port `8080`
- `database` - MySQL database on port `3306`
- `redis` - Redis cache on port `6379`

## Prerequisites

Before running the application, make sure you have the following installed:

- Docker
- Docker Compose
- Git

Optional:

- Java 17
- Maven

## Application Structure

- [Dockerfile](Dockerfile) - builds the Spring Boot application image
- [frontend/Dockerfile](frontend/Dockerfile) - builds the frontend Nginx image
- [docker-compose.yml](docker-compose.yml) - defines the application services
- [prepare-app.sh](prepare-app.sh) - prepares images and Docker resources
- [start-app.sh](start-app.sh) - starts the full application
- [stop-app.sh](stop-app.sh) - stops the containers
- [remove-app.sh](remove-app.sh) - removes containers, volumes, and related Docker resources
- [db/init.sql](db/init.sql) - database initialization script

## Prepare the Application

From the project root, run:

```bash
./prepare-app.sh
```

This script will:

1. Build all Docker images with `docker compose build`
2. Create the Docker volume `bank_data`
3. Create the Docker network `banking_network` 
if it does not already exist.

If you prefer to run the steps manually, use:

```bash
docker compose build
docker volume create bank_data
docker network create banking_network || true
```

## Start the Application

To start all services in detached mode:

```bash
./start-app.sh
```

The startup script runs:

```bash
docker compose up -d
```

Once started, the app will be available at:

- Frontend Dashboard: `http://localhost:8081`
- Spring Boot REST API: `http://localhost:8080`
- MySQL Database: `localhost:3306`
- Redis Cache: `localhost:6379`

## Stop the Application

To stop running containers while keeping the database data:

```bash
./stop-app.sh
```

This script runs:

```bash
docker compose stop
```

## Remove the Application

To remove the application containers and clean up Docker resources:

```bash
./remove-app.sh
```

This script performs:

```bash
docker compose down
docker volume rm bank_data redis_data || true
docker network rm banking_network
docker image rm docker_app-app || true
```

## Useful Docker Commands

Check running containers:

```bash
docker compose ps
```

View logs:

```bash
docker compose logs -f
```

Restart the application:

```bash
docker compose restart
```

## Notes

- The MySQL database uses a Docker named volume to preserve data between restarts.
- The Redis service uses its own Docker volume for persistent cache data.
- The Spring Boot backend connects to MySQL using the internal service name `database` and Redis using the internal service name `redis`.

## Summary

The recommended workflow is:

```bash
./prepare-app.sh
./start-app.sh
```

And when you are done:

```bash
./stop-app.sh
```

If you want a complete reset, run:

```bash
./remove-app.sh
```
