package com.daghanemre.fintech.customer.infrastructure.adapter.rest.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.Optional;

/**
 * Maps domain-specific specification violation codes to corresponding HTTP
 * status codes.
 * This ensures consistent API behavior across different business rules.
 */
public final class SpecificationHttpStatusMapper {

    private static final Map<String, HttpStatus> MAPPINGS = Map.of(
            "CUSTOMER_DELETED", HttpStatus.GONE, // 410
            "CUSTOMER_BLOCKED", HttpStatus.FORBIDDEN, // 403
            "CUSTOMER_NOT_PENDING", HttpStatus.CONFLICT, // 409
            "CUSTOMER_NOT_SUSPENDED", HttpStatus.CONFLICT, // 409
            "CUSTOMER_NOT_ACTIVE", HttpStatus.CONFLICT, // 409
            "CUSTOMER_ALREADY_ACTIVE", HttpStatus.CONFLICT, // 409
            "INVALID_STATUS_TRANSITION", HttpStatus.CONFLICT // 409
    );

    private SpecificationHttpStatusMapper() {
    }

    /**
     * Resolves the HTTP status for a given violation code.
     *
     * @param code The domain violation code
     * @return The corresponding HttpStatus, or 422 (Unprocessable Entity) as
     *         default
     */
    public static HttpStatus resolve(String code) {
        return Optional.ofNullable(MAPPINGS.get(code))
                .orElse(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
