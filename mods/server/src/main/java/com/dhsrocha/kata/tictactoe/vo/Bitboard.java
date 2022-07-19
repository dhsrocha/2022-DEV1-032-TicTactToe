package com.dhsrocha.kata.tictactoe.vo;

import com.dhsrocha.kata.tictactoe.feature.game.Game;
import com.dhsrocha.kata.tictactoe.feature.turn.Turn;
import com.dhsrocha.kata.tictactoe.system.ExceptionCode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Optional;
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
@Schema(description = "Representation of board's state of a Game in the moment a Turn is occurred.")
@Data
@Setter(AccessLevel.NONE)
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Bitboard implements Serializable, Comparable<Bitboard> {

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
   * Puts the instance under a validated state.
   *
   * @param validator Validator engine.
   * @return Bitboard in is valid and processed state.
   */
  public final Processed processWith(
      @NonNull final Bitboard last, @NonNull final Validator validator) {
    return new Processed(last, state, validator);
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

  /**
   * {@link Bitboard} in is valid and processed state.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  public static final class Processed extends Bitboard {

    private Processed(final Bitboard last, final long current, @NonNull final Validator validator) {
      super(current);
      ExceptionCode.BITBOARD_UNSET_STATE.unless(getState() != 0);
      validator.validate(last, this).ifPresent(ExceptionCode::trigger);
    }
  }

  /**
   * Validates {@link Bitboard}'s containing state.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  public interface Validator {

    /**
     * Calculates legal conditions, from the provided bitboard's state, according to a {@link
     * Game}'s set of rules.
     *
     * @param last Last round's state from a {@link Game}'s board, in bitboard notation.
     * @param current Current state from a {@link Game}'s board, in bitboard notation.
     * @return Optionally first exception retrieved.
     */
    Optional<ExceptionCode> validate(@NonNull final Bitboard last, @NonNull final Bitboard current);
  }

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
    Result resultOf(@NonNull final Bitboard.Processed state);
  }
}
