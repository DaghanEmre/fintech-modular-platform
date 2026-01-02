package com.daghanemre.fintech.customer.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Domain unit tests for CustomerId Value Object.
 *
 * Tests verify:
 * - Generation (UUID v4) and uniqueness
 * - Factory methods (from String, of UUID)
 * - Validation rules (null checks, UUID format)
 * - Value object semantics (equality, consistency)
 * - Round-trip serialization
 *
 * No infrastructure dependencies (pure domain test).
 */
@DisplayName("CustomerId Value Object")
class CustomerIdTest {

    @Nested
    @DisplayName("Valid CustomerId creation")
    class ValidCustomerIdCreation {

        @Test
        @DisplayName("generate - should create non-null CustomerId with valid UUID")
        void generate_ShouldCreateValidId() {
            CustomerId customerId = CustomerId.generate();

            assertNotNull(customerId);
            assertNotNull(customerId.value());
        }

        /**
         * Architectural guardrail test for UUID version.
         *
         * This test intentionally verifies UUID v4 usage as defined in ADR-0002.
         *
         * If this test fails:
         * - Check ADR-0002 for UUID generation strategy changes
         * - UUID version may have been upgraded (e.g., v7 migration)
         * - Update ADR and this test together
         *
         * This is NOT a core domain behavior test, but an architectural decision
         * verification.
         */
        @Test
        @DisplayName("generate - should use UUID v4 as defined in ADR-0002 (architectural guardrail)")
        void generate_ShouldUseUuidV4_AsPerAdr0002() {
            CustomerId customerId = CustomerId.generate();

            assertEquals(4, customerId.value().version(),
                    "UUID version is part of an explicit architectural decision (ADR-0002). " +
                            "If this fails, review ADR-0002 for potential UUID strategy changes.");
        }

        @Test
        @DisplayName("generate - should create unique IDs consistently")
        void generate_ShouldCreateUniqueIdsConsistently() {
            Set<CustomerId> ids = new HashSet<>();

            // Generate 1000 IDs and verify no collisions
            for (int i = 0; i < 1000; i++) {
                CustomerId id = CustomerId.generate();
                assertTrue(ids.add(id),
                        "UUID collision detected at iteration " + i);
            }

            assertEquals(1000, ids.size());
        }

        @Test
        @DisplayName("of - should create CustomerId from existing UUID")
        void of_ShouldCreateFromUuid() {
            UUID uuid = UUID.randomUUID();
            CustomerId customerId = CustomerId.of(uuid);

            assertEquals(uuid, customerId.value());
        }

        @Test
        @DisplayName("from - should create CustomerId from valid string")
        void from_ShouldCreateFromString() {
            String uuidStr = "550e8400-e29b-41d4-a716-446655440000";
            CustomerId customerId = CustomerId.from(uuidStr);

            assertEquals(UUID.fromString(uuidStr), customerId.value());
        }

        @Test
        @DisplayName("from - should normalize string by trimming")
        void from_ShouldNormalizeString() {
            String uuidStr = "  550e8400-e29b-41d4-a716-446655440000  ";
            CustomerId customerId = CustomerId.from(uuidStr);

            assertEquals(UUID.fromString(uuidStr.trim()), customerId.value());
        }

        @Test
        @DisplayName("from - should accept both lowercase and uppercase UUID")
        void from_ShouldAcceptBothCases() {
            String lowercase = "550e8400-e29b-41d4-a716-446655440000";
            String uppercase = "550E8400-E29B-41D4-A716-446655440000";

            CustomerId id1 = CustomerId.from(lowercase);
            CustomerId id2 = CustomerId.from(uppercase);

            assertEquals(id1, id2,
                    "UUIDs should be case-insensitive");
        }
    }

    @Nested
    @DisplayName("Invalid CustomerId creation")
    class InvalidCustomerIdCreation {

        @Test
        @DisplayName("constructor - should reject null value")
        void constructor_ShouldRejectNullValue() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> new CustomerId(null));

            assertEquals("CustomerId cannot be null", ex.getMessage());
        }

        @Test
        @DisplayName("of - should reject null UUID")
        void of_ShouldRejectNullUuid() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> CustomerId.of(null));

            assertEquals("CustomerId cannot be null", ex.getMessage());
        }

        @Test
        @DisplayName("from - should reject null string")
        void from_ShouldRejectNullString() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> CustomerId.from(null));

            assertTrue(ex.getMessage().contains("cannot be null or blank"));
        }

        @Test
        @DisplayName("from - should reject blank string")
        void from_ShouldRejectBlankString() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> CustomerId.from("   "));

            assertTrue(ex.getMessage().contains("cannot be null or blank"));
        }

        @Test
        @DisplayName("from - should reject empty string")
        void from_ShouldRejectEmptyString() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> CustomerId.from(""));

            assertTrue(ex.getMessage().contains("cannot be null or blank"));
        }

        @Test
        @DisplayName("from - should reject completely invalid format")
        void from_ShouldRejectInvalidFormat() {
            String invalidUuid = "not-a-uuid";

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> CustomerId.from(invalidUuid));

            assertTrue(ex.getMessage().contains("Invalid CustomerId format"));
        }

        @Test
        @DisplayName("from - should reject malformed UUID (wrong segment lengths)")
        void from_ShouldRejectMalformedUuid() {
            String malformedUuid = "550e8400-e29b-41d4-a716-4466554400"; // Too short

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> CustomerId.from(malformedUuid));

            assertTrue(ex.getMessage().contains("Invalid CustomerId format"));
        }

        @Test
        @DisplayName("from - should reject UUID with invalid characters")
        void from_ShouldRejectUuidWithInvalidCharacters() {
            String invalidUuid = "550e8400-e29b-41d4-a716-44665544000g"; // 'g' at end

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> CustomerId.from(invalidUuid));

            assertTrue(ex.getMessage().contains("Invalid CustomerId format"));
        }

        @Test
        @DisplayName("from - should reject UUID without hyphens")
        void from_ShouldRejectUuidWithoutHyphens() {
            String noHyphens = "550e8400e29b41d4a716446655440000";

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> CustomerId.from(noHyphens));

            assertTrue(ex.getMessage().contains("Invalid CustomerId format"));
        }
    }

    @Nested
    @DisplayName("Value object semantics")
    class ValueObjectSemantics {

        @Test
        @DisplayName("equals - should be equal when UUIDs match")
        void equals_ShouldBeEqualWhenUuidsMatch() {
            UUID uuid = UUID.randomUUID();
            CustomerId id1 = CustomerId.of(uuid);
            CustomerId id2 = CustomerId.of(uuid);

            assertEquals(id1, id2);
        }

        @Test
        @DisplayName("equals - should not be equal when UUIDs differ")
        void equals_ShouldNotBeEqualWhenUuidsDiffer() {
            CustomerId id1 = CustomerId.generate();
            CustomerId id2 = CustomerId.generate();

            assertNotEquals(id1, id2);
        }

        @Test
        @DisplayName("equals - should be reflexive")
        void equals_ShouldBeReflexive() {
            CustomerId id = CustomerId.generate();

            assertEquals(id, id);
        }

        @Test
        @DisplayName("equals - should be symmetric")
        void equals_ShouldBeSymmetric() {
            UUID uuid = UUID.randomUUID();
            CustomerId id1 = CustomerId.of(uuid);
            CustomerId id2 = CustomerId.of(uuid);

            assertEquals(id1, id2);
            assertEquals(id2, id1);
        }

        @Test
        @DisplayName("hashCode - should be consistent with equals")
        void hashCode_ShouldBeConsistentWithEquals() {
            UUID uuid = UUID.randomUUID();
            CustomerId id1 = CustomerId.of(uuid);
            CustomerId id2 = CustomerId.of(uuid);

            assertEquals(id1.hashCode(), id2.hashCode());
        }

        @Test
        @DisplayName("hashCode - should be consistent across calls")
        void hashCode_ShouldBeConsistentAcrossCalls() {
            CustomerId id = CustomerId.generate();
            int firstHashCode = id.hashCode();
            int secondHashCode = id.hashCode();

            assertEquals(firstHashCode, secondHashCode);
        }

        @Test
        @DisplayName("toString - should return UUID string representation")
        void toString_ShouldReturnUuidString() {
            UUID uuid = UUID.randomUUID();
            CustomerId customerId = CustomerId.of(uuid);

            assertEquals(uuid.toString(), customerId.toString());
        }

        @Test
        @DisplayName("from and toString - should be reversible (round-trip)")
        void fromAndToString_ShouldBeReversible() {
            String originalUuid = "550e8400-e29b-41d4-a716-446655440000";

            CustomerId customerId = CustomerId.from(originalUuid);
            String roundTrip = customerId.toString();

            assertEquals(originalUuid, roundTrip,
                    "Round-trip conversion should preserve original UUID");
        }

        @Test
        @DisplayName("value - should return internal UUID")
        void value_ShouldReturnInternalUuid() {
            UUID uuid = UUID.randomUUID();
            CustomerId customerId = CustomerId.of(uuid);

            assertEquals(uuid, customerId.value());
        }
    }
}
