package com.dhsrocha.kata.tictactoe.system.configuration;

import java.time.ZonedDateTime;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * System's configuration entry-point.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@org.springframework.context.annotation.Configuration
@EnableJpaAuditing(dateTimeProviderRef = "providerRef", modifyOnCreate = false)
class Configuration implements RepositoryRestConfigurer, WebMvcConfigurer {

  static final String DEV = "dev";
  static final String PROD = "prod";

  @Override
  public void configureRepositoryRestConfiguration(
      final RepositoryRestConfiguration cfg, final CorsRegistry reg) {
    cfg.setDefaultMediaType(MediaType.APPLICATION_JSON);
    cfg.useHalAsDefaultJsonMediaType(Boolean.FALSE);
  }

  @Override
  public void configureContentNegotiation(final ContentNegotiationConfigurer config) {
    config.defaultContentType(MediaType.APPLICATION_JSON);
  }

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
