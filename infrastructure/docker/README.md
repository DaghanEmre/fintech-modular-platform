# Docker Configuration

## Overview
Docker and Docker Compose configurations for local development, testing, and deployment.

## Prerequisites

### Docker Installation (Ubuntu/Linux Mint)

```bash
# Install Docker
sudo apt update
sudo apt install -y docker.io

# Add current user to docker group (avoids sudo for docker commands)
sudo usermod -aG docker $USER

# Apply group changes (or logout/login)
newgrp docker

# Verify installation
docker --version
docker ps
```

### Troubleshooting: Permission Denied

If you encounter `permission denied` when running `docker ps`:

```bash
# Temporary fix (resets on reboot)
sudo chmod 666 /var/run/docker.sock

# Permanent fix: Ensure user is in docker group
sudo usermod -aG docker $USER
# Then logout and login again
```

## Docker Images Used

| Image | Purpose | Used By |
|-------|---------|---------|
| `postgres:15-alpine` | PostgreSQL database | Integration tests, local development |
| `testcontainers/ryuk:0.5.1` | Container cleanup | Testcontainers (automatic) |

## Testcontainers

This project uses [Testcontainers](https://www.testcontainers.org/) for integration testing.

### How It Works

1. **Automatic Container Management**: Testcontainers starts PostgreSQL container before tests
2. **Dynamic Port Mapping**: Random ports avoid conflicts
3. **Cleanup**: Containers are automatically stopped after tests

### Configuration

Integration tests extend `AbstractIntegrationTest`:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
        new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
    }
}
```

### Running Integration Tests

```bash
# Ensure Docker is running
docker ps

# Run all integration tests
cd services/customer-service
mvn test -Dtest=*IT

# Run specific integration test
mvn test -Dtest=ActivateCustomerIT
```

## Docker Compose (Future)

Docker Compose configurations for multi-service development will be added here:

- `docker-compose.yml` - Base configuration
- `docker-compose.dev.yml` - Local development overrides
- `docker-compose.test.yml` - Testing environment

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `POSTGRES_HOST` | Database host | `localhost` |
| `POSTGRES_PORT` | Database port | `5432` |
| `POSTGRES_DB` | Database name | `customer_db` |
| `POSTGRES_USER` | Database user | `fintech` |
| `POSTGRES_PASSWORD` | Database password | (required) |

## Best Practices

1. **Never commit credentials** - Use environment variables or secrets management
2. **Use specific image tags** - Avoid `latest` tag for reproducibility
3. **Clean up resources** - Remove unused containers and images regularly:
   ```bash
   docker system prune -a
   ```

## Related Documentation

- [Customer Service README](../../services/customer-service/README.md)
- [ADR-0003: Application Layer Design](../../docs/adr/0003-application-layer-design.md)
