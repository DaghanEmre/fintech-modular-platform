package com.daghanemre.fintech.customer.adapter.rest.serialization;

import com.daghanemre.fintech.customer.domain.model.CustomerStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Enum Serialization Compatibility Test (ADR-0008 — Risk Mitigation Gate 4: Behavioral)
 *
 * <p>Verifies that Jackson serialization behavior is stable across Spring Boot upgrades.
 * Spring Boot 3.2 → 3.5 may silently change Jackson defaults around enum coercion
 * and unknown value handling. This test prevents behavioral drift.
 *
 * <p>Scenarios covered:
 * <ul>
 *   <li>Serialization: enum → JSON string (not ordinal)</li>
 *   <li>Deserialization: JSON string → enum</li>
 *   <li>Forward compatibility: unknown future enum values via {@code safeParse}</li>
 *   <li>Deserialization drift: unknown values trigger explicit failure (not silent null)</li>
 *   <li>Null safety: null input to {@code safeParse} returns {@code Optional.empty()}</li>
 * </ul>
 *
 * <p>Package rationale: placed under {@code adapter.rest.serialization} because this test
 * validates the Jackson/domain contract at the API boundary, not pure domain logic
 * (which lives in {@code domain.model.CustomerStatusTest}).
 *
 * @see com.daghanemre.fintech.customer.domain.model.CustomerStatus
 * @see com.daghanemre.fintech.customer.domain.model.CustomerStatusTest
 */
@JsonTest
class CustomerStatusSerializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeCustomerStatusAsString() throws Exception {
        // Enum must serialize as string "ACTIVE", not as ordinal 0
        // Risk: Spring Boot upgrade may change Jackson enum serialization defaults
        String json = objectMapper.writeValueAsString(CustomerStatus.ACTIVE);

        assertThat(json)
                .as("CustomerStatus enum must serialize as JSON string, not ordinal")
                .isEqualTo("\"ACTIVE\"");
    }

    @Test
    void shouldDeserializeCustomerStatusFromString() throws Exception {
        // Deserialization must be the inverse of serialization
        // Risk: Jackson version change may alter case sensitivity or enum mapping behavior
        CustomerStatus result = objectMapper.readValue("\"ACTIVE\"", CustomerStatus.class);

        assertThat(result)
                .as("JSON string 'ACTIVE' must deserialize to CustomerStatus.ACTIVE")
                .isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    void shouldParseKnownCustomerStatus() {
        // Domain safeParse must resolve known values
        var result = CustomerStatus.safeParse("ACTIVE");

        assertThat(result)
                .as("Known enum value ACTIVE must parse successfully")
                .contains(CustomerStatus.ACTIVE);
    }

    @Test
    void shouldTolerateUnknownFutureCustomerStatus() {
        // Forward compatibility: newer producer sends "FROZEN" which this consumer doesn't know
        // Expected: graceful degradation via Optional.empty(), not an exception
        var result = CustomerStatus.safeParse("FROZEN");

        assertThat(result)
                .as("Unknown future enum value should not crash; must return Optional.empty()")
                .isEmpty();
    }

    @Test
    void shouldFailGracefullyForUnknownEnumDuringJacksonDeserialization() {
        // Production risk: older consumer receives unknown enum value from newer producer.
        // Jackson's default behavior throws an exception — this test explicitly verifies
        // and documents that contract so any change (e.g., to silent null) is caught.
        //
        // If future-proof handling is needed, consider @JsonEnumDefaultValue on an UNKNOWN
        // sentinel value (architectural decision — track in ADR-0006).
        assertThatThrownBy(() -> objectMapper.readValue("\"FROZEN\"", CustomerStatus.class))
                .as("Jackson deserialization of unknown enum value must throw an exception")
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldHandleNullGracefully() {
        // Null input must not cause NullPointerException in safeParse
        var result = CustomerStatus.safeParse(null);

        assertThat(result)
                .as("Null input to safeParse must return Optional.empty(), not throw NPE")
                .isEmpty();
    }
}
