package com.daghanemre.fintech.customer.domain.model;

/**
 * Represents the lifecycle status of a Customer within the system.
 *
 * This enum reflects real-world FinTech and banking requirements,
 * including compliance, fraud handling, and customer-initiated actions.
 *
 * Status transition rules are intentionally NOT enforced here yet.
 * They will be introduced as the domain complexity grows.
 */
public enum CustomerStatus {

    /**
     * Customer has been created but KYC verification is not completed yet.
     * Limited or no operations are allowed.
     */
    PENDING,

    /**
     * Customer is fully verified and allowed to perform normal operations.
     */
    ACTIVE,

    /**
     * Customer is temporarily restricted due to fraud suspicion or investigation.
     * Reversible state.
     */
    SUSPENDED,

    /**
     * Customer account has been closed by the customer.
     * No further changes should be allowed.
     */
    INACTIVE,

    /**
     * Customer is permanently blocked due to AML or severe fraud.
     * This state is irreversible.
     */
    BLOCKED
}

