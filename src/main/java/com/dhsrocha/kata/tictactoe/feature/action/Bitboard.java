package com.dhsrocha.kata.tictactoe.feature.action;

import com.dhsrocha.kata.tictactoe.feature.game.Game;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;
import javax.persistence.Embeddable;
import javax.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

/**
 * Representation of board's state of a {@link Game} in the moment an {@link Action} is occurred.
 *
 * <p>It acts like the game's engine and will assemble all rules and their corresponding
 * calculation.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Bitboard">Technical reference</a>
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Embeddable
@Value(staticConstructor = "of")
class Bitboard implements Serializable, Comparable<Bitboard> {

  /** Comparison criteria. */
  private static final Comparator<Bitboard> COMPARATOR = Comparator.comparing(Bitboard::getState);
  /** Value in bitboard notation. */
  @Schema(description = "Value in bitboard notation.")
  @PositiveOrZero
  int state;

  @Override
  public int compareTo(@NonNull final Bitboard other) {
    return COMPARATOR.compare(this, other);
  }

  /**
   * Represents the outgoing result of a game.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @AllArgsConstructor
  enum Result {
    /** Home player has won the game. */
    HOME,
    /** Away player has won the game. */
    AWAY,
    /** The game is finished and there is no possibility to perform action on it. */
    TIE,
    /** The game is not over. There is possibility to bitboard evolve over. */
    NOT_OVER,
  }

  enum RuleSet {
    TIC_TAC_TOE {
      @Override
      Result calculate(final int bitboard) {
        if (bitboard < 0) {
          throw new IllegalArgumentException(MSG_POSITIVE);
        }
        final var rounds = Integer.bitCount(bitboard);
        if (rounds > 9) {
          throw new IllegalArgumentException(MSG_BITS);
        }
        if (rounds < 5) {
          return Result.NOT_OVER;
        }
        final var homeWon = WIN_STATES.stream().anyMatch(w -> w == (w & (bitboard >> 9)));
        final var awayWon = WIN_STATES.stream().anyMatch(w -> w == (w & (bitboard & (1 << 9) - 1)));
        return rounds == 9 && homeWon == awayWon
            ? Result.TIE
            : homeWon ? Result.HOME : awayWon ? Result.AWAY : Result.NOT_OVER;
      }

      /** Winning states. */
      private static final Set<Integer> WIN_STATES =
          Set.of(
              /*
               * |o o o|
               * |     |
               * |     |
               */
              0b111000000,
              /*
               * |     |
               * |o o o|
               * |     |
               */
              0b000111000,
              /*
               * |     |
               * |     |
               * |o o o|
               */
              0b000000111,
              /*
               * |o    |
               * |o    |
               * |o    |
               */
              0b100100100,
              /*
               * |  o  |
               * |  o  |
               * |  o  |
               */
              0b010010010,
              /*
               * |    o|
               * |    o|
               * |    o|
               */
              0b001001001,
              /*
               * |o    |
               * |  o  |
               * |    o|
               */
              0b100010001,
              /*
               * |    o|
               * |  o  |
               * |o    |
               */
              0b001010100);
    },
    ;

    private static final String MSG_POSITIVE = "Bitboard must be positive or zero";
    private static final String MSG_BITS = "Bitboard bit count must be 18 bits at most";

    /**
     * Calculates if an bitboard has a winning state, according to the provide rule set.
     *
     * @param bitboard The boards' state in bitboard notation.
     * @return A outgoing result of the state.
     */
    abstract Result calculate(final int bitboard);
  }
}
