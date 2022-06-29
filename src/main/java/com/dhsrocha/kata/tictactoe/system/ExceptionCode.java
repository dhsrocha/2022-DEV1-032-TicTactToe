package com.dhsrocha.kata.tictactoe.system;

import com.dhsrocha.kata.tictactoe.feature.game.Game;
import com.dhsrocha.kata.tictactoe.feature.player.Player;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

/**
 * System's unique exception, to holld.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@AllArgsConstructor
public enum ExceptionCode implements Supplier<RuntimeException> {
  // Player
  /** {@link Player} is not in {@link Game}. */
  PLAYER_NOT_IN_GAME(HttpStatus.CONFLICT),

  // Game
  /** {@link Game} is not in an in-progress stage. */
  GAME_NOT_IN_PROGRESS(HttpStatus.CONFLICT),
  ;
  /** Corresponding HTTP status. */
  private final HttpStatus code;

  @Override
  public final RuntimeException get() {
    return new HttpClientErrorException(code, name());
  }

  /**
   * Throws exception with the indexed HTTP status code if provided condition is not satisfied.
   *
   * @param test The test with condition.
   * @throws HttpClientErrorException if the provided test is not satisfied.
   */
  public void unless(final boolean test) {
    if (!test) {
      throw get();
    }
  }
}
