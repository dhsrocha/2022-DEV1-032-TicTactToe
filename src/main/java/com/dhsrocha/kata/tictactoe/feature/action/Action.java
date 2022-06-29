package com.dhsrocha.kata.tictactoe.feature.action;

import com.dhsrocha.kata.tictactoe.base.Domain;
import com.dhsrocha.kata.tictactoe.feature.game.Game;
import com.dhsrocha.kata.tictactoe.feature.player.Player;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Comparator;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Represents an action which can be made in a {@link Game}.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@Setter(AccessLevel.PACKAGE)
public class Action extends Domain implements Comparable<Action> {

  /** Domain tag to use on endpoint paths and OpenAPI grouping. */
  public static final String TAG = "actions";
  /** Comparison criteria. */
  private static final Comparator<Action> COMPARATOR =
      Comparator.comparing(Action::getGame)
          .thenComparing(Action::getPlayer)
          .thenComparing(Action::getState);

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

  @Override
  public final int compareTo(@NonNull final Action toCompare) {
    return COMPARATOR.thenComparing(DOMAIN_COMPARATOR).compare(this, toCompare);
  }
}
