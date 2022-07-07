package com.dhsrocha.kata.tictactoe.feature.game;

import com.dhsrocha.kata.tictactoe.base.Domain;
import com.dhsrocha.kata.tictactoe.feature.player.Player;
import com.dhsrocha.kata.tictactoe.feature.turn.Turn;
import com.dhsrocha.kata.tictactoe.system.ExceptionCode;
import com.dhsrocha.kata.tictactoe.vo.Bitboard;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import java.util.Comparator;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.Singular;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Holds a match between two {@link Player} entities.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Schema(description = "Holds a match between two Player entities.")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter(AccessLevel.PACKAGE)
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class Game extends Domain implements Comparable<Game> {

  /** Domain tag to use on endpoint paths and OpenAPI grouping. */
  public static final String TAG = "games";
  /** Id to use on {@link PathVariable}'s {@link URI}. */
  public static final String ID = "gameId";
  /** Comparison criteria. */
  private static final Comparator<Game> COMPARATOR =
      Comparator.comparing(Game::getStage)
          .thenComparing(Game::getWinner)
          .thenComparing(Game::getHome)
          .thenComparing(Game::getAway)
          .thenComparing(g -> g.turns.size());

  /** Game's type. Holds its rules in it. */
  @Schema(description = "Game's type. Holds its rules in it.", example = "TIC_TAC_TOE")
  @Column(nullable = false)
  private @NonNull @NotNull Type type;
  /** Current {@link Game}'s life-cycle stage. Starts as {@link Stage#AWAITS}. */
  @Schema(description = "Current Game's life-cycle stage. Starts as Stage#AWAITS.")
  @Column(nullable = false)
  private @NonNull @NotNull Stage stage;
  /** Home player. Holds the {@link Game} and awaits for another one as {@link #away}. */
  @Schema(description = "Home player. Holds the Game and awaits for another one as away.")
  @ManyToOne
  private @NonNull @NotNull Player home;
  /** Away player. Joins the {@link Game} as the {@link #home}'s opponent. */
  @Schema(description = "Away player. Joins the Game as the home's opponent.")
  @ManyToOne
  private Player away;
  /** {@link Game}'s winner. Null until finish it to determine which {@link Player} has won. */
  @Schema(description = "Game's winner. Null until finish it to determine which Player has won.")
  @ManyToOne
  private Player winner;
  /** Actions occurred in the current game. */
  @Schema(description = "Actions occurred in the current game.")
  @OneToMany(cascade = CascadeType.ALL)
  private final @Singular @NotNull @NonNull Set<@NotNull Turn> turns = Set.of();

  @Override
  public final int compareTo(@NonNull final Game toCompare) {
    return COMPARATOR.thenComparing(DOMAIN_COMPARATOR).compare(this, toCompare);
  }

  /**
   * Finishes this {@link Game} and awards a {@link Player} as its winner.
   *
   * @param lastRound The player who did the last {@link Turn}:
   *     <ul>
   *       <li>Must be enrolled to the game as {@link #home} or {@link #away}.
   *     </ul>
   *
   * @return The invoking instance.
   */
  public final @NonNull Game finish(@NonNull final Player lastRound) {
    return finish(lastRound, Boolean.FALSE);
  }

  /**
   * Finishes this {@link Game} and awards a {@link Player} as its winner.
   *
   * @param lastRound The player who did the last {@link Turn}:
   *     <ul>
   *       <li>Must be enrolled to the game as {@link #home} or {@link #away}.
   *     </ul>
   *
   * @param surrendered Victory is given to the opponent if the last round's player surrenders.
   * @return The invoking instance.
   */
  public final @NonNull Game finish(@NonNull final Player lastRound, final boolean surrendered) {
    ExceptionCode.GAME_NOT_IN_PROGRESS.unless(Stage.IN_PROGRESS == stage);
    ExceptionCode.PLAYER_NOT_IN_GAME.unless(home == lastRound || null != away && away == lastRound);
    setWinner(surrendered ^ home == lastRound ? home : away == lastRound ? away : home);
    setStage(Stage.FINISHED);
    return this;
  }

  /**
   * Process results of a {@link Bitboard}, according to provided {@link Type}.
   *
<   * @param bitboard Bitboard holding a state.
   * @return Indicates if the instance's result corresponds to its end.
   */
  final @NonNull boolean resultFrom(@NonNull final Bitboard bitboard) {
    ExceptionCode.GAME_NOT_IN_PROGRESS.unless(stage == Stage.IN_PROGRESS);
    final var result = type.process(bitboard);
    if (null != away && result.isFinished()) {
      finish(Bitboard.Result.HOME == result ? home : away);
    }
    if (null != away && Bitboard.Result.NOT_OVER != result) {
      setStage(Game.Stage.FINISHED);
    }
    return result.isFinished();
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
