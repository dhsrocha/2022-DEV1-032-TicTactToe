package com.dhsrocha.kata.tictactoe.system.configuration;

import static org.springframework.security.config.Customizer.withDefaults;

import com.dhsrocha.kata.tictactoe.feature.auth.Auth;
import com.dhsrocha.kata.tictactoe.feature.auth.Auth.Role;
import javax.sql.DataSource;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * System configuration scoped to security concerns.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@SuppressWarnings("unused")
@EnableWebSecurity
interface AuthConfiguration {

  /** Defines http chain across profiled configuration. */
  SecurityFilterChain chain(final HttpSecurity http) throws Exception;

  /** Defines which security-related technologies will be used. */
  default HttpSecurity chainPolicy(@NonNull final HttpSecurity http) throws Exception {
    return http.cors()
        .and()
        .sessionManagement(cfg -> cfg.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeRequests(a -> a.antMatchers("/h2-console/**", "/v3/api-docs/**").permitAll())
        .authorizeRequests(a -> a.antMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll())
        .authorizeRequests(a -> a.anyRequest().authenticated());
  }

  /**
   * Security setup for test environments. Relies on autoconfiguration and, additionally, add basic
   * authentication.
   *
   * <p>Is meant to have the most manageable setup for requests.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @Configuration
  class Test implements AuthConfiguration {

    /**
     * Adds Basic authentication.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7617">Specification Reference</a>
     */
    @Bean
    @Override
    public SecurityFilterChain chain(final HttpSecurity http) throws Exception {
      return chainPolicy(http).csrf().disable().httpBasic(withDefaults()).getOrBuild();
    }

    @Bean
    UserDetailsManager service() {
      final var svc = new InMemoryUserDetailsManager();
      final var user =
          User.withUsername(Auth.ADMIN).password(Auth.ADMIN).authorities(Role.ADMIN, Role.PLAYER);
      svc.createUser(user.build());
      return svc;
    }

    @Bean
    PasswordEncoder encoder() {
      return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    DataSource dataSource() {
      return new EmbeddedDatabaseBuilder()
          .setType(EmbeddedDatabaseType.H2)
          .addScript(JdbcDaoImpl.DEFAULT_USER_SCHEMA_DDL_LOCATION)
          .build();
    }
  }
}
