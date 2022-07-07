package com.dhsrocha.kata.tictactoe.vo;

import com.dhsrocha.kata.tictactoe.feature.game.Game;
import com.dhsrocha.kata.tictactoe.feature.turn.Turn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Comparator;
import javax.persistence.Embeddable;
import javax.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Representation of board's state of a {@link Game} in the moment an {@link Turn} is occurred.
 *
 * <p>It acts like the game's engine and will assemble all rules and their corresponding
 * calculation.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Bitboard">Technical reference</a>
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Embeddable
@Schema(
    description = "Representation of board's state of a Game in the moment an Turn is occurred.")
@Data
@Setter(AccessLevel.NONE)
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Bitboard implements Serializable, Comparable<Bitboard> {

  /**
   * Processes {@link Bitboard}'s containing state.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see Bitboard
   */
  public interface Processor {

    /**
     * Calculates the end conditions, if any, from the provided bitboard's state, according to a
     * {@link Game}'s set of rules.
     *
     * @param state A {@link Game}'s board in bitboard notation.
     * @return A outgoing result of the state.
     */
    Result process(@NonNull final Bitboard state);
  }

  /** Comparison criteria. */
  private static final Comparator<Bitboard> COMPARATOR = Comparator.comparing(Bitboard::getState);
  /** Value in bitboard notation. */
  @Schema(description = "State in bitboard notation.")
  private @PositiveOrZero long state;

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
  public enum Result {
    /** {@link Game} is over with {@link Game#getHome()} player as the winner. */
    HOME(true),
    /** {@link Game} is over with {@link Game#getAway()} player as the winner. */
    AWAY(true),
    /** {@link Game} is finished and there is no possibility to perform another actions on it. */
    TIE(false),
    /** {@link Game} is not over. */
    NOT_OVER(false),
    ;
    /** The result has a determined winner. */
    private final @Getter boolean isFinished;
  }
}
