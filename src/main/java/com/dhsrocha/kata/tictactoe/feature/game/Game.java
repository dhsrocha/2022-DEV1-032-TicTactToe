package com.dhsrocha.kata.tictactoe.feature.game;

import com.dhsrocha.kata.tictactoe.base.Domain;
import com.dhsrocha.kata.tictactoe.feature.action.Action;
import com.dhsrocha.kata.tictactoe.feature.player.Player;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Comparator;
import java.util.HashSet;
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

  /** Current life-cycle stage. */
  @Schema(description = "Current life-cycle stage.")
  @Column(nullable = false)
  private final @NonNull @NotNull Stage stage;
  /** Home player. */
  @Schema(description = "Home player.")
  @ManyToOne
  private final @NotNull Player home;
  /** Away player. */
  @Schema(description = "Away player.")
  @ManyToOne
  private final @NotNull Player away;
  /** {@link Game}'s winner. */
  @Schema(description = "Game winner.")
  @ManyToOne
  private final Player winner;
  /** Actions occurred in the current game. */
  @Schema(description = "Actions occurred in the current game.")
  @OneToMany(cascade = CascadeType.ALL)
  private final @NotNull @Builder.Default Set<@NotNull Action> actions = new HashSet<>();

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
}
