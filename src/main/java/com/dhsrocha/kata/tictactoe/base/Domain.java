package com.dhsrocha.kata.tictactoe.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.PastOrPresent;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstraction for the domain layer.<br>
 *
 * <p>The following states that every implementation should:</p>
 *
 * <ul>
 *   <li>Keep identity used by persistence only be handled by application container.
 *   <li>Be traceable, with creation and last update timestamps.
 * </ul>
 *
 * <h2>Implementation Example:</h2>
 *
 * <pre>{@code
 * @javax.persistence.Entity
 * @lombok.Data
 * @lombok.NoArgsConstructor
 * @lombok.AllArgsConstructor
 * @lombok.Builder(toBuilder = true)
 * @lombok.EqualsAndHashCode(callSuper = true)
 * public class SomeDomain extends Domain implements Comparable<SomeDomain> {
 *
 *   // (...)
 *
 * }
 * }</pre>
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@MappedSuperclass
@Data
@Setter(AccessLevel.NONE)
public abstract class Domain {

  /** Record's primary key. Meant to be hidden from the outside world. */
  @Schema(hidden = true)
  @Getter(AccessLevel.NONE)
  private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

  /** Surrogate key. Meant to be handled by the outside world. */
  @Schema(
      name = "id",
      description = "Surrogate key. Meant to be handled by the outside world.",
      accessMode = Schema.AccessMode.READ_ONLY,
      example = "ed3d3509-cd6e-4a21-a021-f6826440935a")
  @JsonProperty(value = "id", access = Access.READ_ONLY)
  @Column(name = "external_id", nullable = false, unique = true)
  private UUID externalId;

  /** Record's persist timestamp. Meant to not be updatable. */
  @Schema(
      description = "Record's persist timestamp. Meant to not be updatable.",
      accessMode = Schema.AccessMode.READ_ONLY,
      example = "1986-08-19T10:20:00.000Z")
  @PastOrPresent
  @Column(name = "created_at", nullable = false)
  @JsonProperty(access = Access.READ_ONLY)
  private OffsetDateTime createdAt;

  /** Record's last update timestamp. Null as initial value. */
  @Schema(
      description = "Record's last update timestamp. Null as initial value.",
      accessMode = Schema.AccessMode.READ_ONLY,
      example = "1986-08-19T10:20:00.000Z")
  @PastOrPresent
  @Column(name = "updated_at", nullable = false)
  @JsonProperty(access = Access.READ_ONLY)
  private OffsetDateTime updatedAt;

  /** Sorting criteria from {@link Domain} attributes to provide for its implementations. */
  protected static final Comparator<Domain> DOMAIN_COMPARATOR =
      Comparator.nullsLast(Comparator.comparing(Domain::getExternalId))
          .thenComparing(Comparator.nullsLast(Comparator.comparing(Domain::getUpdatedAt)))
          .thenComparing(Comparator.nullsLast(Comparator.comparing(Domain::getCreatedAt)));

  /**
   * Generates values for {@link Domain#externalId} and {@link Domain#createdAt} on creation.
   *
   * @see PrePersist
   */
  @SuppressWarnings("unused")
  @PrePersist
  final void idAndCreatedAtOnPersist() {
    externalId = UUID.randomUUID();
    createdAt = OffsetDateTime.now();
    updatedAt = null;
  }

  /**
   * Generates current timestamp for {@link Domain#updatedAt} on update.
   *
   * @see PreUpdate
   */
  @SuppressWarnings("unused")
  @PreUpdate
  final void updatedAtOnUpdate() {
    updatedAt = OffsetDateTime.now();
  }
}
