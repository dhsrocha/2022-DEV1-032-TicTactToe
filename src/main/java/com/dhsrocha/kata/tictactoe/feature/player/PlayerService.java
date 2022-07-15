package com.dhsrocha.kata.tictactoe.feature.player;

import com.dhsrocha.kata.tictactoe.base.BaseService;
import com.dhsrocha.kata.tictactoe.base.Domain;
import com.dhsrocha.kata.tictactoe.feature.auth.Auth.Role;
import com.dhsrocha.kata.tictactoe.feature.email.EmailService;
import com.dhsrocha.kata.tictactoe.feature.player.PlayerService.Search;
import com.dhsrocha.kata.tictactoe.system.ExceptionCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

  /**
   * Activates an inactive {@link Player} if the sent confirmation code matches to the cached one at
   * record creation.
   *
   * @param playerId Player's id who wants to be activated.
   * @param token Confirmation which must be equal to the cached one to let the operation succeed.
   * @return If operation is successfully activated. Evicts the related stored cache afterwards.
   */
  abstract boolean enable(@NonNull final UUID playerId, @NonNull final int token);

  /** {@inheritDoc} */
  @SuppressWarnings("unused")
  @Validated
  @Service
  @AllArgsConstructor
  private static class Impl extends PlayerService implements UserDetailsService {

    private final PlayerRepository repository;
    private final EmailService emailService;
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
                  cb.like(r.get(Search.USERNAME), criteria.username + '%')),
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
      final var token = tokenOf(p);
      ExceptionCode.PLAYER_ALREADY_IN_GAME.unless(emailService.send(p, token));
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
      return toggle(id, Boolean.FALSE);
    }

    @Override
    @CacheEvict(key = "{#id}")
    boolean enable(@NonNull final UUID id, final int token) {
      return toggle(id, Boolean.TRUE);
    }

    private boolean toggle(@NonNull final UUID id, final boolean active) {
      final var found = find(id);
      found.ifPresent(
          p -> {
            p.setActive(active);
            repository.save(p);
          });
      return found.isPresent();
    }

    @Cacheable(key = "{#player.externalId}")
    private int tokenOf(@NonNull final Player player) {
      return ThreadLocalRandom.current().nextInt(100_000, 999_999);
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
    private Boolean active;
    private String username;
  }
}
