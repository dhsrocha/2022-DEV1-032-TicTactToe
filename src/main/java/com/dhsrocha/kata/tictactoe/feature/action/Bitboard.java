package com.dhsrocha.kata.tictactoe.feature.action;

import com.dhsrocha.kata.tictactoe.feature.game.Game;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Comparator;
import javax.persistence.Embeddable;
import javax.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

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
@Data
@Setter(AccessLevel.PACKAGE)
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class Bitboard implements Serializable, Comparable<Bitboard> {

  /** Comparison criteria. */
  private static final Comparator<Bitboard> COMPARATOR = Comparator.comparing(Bitboard::getState);
  /** Value in bitboard notation. */
  @Schema(description = "Value in bitboard notation.")
  @PositiveOrZero
  private int state;

  @Override
  public int compareTo(@NonNull final Bitboard other) {
    return COMPARATOR.compare(this, other);
  }
}
