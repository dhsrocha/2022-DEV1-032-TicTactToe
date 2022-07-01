package com.dhsrocha.kata.tictactoe.feature.player;

import com.dhsrocha.kata.tictactoe.base.Domain;
import com.dhsrocha.kata.tictactoe.feature.game.Game;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import java.util.Comparator;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.Singular;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Represents a person who plays a {@link Game}.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Schema(description = "Represents a person who plays a Game.")
@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = "username"),
    indexes = {@Index(name = "idx_username", columnList = "username", unique = true)})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter(AccessLevel.PACKAGE)
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class Player extends Domain implements Comparable<Player> {

  /** Domain tag to use on endpoint paths and OpenAPI grouping. */
  public static final String TAG = "players";
  /** Id to use on {@link PathVariable}'s {@link URI}. */
  public static final String ID = "playerId";
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
  private boolean active;
  /** Human-readable identification. */
  @Schema(
      description = "Human-readable unique identification.",
      pattern = "^[a-z][a-z_]{1,19}$",
      example = "diego_rocha")
  @Size(min = MIN_LENGTH, max = MAX_LENGTH)
  @Pattern(regexp = "^[a-z][a-z_]{1,19}$")
  @Column(nullable = false, unique = true)
  private @NonNull String username;
  /** Game being or already played. */
  @Schema(description = "Game being or already played.")
  @OneToMany(cascade = CascadeType.ALL)
  private final @Singular @NotNull @NotNull Set<@NotNull Game> games = Set.of();

  @Override
  public final int compareTo(@NonNull final Player toCompare) {
    return COMPARATOR.thenComparing(DOMAIN_COMPARATOR).compare(this, toCompare);
  }
}
