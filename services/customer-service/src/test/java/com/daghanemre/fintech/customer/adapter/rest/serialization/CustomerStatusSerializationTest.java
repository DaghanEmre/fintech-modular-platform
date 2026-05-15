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
 * <p>This is NOT a pure Jackson test. It is a SPRING BOOT RUNTIME CONTRACT verification.
 *
 * <p>Key distinction: {@code new ObjectMapper()} ≠ Spring Boot's auto-configured ObjectMapper.
 * Spring Boot registers Jackson modules, naming strategies, and serialization policies.
 * A standalone instance misses all of these — silent drift would not be caught.
 *
 * <p>{@code @JsonTest} is NOT bloat:
 * <ul>
 *   <li>It is a minimal slice — only JSON configuration is loaded</li>
 *   <li>Startup takes ~200ms vs ~2s for full {@code @SpringBootTest}</li>
 *   <li>It verifies the production mapper, not raw Jackson defaults</li>
 *   <li>Spring Boot 3.2 → 3.5 may ship different Jackson modules or defaults</li>
 * </ul>
 *
 * <p>Package rationale: {@code adapter.rest.serialization} — validates the Jackson/domain
 * contract at the API boundary, not pure domain logic (see {@code domain.model.CustomerStatusTest}).
 *
 * @see com.daghanemre.fintech.customer.domain.model.CustomerStatus
 */
@JsonTest
class CustomerStatusSerializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeCustomerStatusAsString() throws Exception {
        // Spring Boot configured ObjectMapper must serialize enum as string, not ordinal.
        // Risk: Boot version upgrade may change Jackson enum defaults silently.
        String json = objectMapper.writeValueAsString(CustomerStatus.ACTIVE);

        assertThat(json)
                .as("CustomerStatus must serialize as JSON string via Spring Boot ObjectMapper")
                .isEqualTo("\"ACTIVE\"");
    }

    @Test
    void shouldDeserializeCustomerStatusFromString() throws Exception {
        // Deserialization must be the inverse of serialization via the same configured mapper.
        CustomerStatus result = objectMapper.readValue("\"ACTIVE\"", CustomerStatus.class);

        assertThat(result)
                .as("JSON string 'ACTIVE' must deserialize to CustomerStatus.ACTIVE")
                .isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    void shouldParseKnownCustomerStatus() {
        var result = CustomerStatus.safeParse("ACTIVE");

        assertThat(result)
                .as("Known enum value ACTIVE must parse successfully")
                .contains(CustomerStatus.ACTIVE);
    }

    @Test
    void shouldTolerateUnknownFutureCustomerStatus() {
        // Forward compatibility: newer producer sends unknown value — must not crash.
        var result = CustomerStatus.safeParse("FROZEN");

        assertThat(result)
                .as("Unknown future enum value must return Optional.empty(), not throw")
                .isEmpty();
    }

    @Test
    void shouldFailExplicitlyForUnknownEnumDuringJacksonDeserialization() {
        // Jackson's default: throw on unknown enum. This test documents the contract.
        // If this behavior changes (e.g., to silent null), the test will catch it.
        // See ADR-0006 (planned) for @JsonEnumDefaultValue strategy consideration.
        assertThatThrownBy(() -> objectMapper.readValue("\"FROZEN\"", CustomerStatus.class))
                .as("Unknown enum value must cause Jackson to throw, not return null")
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldHandleNullGracefully() {
        var result = CustomerStatus.safeParse(null);

        assertThat(result)
                .as("Null input to safeParse must return Optional.empty(), not throw NPE")
                .isEmpty();
    }
}
