package com.daghanemre.fintech.customer.domain.model;

import java.util.regex.Pattern;

/**
 * Value Object representing a Customer's email address.
 *
 * <p>This implementation is RFC-inspired but pragmatically focused on:
 * <ul>
 *     <li>Business safety (no exotic RFC 5322 edge cases)</li>
 *     <li>Human-readable format</li>
 *     <li>Duplicate prevention through normalization</li>
 * </ul>
 *
 * <p>Email addresses are:
 * <ul>
 *     <li><b>Normalized</b>: trimmed and lowercased</li>
 *     <li><b>Length-validated</b>: max 254 characters (RFC 5321/3696)</li>
 *     <li><b>Format-validated</b>: pragmatic, business-safe regex pattern</li>
 * </ul>
 *
 * <p>NOT validated:
 * <ul>
 *     <li>DNS or MX records (infrastructure concern)</li>
 *     <li>TLD existence (sandbox and test environments need flexibility)</li>
 *     <li>Deliverability (external email services responsibility)</li>
 * </ul>
 */
public record Email(String value) {

    /**
     * RFC-inspired email pattern for business use.
     *
     * <p>Allows:
     * <ul>
     *     <li>Standard alphanumeric + common special characters</li>
     *     <li>Dot notation (user.name@example.com)</li>
     *     <li>Plus addressing (user+tag@example.com)</li>
     *     <li>Subdomains (user@mail.example.com)</li>
     * </ul>
     *
     * <p>Rejects:
     * <ul>
     *     <li>Quoted strings ("john doe"@example.com)</li>
     *     <li>IP literals (user@[192.168.1.1])</li>
     *     <li>Exotic RFC 5322 edge cases</li>
     * </ul>
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$"
    );

    /**
     * Maximum allowed email length per RFC 5321/3696.
     * Enforced at domain level to prevent database and API issues.
     */
    private static final int MAX_EMAIL_LENGTH = 254;

    /**
     * Compact constructor performing validation and normalization.
     *
     * <p>Normalization steps:
     * <ol>
     *     <li>Trim whitespace (tolerates API formatting issues)</li>
     *     <li>Convert to lowercase (case-insensitive comparison, duplicate prevention)</li>
     * </ol>
     *
     * <p>Validation rules:
     * <ul>
     *     <li>Must not be null or blank</li>
     *     <li>Must not exceed {@link #MAX_EMAIL_LENGTH}</li>
     *     <li>Must match business-safe email pattern</li>
     * </ul>
     *
     * @throws IllegalArgumentException if the email is invalid
     */
    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }

        // Preserve raw input for meaningful exception messages
        String rawValue = value;

        // Normalize
        value = value.trim().toLowerCase();

        // Length validation
        if (value.length() > MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException(
                "Email exceeds maximum length of " + MAX_EMAIL_LENGTH + " characters: " + rawValue
            );
        }

        // Format validation
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + rawValue);
        }
    }

    /**
     * Factory method for creating an Email value object from a raw string.
     *
     * <p>This method is intended as the primary entry point when creating
     * Email instances from external sources (API, persistence, messaging).
     *
     * @param value raw email string
     * @return validated and normalized Email instance
     * @throws IllegalArgumentException if the email is invalid
     */
    public static Email of(String value) {
        return new Email(value);
    }

    /**
     * Returns the normalized email address.
     *
     * <p>The returned value is always lowercase and trimmed.
     * This behavior is intentional, as this value object represents
     * a single semantic value.
     *
     * @return normalized email string
     */
    @Override
    public String toString() {
        return value;
    }
}
