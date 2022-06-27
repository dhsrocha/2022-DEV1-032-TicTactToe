package com.dhsrocha.kata.tictactoe.feature.player;

import com.dhsrocha.kata.tictactoe.base.Domain;
import com.dhsrocha.kata.tictactoe.feature.game.Game;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Comparator;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;

/**
 * Represents a person who is playing a {@link Game}.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = "username"),
    indexes = {@Index(name = "idx_username", columnList = "username", unique = true)})
@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class Player extends Domain implements Comparable<Player> {

  /** Domain tag to use on endpoint paths and OpenAPI grouping. */
  public static final String TAG = "players";
  /** Minimum string length. */
  private static final int MIN_LENGTH = 2;
  /** Maximum string length. */
  private static final int MAX_LENGTH = 20;
  /** Comparison criteria. */
  private static final Comparator<Player> COMPARATOR =
      Comparator.comparing(Player::isActive)
          .thenComparing(Player::getUsername)
          .thenComparing(p -> p.games.size());

  /** Indicates allowance to perform actions on the system. */
  @Schema(description = "Indicates allowance to perform actions on the system.", example = "false")
  @Column(nullable = false)
  private final boolean active;
  /** Human-readable identification. */
  @Schema(
      description = "Human-readable unique identification.",
      pattern = "^[a-z][a-z_]{1,19}$",
      example = "diego_rocha")
  @NotBlank
  @Size(min = MIN_LENGTH, max = MAX_LENGTH)
  @Pattern(regexp = "^[a-z][a-z_]{1,19}$")
  @Column(nullable = false, unique = true)
  private final @NotNull @lombok.NonNull String username;
  /** Game being or already played. */
  @Schema(description = "Game being or already played.")
  @OneToMany(cascade = CascadeType.ALL)
  private final @Singular @NotNull @NotNull Set<@NotNull Game> games = Set.of();

  @Override
  public final int compareTo(@lombok.NonNull final Player toCompare) {
    return COMPARATOR.thenComparing(DOMAIN_COMPARATOR).compare(this, toCompare);
  }
}
