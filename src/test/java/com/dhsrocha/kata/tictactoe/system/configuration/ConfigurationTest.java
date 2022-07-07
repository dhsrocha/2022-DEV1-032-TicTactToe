package com.dhsrocha.kata.tictactoe.system.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test suite for the system's configuration entry-point.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@DisplayName("Test suite for the system's configuration entry-point.")
class ConfigurationTest {

  @DisplayName("Should provide ZonedDateTime as provider.")
  @Test
  void shouldProvide_ZonedDateTime_asProvider() {
    // Arrange
    final var subject = new Configuration();
    // Act
    final var provider = subject.providerRef();
    // Assert
    assertTrue(provider.getNow().isPresent());
    provider.getNow().ifPresent(zdt -> assertEquals(ZonedDateTime.class, zdt.getClass()));
  }
}
