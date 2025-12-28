# Customer Service

## Overview
Customer management and profile service.

## Responsibilities
- Customer registration and authentication
- Profile management
- KYC (Know Your Customer) verification

## Technology Stack
- Java 17+
- Spring Boot
- PostgreSQL

## API Endpoints
*Coming soon...*

## Configuration Profiles

This service uses Spring profiles for environment-specific configuration:

- **dev**: Local development (default)
- **test**: Integration tests with Testcontainers
- **prod**: Production environment
- **local**: Personal developer overrides (not tracked in git)

### Local Developer Configuration

`application-local.yml` is **intentionally ignored** by git and used for personal developer overrides.

To create your own local configuration:
```bash
# Create your personal config
cp src/main/resources/application-dev.yml src/main/resources/application-local.yml

# Customize as needed (port, database, logging, etc.)
nano src/main/resources/application-local.yml

# Run with local profile
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Example `application-local.yml`:**
```yaml
server:
  port: 9091  # Use different port

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/my_local_db
    username: myuser
    password: mypass

logging:
  level:
    com.daghanemre.fintech.customer: TRACE
```

> **Note**: Never commit `application-local.yml` to version control. It's for your personal environment only.
