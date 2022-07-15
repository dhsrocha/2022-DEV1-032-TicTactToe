package com.dhsrocha.kata.tictactoe.feature.player;

import com.dhsrocha.kata.tictactoe.base.Domain;
import com.dhsrocha.kata.tictactoe.feature.game.Game;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
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
          .thenComparing(Player::getEmail)
          .thenComparing(Player::getFirstName)
          .thenComparing(Player::getLastName)
          .thenComparing(Player::getGender)
          .thenComparing(Player::getBirthDate)
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
  /** Player's chosen gender. */
  @Schema(description = "Player's chosen gender.", example = "MALE")
  @Column(nullable = false)
  private @NotNull @NonNull Gender gender;
  /** Player's main contact point. */
  @Schema(description = "Player's main contact point.", example = "dhsrocha@phabula.com")
  @Email
  @NotBlank
  @Column(nullable = false)
  private @NonNull String email;
  /** Player's first name. */
  @Schema(description = "Player's first name.", pattern = "^[A-Z][a-z]{1,19}$", example = "Diego")
  @NotBlank
  @Size(min = MIN_LENGTH, max = MAX_LENGTH)
  @Pattern(regexp = "^[A-Z][a-z]{1,19}$")
  @Column(name = "first_name", nullable = false)
  private @NonNull String firstName;
  /** Player's last name. */
  @Schema(description = "Player's last name.", pattern = "^[A-Z][a-z]{1,19}$", example = "Rocha")
  @NotBlank
  @Size(min = MIN_LENGTH, max = MAX_LENGTH)
  @Pattern(regexp = "^[A-Z][a-z]{1,19}$")
  @Column(name = "last_name", nullable = false)
  private @NonNull String lastName;
  /** Indicates player's birthdate. */
  @Schema(description = "Indicates player's birth-date.", example = "1986-08-19T10:20:00.000Z")
  @Past
  @NotNull
  @Column(name = "birth_date", nullable = false)
  private @NonNull OffsetDateTime birthDate;
  /** Game being or already played. */
  @Schema(description = "Game being or already played.")
  @OneToMany(cascade = CascadeType.ALL)
  private final @Singular @NotNull @NotNull Set<@NotNull Game> games = Set.of();

  @Override
  public final int compareTo(@NonNull final Player toCompare) {
    return COMPARATOR.thenComparing(DOMAIN_COMPARATOR).compare(this, toCompare);
  }

  /**
   * Describes a {@link Player}'s gender.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @SuppressWarnings("unused")
  @Schema(name = "PlayerGender", description = "Describes a player's gender.")
  public enum Gender {
    /** Masculine gender. */
    MALE,
    /** Feminine gender. */
    FEMALE
  }
}
