package com.dhsrocha.kata.tictactoe.system;

import java.time.ZonedDateTime;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * System's configuration entry-point.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@org.springframework.context.annotation.Configuration
@EnableJpaAuditing(dateTimeProviderRef = "providerRef", modifyOnCreate = false)
class Configuration {

  /**
   * To make {@link ZonedDateTime} compatible with auditing fields.
   *
   * @return Typed timestamp for the creation or modification event.
   */
  @Bean
  public DateTimeProvider providerRef() {
    return () -> Optional.of(ZonedDateTime.now());
  }
}
