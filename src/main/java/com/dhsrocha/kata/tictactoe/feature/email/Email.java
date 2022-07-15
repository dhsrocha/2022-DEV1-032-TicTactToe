package com.dhsrocha.kata.tictactoe.feature.email;

import java.nio.file.Path;
import lombok.AllArgsConstructor;

/**
 * .
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@AllArgsConstructor
public enum Email {
  /** . */
  // TODO
  CONFIRMATION(Path.of("You need to confirm ")),
  ;
  public static final String TAG = "email";
  /** Template path. */
  private final Path template;
}
