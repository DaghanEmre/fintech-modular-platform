package com.daghanemre.fintech.customer.infrastructure.config;

import com.daghanemre.fintech.customer.application.usecase.ActivateCustomerUseCase;
import com.daghanemre.fintech.customer.domain.port.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for application use cases.
 *
 * <p>
 * <b>Purpose:</b>
 * Wires application layer use cases with their dependencies (repository ports).
 * This keeps the application layer framework-agnostic while enabling Spring DI.
 *
 * <p>
 * <b>Design Note:</b>
 * Use cases are intentionally NOT annotated with {@code @Service} or
 * {@code @Component}
 * to maintain independence from Spring. Instead, they are instantiated via
 * explicit {@code @Bean} methods.
 */
@Configuration
public class UseCaseConfiguration {

    /**
     * Creates the {@link ActivateCustomerUseCase} bean.
     *
     * @param customerRepository the repository implementation (injected by Spring)
     * @return configured use case instance
     */
    @Bean
    public ActivateCustomerUseCase activateCustomerUseCase(CustomerRepository customerRepository) {
        return new ActivateCustomerUseCase(customerRepository);
    }
}
