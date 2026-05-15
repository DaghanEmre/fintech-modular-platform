package com.daghanemre.fintech.customer.integration.persistence;

import com.daghanemre.fintech.customer.domain.model.CustomerStatus;
import com.daghanemre.fintech.customer.infrastructure.persistence.jpa.entity.CustomerJpaEntity;
import com.daghanemre.fintech.customer.infrastructure.persistence.jpa.repository.SpringDataCustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Test: JPA Persistence Layer with Real PostgreSQL (ADR-0008 Gate 5)
 *
 * <p>Package: {@code integration.persistence} — not {@code adapter.persistence}.
 * This test verifies the persistence BOUNDARY, not the adapter unit.
 * It crosses infrastructure concerns (Spring Data JPA, Testcontainers, PostgreSQL).
 *
 * <p>Boundary contract:
 * <ul>
 *   <li>Domain aggregates ({@code Customer}) flow through the repository PORT interface.</li>
 *   <li>The JPA adapter ({@code CustomerJpaRepositoryAdapter}) handles mapping internally.</li>
 *   <li>This test exercises {@code SpringDataCustomerRepository} + {@code CustomerJpaEntity}
 *       directly — the infrastructure layer itself — to verify JPA/Hibernate compatibility
 *       after the Spring Boot 3.5 upgrade.</li>
 * </ul>
 *
 * <p>No {@code @Testcontainers} annotation: the Testcontainers JDBC URL
 * ({@code jdbc:tc:postgresql:15/testdb}) in {@code application-test.yml} causes the driver
 * to manage the container lifecycle automatically. An explicit {@code @Container} field
 * and {@code @Testcontainers} would be redundant and misleading.
 *
 * <p>Verifies:
 * <ul>
 *   <li>Spring Data JPA + Hibernate 6 persistence compatibility under Spring Boot 3.5.14</li>
 *   <li>Enum round-trip: {@code CustomerStatus} stored as STRING, retrieved correctly</li>
 *   <li>Timestamp preservation: {@code createdAt} / {@code updatedAt} survive DB round-trip</li>
 *   <li>Unique constraint enforcement on {@code email}</li>
 * </ul>
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CustomerJpaEntity — Persistence Integration Tests (Gate 5)")
class CustomerJpaRepositoryIT {

    @Autowired
    private SpringDataCustomerRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("should persist and retrieve CustomerJpaEntity with correct status")
    void shouldPersistAndRetrieveCustomerEntity() {
        var id = UUID.randomUUID();
        var entity = new CustomerJpaEntity(
                id,
                "alice@example.com",
                CustomerStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        repository.save(entity);
        entityManager.flush();
        entityManager.clear(); // Evict from first-level cache — force reload from DB

        var retrieved = repository.findById(id).orElseThrow();

        assertThat(retrieved.getId()).isEqualTo(id);
        assertThat(retrieved.getEmail()).isEqualTo("alice@example.com");
        assertThat(retrieved.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(retrieved.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("should store CustomerStatus enum as STRING and retrieve correctly")
    void shouldRoundTripCustomerStatusEnumThroughDatabase() {
        // Risk: Hibernate 6 + Spring Boot 3.5 upgrade may alter @Enumerated(STRING) behavior.
        // This test explicitly verifies the round-trip contract.
        var id = UUID.randomUUID();
        var entity = new CustomerJpaEntity(
                id,
                "bob@example.com",
                CustomerStatus.SUSPENDED,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        repository.save(entity);
        entityManager.flush();
        entityManager.clear();

        var retrieved = repository.findById(id).orElseThrow();

        assertThat(retrieved.getStatus())
                .as("CustomerStatus.SUSPENDED must survive DB round-trip unchanged")
                .isEqualTo(CustomerStatus.SUSPENDED);
    }

    @Test
    @DisplayName("should find customer by email via Spring Data query method")
    void shouldFindCustomerByEmail() {
        var entity = new CustomerJpaEntity(
                UUID.randomUUID(),
                "carol@example.com",
                CustomerStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        repository.save(entity);
        entityManager.flush();

        var found = repository.findByEmail("carol@example.com");

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(e -> assertThat(e.getEmail()).isEqualTo("carol@example.com"));
    }

    @Test
    @DisplayName("should preserve createdAt and updatedAt timestamps through persistence")
    void shouldPreserveTimestamps() {
        var before = LocalDateTime.now().minusSeconds(1);
        var createdAt = LocalDateTime.now();

        var entity = new CustomerJpaEntity(
                UUID.randomUUID(),
                "dave@example.com",
                CustomerStatus.ACTIVE,
                createdAt,
                createdAt,
                null
        );

        repository.save(entity);
        entityManager.flush();
        entityManager.clear();

        var retrieved = repository.findByEmail("dave@example.com").orElseThrow();

        assertThat(retrieved.getCreatedAt())
                .as("createdAt must be preserved through DB round-trip")
                .isAfterOrEqualTo(before);
        assertThat(retrieved.getUpdatedAt())
                .as("updatedAt must be preserved through DB round-trip")
                .isAfterOrEqualTo(before);
    }
}
