package com.daghanemre.fintech.customer.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StateChangeReason Value Object")
class StateChangeReasonTest {

    @Nested
    @DisplayName("Valid reason creation")
    class ValidReasonCreation {

        @Test
        @DisplayName("of - should accept valid reason and trim it")
        void of_ShouldAcceptValidReason() {
            String value = "  Valid reason for status change  ";
            StateChangeReason reason = StateChangeReason.of(value);

            assertNotNull(reason);
            assertEquals("Valid reason for status change", reason.value());
        }

        @Test
        @DisplayName("of - should accept reason with minimum length")
        void of_ShouldAcceptMinLength() {
            String value = "abc";
            StateChangeReason reason = StateChangeReason.of(value);
            assertEquals(3, reason.value().length());
        }

        @Test
        @DisplayName("of - should accept reason with maximum length")
        void of_ShouldAcceptMaxLength() {
            String value = "a".repeat(500);
            StateChangeReason reason = StateChangeReason.of(value);
            assertEquals(500, reason.value().length());
        }
    }

    @Nested
    @DisplayName("Invalid reason creation")
    class InvalidReasonCreation {

        @Test
        @DisplayName("of - should reject null reason")
        void of_ShouldRejectNull() {
            assertThrows(IllegalArgumentException.class, () -> StateChangeReason.of(null));
        }

        @Test
        @DisplayName("of - should reject blank reason")
        void of_ShouldRejectBlank() {
            assertThrows(IllegalArgumentException.class, () -> StateChangeReason.of("   "));
        }

        @Test
        @DisplayName("of - should reject reason shorter than 3 characters")
        void of_ShouldRejectTooShort() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> StateChangeReason.of("ab"));
            assertTrue(ex.getMessage().contains("at least 3 characters"));
        }

        @Test
        @DisplayName("of - should reject reason longer than 500 characters")
        void of_ShouldRejectTooLong() {
            String tooLong = "a".repeat(501);
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> StateChangeReason.of(tooLong));
            assertTrue(ex.getMessage().contains("cannot exceed 500 characters"));
        }
    }

    @Nested
    @DisplayName("Value object semantics")
    class ValueObjectSemantics {

        @Test
        @DisplayName("equals - should be equal when values match")
        void equals_ShouldMatch() {
            StateChangeReason reason1 = StateChangeReason.of("Same reason");
            StateChangeReason reason2 = StateChangeReason.of(" Same reason ");
            assertEquals(reason1, reason2);
        }

        @Test
        @DisplayName("hashCode - should be consistent with equals")
        void hashCode_ShouldBeConsistent() {
            StateChangeReason reason1 = StateChangeReason.of("Reason");
            StateChangeReason reason2 = StateChangeReason.of("Reason");
            assertEquals(reason1.hashCode(), reason2.hashCode());
        }

        @Test
        @DisplayName("toString - should return the value")
        void toString_ShouldReturnValue() {
            StateChangeReason reason = StateChangeReason.of("The Reason");
            assertEquals("The Reason", reason.toString());
        }
    }
}
