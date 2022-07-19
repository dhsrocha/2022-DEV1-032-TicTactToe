package com.dhsrocha.kata.tictactoe.feature.player;

import com.dhsrocha.kata.tictactoe.base.BaseService;
import com.dhsrocha.kata.tictactoe.base.Domain;
import com.dhsrocha.kata.tictactoe.feature.auth.Auth.Role;
import com.dhsrocha.kata.tictactoe.feature.player.Player.Gender;
import com.dhsrocha.kata.tictactoe.feature.player.PlayerService.Search;
import com.dhsrocha.kata.tictactoe.system.ExceptionCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Handles features related to {@link Player} concerns.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public abstract class PlayerService implements BaseService<Search, Player> {

  /**
   * Persists a {@link Player} resource.
   *
   * @param toCreate Resource to persist.
   * @return Resource's Location URI in proper header.
   */
  abstract @NonNull Player save(@NonNull final Player toCreate);

  /**
   * Updates a {@link Player} resource, if exists.
   *
   * @param playerId Resource's external identification:
   *     <ul>
   *       <li>Must belong to an existing active player.
   *     </ul>
   *
   * @param toUpdate Player attributes to update at.
   * @return No content.
   */
  abstract boolean update(@NonNull final UUID playerId, @NonNull final Player toUpdate);

  /**
   * Removes a {@link Player} resource.
   *
   * @param playerId Resource's external identification:
   *     <ul>
   *       <li>Must belong to an existing active player.
   *     </ul>
   *
   * @return No content.
   */
  abstract boolean remove(@NonNull final UUID playerId);

  /** {@inheritDoc} */
  @SuppressWarnings("unused")
  @Validated
  @Service
  @AllArgsConstructor
  private static class Impl extends PlayerService implements UserDetailsService {

    private final PlayerRepository repository;
    private final UserDetailsManager authService;

    @Override
    public UserDetails loadUserByUsername(@NonNull final String uuid)
        throws UsernameNotFoundException {
      return find(UUID.fromString(uuid))
          .map(p -> authService.loadUserByUsername(p.getExternalId().toString()))
          .orElseThrow(ExceptionCode.PLAYER_NOT_FOUND);
    }

    @Override
    public @NonNull Page<Player> find(
        @NonNull final Search criteria, @NonNull final Pageable pageable) {
      return repository.findAll(
          (r, cq, cb) ->
              cb.or(
                  cb.conjunction(),
                  cb.equal(r.get(Search.ACTIVE), criteria.active),
                  cb.like(r.get(Search.USERNAME), criteria.username + '%'),
                  cb.equal(r.get(Search.GENDER), criteria.gender),
                  cb.like(r.get(Search.EMAIL), criteria.email + '%'),
                  cb.like(r.get(Search.FIRST_NAME), criteria.firstName + '%'),
                  cb.like(r.get(Search.LAST_NAME), criteria.lastName + '%'),
                  cb.equal(r.get(Search.BIRTH_DATE), criteria.birthDate)),
          pageable);
    }

    @Override
    public Optional<Player> find(@NonNull final UUID id) {
      return repository
          .findAll(
              (r, cq, cb) ->
                  cb.and(
                      cb.equal(r.get(Search.ACTIVE), Boolean.TRUE),
                      cb.equal(r.get(Domain.EXTERNAL_ID), id)))
          .stream()
          .findFirst();
    }

    @Override
    public @NonNull Player save(@NonNull final Player toCreate) {
      final var p = repository.save(toCreate.toBuilder().active(Boolean.TRUE).build());
      final var auth = User.withUsername(p.getExternalId().toString()).authorities(Role.PLAYER);
      authService.createUser(auth.password(p.getExternalId().toString()).build());
      return p;
    }

    @Override
    public boolean update(@NonNull final UUID id, @NonNull final Player toUpdate) {
      return find(id).map(update(toUpdate)).isPresent();
    }

    private @NonNull UnaryOperator<Player> update(@NonNull final Player toUpdate) {
      return found -> {
        found.setActive(toUpdate.isActive());
        found.setUsername(toUpdate.getUsername());
        found.setGender(toUpdate.getGender());
        found.setEmail(toUpdate.getEmail());
        found.setFirstName(toUpdate.getFirstName());
        found.setLastName(toUpdate.getLastName());
        found.setBirthDate(toUpdate.getBirthDate());
        return repository.save(found);
      };
    }

    @Override
    public boolean remove(@NonNull final UUID id) {
      final var found = find(id);
      found.ifPresent(
          p -> {
            p.setActive(Boolean.FALSE);
            repository.save(p);
          });
      return found.isPresent();
    }
  }

  /**
   * DTO used as search criteria.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @Data
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Search {
    private static final String USERNAME = "username";
    private static final String ACTIVE = "active";
    private static final String GENDER = "gender";
    private static final String EMAIL = "email";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String BIRTH_DATE = "birthDate";

    private Boolean active;
    private String username;
    private Gender gender;
    private String email;
    private String firstName;
    private String lastName;
    private OffsetDateTime birthDate;
  }
}
