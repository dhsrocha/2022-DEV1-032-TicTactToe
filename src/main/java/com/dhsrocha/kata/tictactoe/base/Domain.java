package com.dhsrocha.kata.tictactoe.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.validation.constraints.PastOrPresent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

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
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter(AccessLevel.NONE)
@EqualsAndHashCode(callSuper = true)
public abstract class Domain extends AbstractPersistable<Long> {

  public static final String EXTERNAL_ID = "externalId";

  public static final String CREATED_AT = "createdAt";
  public static final String UPDATED_AT = "updatedAt";

  /** Sorting criteria from {@link Domain} attributes to provide for its implementations. */
  protected static final Comparator<Domain> DOMAIN_COMPARATOR =
      Comparator.comparing(Domain::isNew)
          .thenComparing(Comparator.nullsLast(Comparator.comparing(Domain::getUpdatedAt)))
          .thenComparing(Comparator.nullsLast(Comparator.comparing(Domain::getCreatedAt)));

  /** Surrogate key. Meant to be handled by the outside world. */
  @Schema(
      name = "id",
      description = "Surrogate key. Meant to be handled by the outside world.",
      accessMode = Schema.AccessMode.READ_ONLY,
      example = "ed3d3509-cd6e-4a21-a021-f6826440935a")
  @JsonProperty(access = Access.READ_ONLY)
  @Type(type = "uuid-char")
  @Column(nullable = false, unique = true, updatable = false)
  private UUID externalId;

  /** Record's persist timestamp. Meant to not be updatable. */
  @Schema(
      description = "Record's persist timestamp. Meant to not be updatable.",
      accessMode = Schema.AccessMode.READ_ONLY,
      example = "1986-08-19T10:20:00.000Z")
  @JsonProperty(access = Access.READ_ONLY)
  @PastOrPresent
  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  @DateTimeFormat(iso = ISO.DATE_TIME)
  private OffsetDateTime createdAt;

  /** Record's last update timestamp. Null as initial value. */
  @Schema(
      description = "Record's last update timestamp. Null as initial value.",
      accessMode = Schema.AccessMode.READ_ONLY,
      example = "1986-08-19T10:20:00.000Z")
  @JsonProperty(access = Access.READ_ONLY)
  @PastOrPresent
  @LastModifiedDate
  @Column(name = "updated_at")
  @DateTimeFormat(iso = ISO.DATE_TIME)
  private OffsetDateTime updatedAt;

  @Schema(hidden = true)
  @JsonIgnore
  @Override
  public Long getId() {
    return super.getId();
  }

  @Schema(hidden = true)
  @JsonIgnore
  @Override
  public boolean isNew() {
    return super.isNew();
  }

  /** Generates values for {@link #externalId} and {@link #createdAt}. */
  @SuppressWarnings("unused")
  @PrePersist
  final void prePersist() {
    externalId = UUID.randomUUID();
  }
}
