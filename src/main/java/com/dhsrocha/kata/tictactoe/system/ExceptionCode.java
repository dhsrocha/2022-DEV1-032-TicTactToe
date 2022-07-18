package com.dhsrocha.kata.tictactoe.system;

import com.dhsrocha.kata.tictactoe.feature.game.Game;
import com.dhsrocha.kata.tictactoe.feature.player.Player;
import com.dhsrocha.kata.tictactoe.feature.turn.Turn;
import com.dhsrocha.kata.tictactoe.vo.Bitboard;
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
  /** {@link Turn} cannot be calculated without its previous round. */
  TURN_IS_THE_FIRST(HttpStatus.CONFLICT),

  // Bitboard
  /** {@link Bitboard}'s has not any bit set. */
  BITBOARD_UNSET_STATE(HttpStatus.BAD_REQUEST),
  /** {@link Bitboard}'s has more bits than predicted for {@link Game}'s set of rules. */
  BITBOARD_EXCESSIVE_BITS(HttpStatus.BAD_REQUEST),
  /** {@link Bitboard} has overlapping bits in its subsets. */
  BITBOARD_PIECE_IN_SAME_TILE(HttpStatus.BAD_REQUEST),
  /** {@link Bitboard} has excessive number of bit between two contiguous bitboards. */
  BITBOARD_EXCESSIVE_BITS_PER_ROUND(HttpStatus.BAD_REQUEST);
  /** Corresponding HTTP status. */
  private final HttpStatus code;

  @Override
  public final RuntimeException get() {
    return new HttpClientErrorException(code, name());
  }

  /** Throws the indexed exception. */
  public final void trigger() {
    throw get();
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
