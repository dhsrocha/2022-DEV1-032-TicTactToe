package com.dhsrocha.kata.tictactoe.system.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.ZonedDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.http.MediaType;

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

  @Test
  @DisplayName(
      "Should "
          + "\"setDefaultMediaType\" as APPLICATION_JSON and "
          + "\"useHalAsDefaultJsonMediaType\" as false.")
  void should_setDefaultMediaType_asAPPLICATION_JSON_and_useHalAsDefaultJsonMediaType_asFalse() {
    // Arrange
    final var subject = new Configuration();
    var cfg = mock(RepositoryRestConfiguration.class);
    // Act
    subject.configureRepositoryRestConfiguration(cfg, null);
    // Assert
    verify(cfg).setDefaultMediaType(MediaType.APPLICATION_JSON);
    verify(cfg).useHalAsDefaultJsonMediaType(Boolean.FALSE);
  }
}
