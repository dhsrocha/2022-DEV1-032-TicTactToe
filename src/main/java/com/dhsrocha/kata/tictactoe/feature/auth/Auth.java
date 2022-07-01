package com.dhsrocha.kata.tictactoe.feature.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

/**
 * Ensembles security (authorization and authentication) concerns.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public class Auth {

  public static final String TAG = "auth";
  public static final String ADMIN = "admin";

  /**
   * System's granted authorities.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @Getter
  public enum Role implements GrantedAuthority {
    PLAYER,
    ADMIN,
    ;

    private final String authority = name();
  }
}
