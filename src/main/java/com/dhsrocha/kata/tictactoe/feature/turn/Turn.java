package com.dhsrocha.kata.tictactoe.feature.turn;

import com.dhsrocha.kata.tictactoe.base.Domain;
import com.dhsrocha.kata.tictactoe.feature.game.Game;
import com.dhsrocha.kata.tictactoe.feature.player.Player;
import com.dhsrocha.kata.tictactoe.vo.Bitboard;
import com.dhsrocha.kata.tictactoe.vo.Bitboard.Processed;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import java.util.Comparator;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Represents an event which depicts the state of a {@link Game} in time.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Schema(description = "Represents an event which depicts the state of a Game in time.")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter(AccessLevel.PACKAGE)
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class Turn extends Domain implements Comparable<Turn> {

  /** Domain tag to use on endpoint paths and OpenAPI grouping. */
  public static final String TAG = "turns";
  /** Id to use on {@link PathVariable}'s {@link URI}. */
  public static final String ID = "turnId";
  /** Comparison criteria. */
  private static final Comparator<Turn> COMPARATOR =
      Comparator.comparing(Turn::getGame)
          .thenComparing(Turn::getPlayer)
          .thenComparing(Turn::getState)
          .thenComparing(Turn::getLast);

  /** Game where this action is taken. */
  @Schema(description = "Game where this action is taken.")
  @ManyToOne(optional = false)
  private @NotNull @NonNull Game game;
  /** Person who did the action. */
  @Schema(description = "Person who did the action.")
  @ManyToOne(optional = false)
  private @NotNull @NonNull Player player;
  /** Represents the game board state in bitboard notation. */
  @Schema(description = "Represents the game board position in bitboard notation.")
  @Embedded
  private @NotNull @NonNull Bitboard state;
  /** Last computed turn from current one. */
  @Schema(hidden = true)
  private transient @Transient Turn last;

  /**
   * Processes its internal state by checking for its validity.
   *
   * @return State with its validity checked.
   */
  public final Processed validState() {
    return state.process(game.getType());
  }

  @Override
  public final int compareTo(@NonNull final Turn toCompare) {
    return COMPARATOR.thenComparing(DOMAIN_COMPARATOR).compare(this, toCompare);
  }
}
