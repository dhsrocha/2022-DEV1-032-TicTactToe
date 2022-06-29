package com.dhsrocha.kata.tictactoe.feature.game;

import com.dhsrocha.kata.tictactoe.base.Domain;
import com.dhsrocha.kata.tictactoe.feature.action.Action;
import com.dhsrocha.kata.tictactoe.feature.player.Player;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Comparator;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

/**
 * Holds a match between two {@link Player} entities.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Entity
@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class Game extends Domain implements Comparable<Game> {

  /** Domain tag to use on endpoint paths and OpenAPI grouping. */
  public static final String TAG = "games";
  /** Comparison criteria. */
  private static final Comparator<Game> COMPARATOR =
      Comparator.comparing(Game::getStage)
          .thenComparing(Game::getWinner)
          .thenComparing(Game::getHome)
          .thenComparing(Game::getAway)
          .thenComparing(g -> g.actions.size());

  /** Game's type. Holds its rules in it. */
  @Schema(description = "Game's type. Holds its rules in it.", example = "TIC_TAC_TOE")
  @Column(nullable = false)
  private final Type type;
  /** Current {@link Game}'s life-cycle stage. Starts as {@link Stage#AWAITS}. */
  @Schema(description = "Current Game's life-cycle stage. Starts as Stage#AWAITS.")
  @Column(nullable = false)
  private final @NonNull @NotNull Stage stage;
  /** Home player. Holds the {@link Game} and awaits for another one as {@link #away}. */
  @Schema(description = "Home player. Holds the Game and awaits for another one as away.")
  @ManyToOne
  private final @NotNull Player home;
  /** Away player. Joins the {@link Game} as the {@link #home}'s opponent. */
  @Schema(description = "Away player. Joins the Game as the home's opponent.")
  @ManyToOne
  private final @NotNull Player away;
  /** {@link Game}'s winner. Null until finish it to determine which {@link Player} has won. */
  @Schema(description = "Game's winner. Null until finish it to determine which Player has won.")
  @ManyToOne
  private final Player winner;
  /** Actions occurred in the current game. */
  @Schema(description = "Actions occurred in the current game.")
  @OneToMany(cascade = CascadeType.ALL)
  private final @Singular @NotNull @NonNull Set<@NotNull Action> actions = Set.of();

  @Override
  public final int compareTo(@NonNull final Game toCompare) {
    return COMPARATOR.thenComparing(DOMAIN_COMPARATOR).compare(this, toCompare);
  }

  /**
   * Represents a game's life-cycle.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @Getter
  @AllArgsConstructor
  public enum Stage {
    /** The {@link Game} is finished. */
    FINISHED(null),
    /** The {@link Game} is in progress. */
    IN_PROGRESS(Stage.FINISHED),
    /** The {@link Game} is created and who created it awaits for another participant. */
    AWAITS(Stage.IN_PROGRESS),
    ;
    /** The next available tests. */
    private final Stage next;
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
}
