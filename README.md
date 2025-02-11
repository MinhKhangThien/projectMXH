
# Social Media Microservices Application

## Member
- **Vũ Đức Minh** - 21522348
- **Nguyễn Thiện** - 21521461
- **Nguyễn Nhật Khang** - 21522193
  
## Overview
This project is a **social media platform** built using a microservice architecture with **Spring Boot** and an **event-driven architecture** using **Kafka**. It allows users to create posts, upload media, interact with posts, and see a feed of interactions.

## Getting Started

This project demonstrates how to run a Spring Boot application together with several supporting services using Docker Compose. The services include:

- **Zipkin**: Distributed tracing for your microservices.
- **Zookeeper** and **Kafka Broker**: For messaging and event streaming.
- **PostgreSQL (`dormy-data`)**: Database service.
- **PgAdmin (`dormy-pgadmin`)**: A web-based GUI to manage PostgreSQL.
- *(Optional)* **Redis**: Uncomment and configure if needed.

The configuration uses an external `.env` file to store environment variables for the PostgreSQL service.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Installation and Setup](#installation-and-setup)
- [Running the Application](#running-the-application)
- [Accessing PgAdmin](#accessing-pgadmin)
- [Troubleshooting](#troubleshooting)
- [License](#license)

## Key Features
- **Post Service**: Users can create, edit, delete, and retrieve posts.
- **Media Service**: Allows uploading and managing media files.
- **Interaction Service**: Handles user interactions such as likes, comments, and shares on posts.
- **Feed Service**: Displays a real-time feed of posts and interactions.
- **Event-Driven Architecture**: Communication between microservices is handled via Kafka to ensure scalability and decoupled services.
- **Microservice Architecture**: Each service is developed and deployed independently.

## Tech Stack
- **Backend**: Java, Spring Boot, Spring Data JPA
- **Message Broker**: Apache Kafka
- **Database**: PostgreSQL
- **Containerization**: Docker
- **Deployment**: Kubernetes
- **CI/CD**: Jenkins
- **Cloud**: AWS

### Event-Driven Communication
All services communicate through Kafka topics:
- `post_topic`: Used by the Post service to broadcast post events.
- `media_topic`: Used by the Media service for media upload events.
- `interaction_topic`: Used by the Interaction service for like/comment/share events.

### Dependencies
- **Java 17**
- **Spring Boot 3**
- **Kafka**
- **Docker**
- **Kubernetes**
- **AWS SDK**

## Prerequisites

Before you begin, ensure you have the following installed:

- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)
- [Java 11 or higher](https://www.oracle.com/java/technologies/javase-downloads.html) (for building/running the Spring Boot app)
- [Maven](https://maven.apache.org/install.html) or [Gradle](https://gradle.org/install/) (if you are building the Spring Boot app locally)

## Project Structure

```
├── .env                  # Environment variables for PostgreSQL
├── docker-compose.yml    # Docker Compose configuration file
├── init.local.sql        # SQL script for initializing the PostgreSQL database
├── mika-data/            # Local directory for PostgreSQL data (as specified in .env)
├── src/                  # Your Spring Boot application source code
├── pom.xml (or build.gradle)
└── README.md             # This file
```

## Configuration

### .env File

Create a file named `.env` in the root of your project (if it doesn’t already exist) with the following content:

```dotenv
DB_PORT=5430
DB_USER=postgres
DB_PASSWORD=postgres
DB_DATA=/data/postgres
DB_LOCAL_DIR=./mika-data
```

These environment variables are used by the `dormy-data` service in the `docker-compose.yml` to configure PostgreSQL.

### docker-compose.yml

Your `docker-compose.yml` file defines several services. Key points include:

- **Zipkin**: Runs on port `9411`.
- **Zookeeper and Kafka Broker**: Provides the messaging infrastructure.
- **dormy-data (PostgreSQL)**:
  - Uses the `.env` file values to set up the database.
  - Maps the local directory `./mika-data` to the container’s data directory.
  - Executes `init.local.sql` on initialization.
- **dormy-pgadmin**:
  - Runs on port `5050` (accessible at `http://localhost:5050`).
  - Uses default credentials (`postgres@gmail.com` / `postgres`).

> **Note:** There is an optional Redis service commented out. If you need Redis, uncomment and configure it accordingly.

## Installation and Setup

1. **Clone the repository:**

   ```bash
   git clone https://your-repo-url.git
   cd your-repo-directory
   ```

2. **Create the `.env` file:**

   Ensure that the `.env` file exists in the project root with the content provided in the [Configuration](#configuration) section.

3. **Build your Spring Boot application:**

   If you prefer to run your Spring Boot application outside Docker, build it using Maven or Gradle:

   ```bash
   # Using Maven
   mvn clean package
   ```

   This will generate an executable JAR file in the `target/` (or `build/libs/`) directory.

## Running the Application

There are two ways to run the entire stack:

### Option 1: Run Services via Docker Compose

1. **Start Docker Compose:**

   From the root directory (where your `docker-compose.yml` is located), run:

   ```bash
   docker-compose up -d
   ```

   This command will start all services in detached mode. You can check the logs using:

   ```bash
   docker-compose logs -f
   ```

2. **Deploy your Spring Boot Application:**

   You can run your Spring Boot application locally (e.g., from your IDE or via the command line) if it connects to the services defined in Docker Compose. Make sure your Spring Boot configuration (usually in `application.properties` or `application.yml`) points to the database host as `localhost` and the port as defined in `.env` (i.e., `5430`).

   Alternatively, you could containerize your Spring Boot app and add it as a service in `docker-compose.yml`.

### Option 2: Run the Spring Boot Application in a Docker Container

If you containerize your Spring Boot application, add a new service in `docker-compose.yml` and build the Docker image. Then, run:

```bash
docker-compose up -d
```

Ensure that the Spring Boot service depends on the necessary services (e.g., `dormy-data`) so that the startup order is maintained.

## Accessing PgAdmin

Once Docker Compose is running:

- Open your browser and navigate to [http://localhost:5050](http://localhost:5050).
- Log in using the following credentials:
  - **Email:** `postgres@gmail.com`
  - **Password:** `postgres`
- After login, add a new server connection in PgAdmin to connect to your PostgreSQL database. Use the following details:
  - **Host:** `dormy-data` (or `localhost` if you connect from the host machine)
  - **Port:** `5430`
  - **Maintenance Database:** `postgres`
  - **Username:** `postgres`
  - **Password:** `postgres`

## Troubleshooting

- **Port Conflicts:**  
  Ensure that the ports defined in the `.env` file and `docker-compose.yml` (e.g., `5430` for PostgreSQL, `5050` for PgAdmin) are not used by other applications.

- **Container Health:**  
  Use `docker ps` and `docker-compose logs <service_name>` to check container statuses and logs if a service fails to start.

- **Database Initialization:**  
  The `init.local.sql` file will run on first startup of the `dormy-data` container. If you modify this script, you may need to remove the volume (`mika-data` folder) to reinitialize the database.
