package com.pe.pivot.customer.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.pe.pivot.customer.domain.strategy.BusinessCustomerStrategy;
import com.pe.pivot.customer.domain.strategy.CustomerValidationStrategy;
import com.pe.pivot.customer.domain.strategy.PersonalCustomerStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link StrategyConfig}.
 * Guarantees that both strategy beans are registered with the expected type.
 */
class StrategyConfigTest {

  private StrategyConfig config;

  @BeforeEach
  void setUp() {
    config = new StrategyConfig();
  }

  @Test
  @DisplayName("should create the personal customer strategy bean")
  void shouldCreatePersonalStrategyBean() {
    // Act
    CustomerValidationStrategy strategy = config.personalCustomerStrategy();

    // Assert
    assertInstanceOf(PersonalCustomerStrategy.class, strategy);
  }

  @Test
  @DisplayName("should create the business customer strategy bean")
  void shouldCreateBusinessStrategyBean() {
    // Act
    CustomerValidationStrategy strategy = config.businessCustomerStrategy();

    // Assert
    assertInstanceOf(BusinessCustomerStrategy.class, strategy);
  }
}
