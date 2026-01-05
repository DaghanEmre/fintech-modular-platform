package com.daghanemre.fintech.customer.infrastructure.persistence;

import com.daghanemre.fintech.customer.integration.AbstractIntegrationTest;

/**
 * Base class for JPA-specific integration tests.
 * Extensions should focus on repository and mapping logic.
 */
public abstract class AbstractJpaIntegrationTest extends AbstractIntegrationTest {
    // Inherits singleton container and dynamic property configuration
}
