package com.daghanemre.fintech.customer.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests using Testcontainers.
 *
 * <p>
 * Provides:
 * <ul>
 * <li>PostgreSQL container setup</li>
 * <li>Spring Boot application context</li>
 * <li>Dynamic database configuration</li>
 * <li>Container lifecycle management</li>
 * </ul>
 *
 * <p>
 * <b>Usage:</b>
 * 
 * <pre>{@code
 * @DisplayName("Activate Customer Integration Test")
 * class ActivateCustomerIT extends AbstractIntegrationTest {
 *     // Tests here
 * }
 * }</pre>
 *
 * <p>
 * <b>Container Strategy:</b>
 * <ul>
 * <li>Single container shared across all tests (performance)</li>
 * <li>Database cleaned between tests (isolation)</li>
 * <li>Container reused within test class (JUnit 5 lifecycle)</li>
 * </ul>
 *
 * <p>
 * <b>Design Decisions:</b>
 * <ul>
 * <li>PostgreSQL 15 (matches production version)</li>
 * <li>Random port mapping (parallel test execution)</li>
 * <li>Test profile active (application-test.yml)</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    /**
     * PostgreSQL container shared across all integration tests.
     *
     * <p>
     * Container lifecycle:
     * <ul>
     * <li>Started once before first test</li>
     * <li>Reused across all test classes</li>
     * <li>Stopped after all tests complete</li>
     * </ul>
     *
     * <p>
     * Configuration:
     * <ul>
     * <li>Image: postgres:15-alpine</li>
     * <li>Database: testdb</li>
     * <li>Username: test</li>
     * <li>Password: test</li>
     * </ul>
     */
    @Container
    protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    /**
     * Configures Spring Boot datasource from Testcontainers.
     *
     * <p>
     * Dynamically injects database connection properties:
     * <ul>
     * <li>JDBC URL (with random port)</li>
     * <li>Username</li>
     * <li>Password</li>
     * </ul>
     *
     * <p>
     * This overrides properties in application-test.yml.
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
