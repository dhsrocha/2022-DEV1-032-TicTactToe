package com.dhsrocha.kata.tictactoe.feature.action;

import com.dhsrocha.kata.tictactoe.feature.game.Game;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Comparator;
import javax.persistence.Embeddable;
import javax.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * Representation of board's state of a {@link Game} in the moment an {@link Action} is occurred.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Bitboard">Technical reference</a>
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Embeddable
@Data
@Builder(toBuilder = true)
class Bitboard implements Serializable, Comparable<Bitboard> {

  /** Comparison criteria. */
  private static final Comparator<Bitboard> COMPARATOR = Comparator.comparing(Bitboard::getState);

  /** Value in bitboard notation. */
  @Schema(description = "Value in bitboard notation.")
  @PositiveOrZero
  private final byte state;

  @Override
  public int compareTo(@NonNull final Bitboard other) {
    return COMPARATOR.compare(this, other);
  }
}
