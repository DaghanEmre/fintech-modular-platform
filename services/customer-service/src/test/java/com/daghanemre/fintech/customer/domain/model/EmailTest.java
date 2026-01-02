package com.daghanemre.fintech.customer.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Domain unit tests for Email Value Object.
 *
 * Tests verify:
 * - Validation rules (format, length)
 * - Normalization behavior (trim, lowercase)
 * - Value object semantics (equality, immutability)
 * - Edge cases and boundary conditions
 *
 * No infrastructure dependencies (pure domain test).
 */
@DisplayName("Email Value Object")
class EmailTest {

    @Nested
    @DisplayName("Valid email creation")
    class ValidEmailCreation {

        @Test
        @DisplayName("create - should accept standard email format")
        void create_ShouldAcceptStandardEmailFormat() {
            Email email = Email.of("user@example.com");

            assertNotNull(email);
            assertEquals("user@example.com", email.value());
        }

        @Test
        @DisplayName("create - should normalize by trimming and lowercasing")
        void create_ShouldNormalizeByTrimmingAndLowercasing() {
            Email email = Email.of("  USER.Name+Tag@Example.COM  ");

            assertEquals("user.name+tag@example.com", email.value());
        }

        @Test
        @DisplayName("create - should allow subdomain emails")
        void create_ShouldAllowSubdomainEmails() {
            Email email = Email.of("user@mail.example.com");

            assertEquals("user@mail.example.com", email.value());
        }

        @Test
        @DisplayName("create - should allow plus addressing")
        void create_ShouldAllowPlusAddressing() {
            Email email = Email.of("user+tag@example.com");

            assertEquals("user+tag@example.com", email.value());
        }

        @Test
        @DisplayName("create - should allow dots in local part")
        void create_ShouldAllowDotsInLocalPart() {
            Email email = Email.of("john.doe@example.com");

            assertEquals("john.doe@example.com", email.value());
        }

        @Test
        @DisplayName("create - should allow numbers and hyphens")
        void create_ShouldAllowNumbersAndHyphens() {
            Email email = Email.of("user-123@test-domain.com");

            assertEquals("user-123@test-domain.com", email.value());
        }
    }

    @Nested
    @DisplayName("Invalid email creation")
    class InvalidEmailCreation {

        @Test
        @DisplayName("create - should reject null email")
        void create_ShouldRejectNullEmail() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> Email.of(null));

            assertEquals("Email cannot be null or blank", ex.getMessage());
        }

        @Test
        @DisplayName("create - should reject blank email")
        void create_ShouldRejectBlankEmail() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> Email.of("   "));

            assertEquals("Email cannot be null or blank", ex.getMessage());
        }

        @Test
        @DisplayName("create - should reject email without @ symbol")
        void create_ShouldRejectEmailWithoutAtSymbol() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> Email.of("userexample.com"));

            assertTrue(ex.getMessage().contains("Invalid email format"));
        }

        @Test
        @DisplayName("create - should reject email without domain")
        void create_ShouldRejectEmailWithoutDomain() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> Email.of("user@"));

            assertTrue(ex.getMessage().contains("Invalid email format"));
        }

        @Test
        @DisplayName("create - should reject email without local part")
        void create_ShouldRejectEmailWithoutLocalPart() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> Email.of("@example.com"));

            assertTrue(ex.getMessage().contains("Invalid email format"));
        }

        @Test
        @DisplayName("create - should reject consecutive dots (pragmatic restriction)")
        void create_ShouldRejectConsecutiveDots() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> Email.of("user..name@example.com"));

            assertTrue(ex.getMessage().contains("Invalid email format"));
        }

        @Test
        @DisplayName("create - should reject email starting with dot")
        void create_ShouldRejectEmailStartingWithDot() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> Email.of(".user@example.com"));

            assertTrue(ex.getMessage().contains("Invalid email format"));
        }
    }

    @Nested
    @DisplayName("Email length constraints")
    class EmailLengthConstraints {

        @Test
        @DisplayName("create - should accept email with exactly 254 characters")
        void create_ShouldAcceptEmailWithExactly254Characters() {
            // Local part: 64 chars (max allowed)
            String localPart = "a".repeat(64);
            // Domain: 189 chars to reach exactly 254 total
            String domain = "b".repeat(185) + ".com";
            String email = localPart + "@" + domain; // 64 + 1 + 189 = 254

            Email result = Email.of(email);

            assertNotNull(result);
            assertEquals(254, result.value().length());
        }

        @Test
        @DisplayName("create - should reject email longer than 254 characters")
        void create_ShouldRejectEmailLongerThan254Characters() {
            String localPart = "a".repeat(64);
            String domain = "b".repeat(186) + ".com"; // 190 chars
            String email = localPart + "@" + domain; // 255 chars

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> Email.of(email));

            assertTrue(ex.getMessage().contains("Email exceeds maximum length"));
            assertTrue(ex.getMessage().contains("254"));
        }
    }

    @Nested
    @DisplayName("Value object semantics")
    class ValueObjectSemantics {

        @Test
        @DisplayName("equals - should be equal after normalization")
        void equals_ShouldBeEqualAfterNormalization() {
            Email email1 = Email.of("USER@EXAMPLE.COM");
            Email email2 = Email.of("user@example.com");
            Email email3 = Email.of("  user@example.com  ");

            assertEquals(email1, email2);
            assertEquals(email2, email3);
            assertEquals(email1, email3);
        }

        @Test
        @DisplayName("hashCode - should be consistent with equals")
        void hashCode_ShouldBeConsistentWithEquals() {
            Email email1 = Email.of("USER@EXAMPLE.COM");
            Email email2 = Email.of("user@example.com");

            assertEquals(email1.hashCode(), email2.hashCode());
        }

        @Test
        @DisplayName("toString - should return normalized value")
        void toString_ShouldReturnNormalizedValue() {
            Email email = Email.of("  User@Example.COM  ");

            assertEquals("user@example.com", email.toString());
        }

        @Test
        @DisplayName("value - should return normalized value")
        void value_ShouldReturnNormalizedValue() {
            Email email = Email.of("  TEST@EXAMPLE.COM  ");

            assertEquals("test@example.com", email.value());
        }
    }
}
