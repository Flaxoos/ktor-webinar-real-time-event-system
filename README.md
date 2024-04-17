# Ktor Webinar: Real-time Event System with Plugins

---

### Overview
This repository showcases the power of Ktor plugins to build a real-time event processing system.
The plugins are available in https://github.com/Flaxoos/extra-ktor-plugins

The system is comprised of:

- **Event Producers**: Multiple Ktor servers generate events and publish them to a Kafka cluster, configured using the Ktor Kafka plugin. Event production is orchestrated using the Ktor Task Scheduler plugin.
- **Event Consumer**: A Ktor server consumes events from Kafka (also using the Ktor Kafka plugin) and exposes a REST endpoint to retrieve the latest events. Rate limiting is implemented on this endpoint using the Ktor Rate Limiting plugin.
- **Client**: A Ktor client demonstrates consumption of the consumer's endpoint, leveraging the Ktor Circuit Breaker plugin to prevent excessive requests and handle potential failures gracefully.

### Features
- Plugin-Driven Architecture: Demonstrates how Ktor plugins streamline the implementation of common, non-functional concerns for microservices.
- Real-time Event Processing: Provides a working example of event streaming and asynchronous communication with Kafka.
- API Resilience: Incorporates rate limiting and circuit breaking to safeguard the system's reliability.

### Prerequisites
- Java (Version 17 or later)
- Gradle
- Docker
- Docker Compose

### Getting Started

- Start Infrastructure:

    ```shell
    docker-compose up -d
    ```
- Run the `Run system` run configuration
- Explore the [documentation](https://flaxoos.github.io/extra-ktor-plugins/), the various plugin configurations and source code
- Tweak the configurations as you see fit and see the effects on the system

### Webinar Recording

- coming soon