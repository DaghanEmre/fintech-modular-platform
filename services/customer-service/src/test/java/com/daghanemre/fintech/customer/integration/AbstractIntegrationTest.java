package com.daghanemre.fintech.customer.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for integration tests using Testcontainers (Singleton Pattern).
 *
 * <p>
 * Provides:
 * <ul>
 * <li>PostgreSQL container setup (Singleton)</li>
 * <li>Spring Boot application context</li>
 * <li>Dynamic database configuration</li>
 * <li>test profile activation</li>
 * </ul>
 *
 * <p>
 * <b>Singleton Container Pattern:</b>
 * The container is started once and shared across all test classes.
 * This prevents port mismatch issues between Hikari connection pool and
 * newly started containers during test suite execution.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    /**
     * Testcontainers singleton pattern.
     *
     * <p>
     * PostgreSQLContainer implements AutoCloseable, but container lifecycle
     * is managed by Testcontainers + Ryuk and cleaned up on JVM shutdown.
     * This is a known false-positive for static analysis tools.
     * </p>
     */
    @SuppressWarnings("resource")
    protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER;

    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
        POSTGRES_CONTAINER.start();
    }

    /**
     * Configures Spring Boot datasource from the singleton Testcontainer.
     *
     * @param registry Spring's dynamic property registry
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
    }
}
