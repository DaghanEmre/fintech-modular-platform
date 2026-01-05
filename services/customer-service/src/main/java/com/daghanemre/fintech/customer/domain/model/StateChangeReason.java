package com.daghanemre.fintech.customer.domain.model;

/**
 * Value Object representing the reason for a customer status change.
 */
public record StateChangeReason(String value) {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 500;

    public StateChangeReason {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Reason cannot be null or blank");
        }

        value = value.trim();

        if (value.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                    "Reason must be at least " + MIN_LENGTH + " characters");
        }

        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Reason cannot exceed " + MAX_LENGTH + " characters");
        }
    }

    public static StateChangeReason of(String value) {
        return new StateChangeReason(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
