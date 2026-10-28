package com.pe.pivot.customer.infrastructure.config;

import com.pe.pivot.customer.domain.strategy.BusinessCustomerStrategy;
import com.pe.pivot.customer.domain.strategy.CustomerValidationStrategy;
import com.pe.pivot.customer.domain.strategy.PersonalCustomerStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Configuration class that registers domain validation strategies as beans.
 * Keeps the domain layer free of Spring annotations.
 */
@Configuration
public class StrategyConfig {

    @Bean
    public CustomerValidationStrategy personalCustomerStrategy() {
        return new PersonalCustomerStrategy();
    }

    @Bean
    public CustomerValidationStrategy businessCustomerStrategy() {
        return new BusinessCustomerStrategy();
    }
}
