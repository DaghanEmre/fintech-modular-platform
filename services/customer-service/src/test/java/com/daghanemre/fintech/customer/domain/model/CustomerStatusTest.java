package com.daghanemre.fintech.customer.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CustomerStatus Enum Tests")
class CustomerStatusTest {

    @ParameterizedTest
    @ValueSource(strings = {"PENDING", "ACTIVE", "SUSPENDED", "INACTIVE", "BLOCKED"})
    @DisplayName("safeParse - should parse valid statuses correctly")
    void safeParse_ShouldParseValidStatuses(String value) {
        Optional<CustomerStatus> result = CustomerStatus.safeParse(value);
        
        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo(value);
    }

    @Test
    @DisplayName("safeParse - should handle case-insensitive input and trimming")
    void safeParse_ShouldHandleCaseAndTrimming() {
        Optional<CustomerStatus> result = CustomerStatus.safeParse("  active  ");
        
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    @DisplayName("safeParse - should return empty for null or blank input")
    void safeParse_ShouldReturnEmptyForNullOrBlank() {
        assertThat(CustomerStatus.safeParse(null)).isEmpty();
        assertThat(CustomerStatus.safeParse("")).isEmpty();
        assertThat(CustomerStatus.safeParse("   ")).isEmpty();
    }

    @Test
    @DisplayName("safeParse - should provide Forward Compatibility for unknown statuses")
    void safeParse_ShouldReturnEmptyForUnknownStatus() {
        // Simulating a status that might exist in a future version (e.g., FROZEN)
        Optional<CustomerStatus> result = CustomerStatus.safeParse("FROZEN");
        
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("toExternalValue - should return the name of the enum")
    void toExternalValue_ShouldReturnEnumName() {
        assertThat(CustomerStatus.ACTIVE.toExternalValue()).isEqualTo("ACTIVE");
        assertThat(CustomerStatus.BLOCKED.toExternalValue()).isEqualTo("BLOCKED");
    }
}
