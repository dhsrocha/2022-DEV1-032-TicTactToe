package com.dhsrocha.kata.tictactoe.system;

import com.dhsrocha.kata.tictactoe.feature.game.Game;
import com.dhsrocha.kata.tictactoe.feature.player.Player;
import com.dhsrocha.kata.tictactoe.feature.turn.Turn;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Assembles the system's exception codes regarding a business concern.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@AllArgsConstructor
public enum ExceptionCode implements Supplier<RuntimeException> {
  // Player
  /** {@link Player} could not be found. */
  PLAYER_NOT_FOUND(HttpStatus.NOT_FOUND),
  /** {@link Player} is in a ongoing {@link Game}. */
  PLAYER_IN_AN_ONGOING_GAME(HttpStatus.CONFLICT),
  /** {@link Player} is not in {@link Game}. */
  PLAYER_NOT_IN_GAME(HttpStatus.CONFLICT),
  /** {@link Player} is already in {@link Game}. */
  PLAYER_ALREADY_IN_GAME(HttpStatus.CONFLICT),

  // Game
  /** {@link Game} could not be found. */
  GAME_NOT_FOUND(HttpStatus.NOT_FOUND),
  /** {@link Game} is not in an awaiting stage. */
  GAME_NOT_IN_AWAITS(HttpStatus.CONFLICT),
  /** {@link Game} is not in an in-progress stage. */
  GAME_NOT_IN_PROGRESS(HttpStatus.CONFLICT),

  // Turn
  /** {@link Turn} could not be found. */
  TURN_NOT_FOUND(HttpStatus.NOT_FOUND),
  /** {@link Turn} which the same {@link Player} did in the last turn. */
  TURN_LAST_SAME_PLAYER(HttpStatus.CONFLICT),
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
