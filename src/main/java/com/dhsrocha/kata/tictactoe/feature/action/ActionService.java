package com.dhsrocha.kata.tictactoe.feature.action;

import static com.dhsrocha.kata.tictactoe.feature.game.Game.Stage.IN_PROGRESS;
import static com.dhsrocha.kata.tictactoe.system.ExceptionCode.ACTION_LAST_SAME_PLAYER;
import static com.dhsrocha.kata.tictactoe.system.ExceptionCode.PLAYER_NOT_IN_GAME;

import com.dhsrocha.kata.tictactoe.base.Domain;
import com.dhsrocha.kata.tictactoe.feature.game.Game;
import com.dhsrocha.kata.tictactoe.feature.game.GameService;
import com.dhsrocha.kata.tictactoe.feature.player.Player;
import com.dhsrocha.kata.tictactoe.feature.player.PlayerService;
import com.dhsrocha.kata.tictactoe.system.ExceptionCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Handles features related to {@link Action} concerns.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public abstract class ActionService {

  /**
   * Retrieves a page of {@link Action} resources, based on search criteria.
   *
   * @param criteria Search criteria with corresponding {@link Action}'s attributes.
   * @param pageable Pagination set of parameters.
   * @return Pagination set of {@link Action} resources.
   */
  abstract @NonNull Page<Action> find(
      @NonNull final Search criteria, @NonNull final Pageable pageable);

  /**
   * Retrieves a {@link Action} resource, based on its external id.
   *
   * @param id {@link Action}'s external identification.
   * @return {@link Action} found.
   */
  abstract @NonNull Optional<Action> find(@NonNull final UUID id);

  /**
   * Opens a Action, by creating it as a resource, and adds the requesting player as the home one.
   *
   * @param gameId {@link Game}'s external identification:
   *     <ul>
   *       <li>Must exist it existing {@link Game}.
   *       <li>Must be in the in-progress stage.
   *     </ul>
   *
   * @param requesterId Requesting {@link Player}'s external identification:
   *     <ul>
   *       <li>Must belong to an existing {@link Player}.
   *       <li>Must be in the sending {@link Game}.
   *       <li>Must be different from the {@link Player one} who created last {@link Action} for the
   *           sending {@link Game}.
   *     </ul>
   *
   * @param bitboard Representation of {@link Game}'s state in {@link Bitboard} notation.
   * @return {@link Action}'s external identification.
   */
  abstract UUID create(
      @NonNull final UUID gameId, @NonNull final UUID requesterId, final int bitboard);

  /** {@inheritDoc} */
  @SuppressWarnings("unused")
  @Validated
  @Service
  @AllArgsConstructor
  private static class Impl extends ActionService {

    private final ActionRepository repository;
    private final GameService gameService;
    private final PlayerService playerService;

    @Override
    public @NonNull Page<Action> find(
        @NonNull final Search criteria, @NonNull final Pageable pageable) {
      return repository.findAll(
          (r, cq, cb) ->
              cb.or(
                  cb.conjunction(),
                  cb.equal(r.get("game"), criteria.game),
                  cb.equal(r.get("player"), criteria.player)),
          pageable);
    }

    @Override
    public Optional<Action> find(@NonNull final UUID id) {
      return repository.findAll((r, cq, cb) -> cb.equal(r.get(Domain.EXTERNAL_ID), id)).stream()
          .findFirst();
    }

    @Override
    UUID create(@NonNull final UUID gameId, @NonNull final UUID requester, final int bitboard) {
      final var game = gameService.find(gameId).orElseThrow(ExceptionCode.GAME_NOT_FOUND);
      ExceptionCode.GAME_NOT_IN_PROGRESS.unless(game.getStage() == IN_PROGRESS);

      final var player = playerService.find(requester).orElseThrow(ExceptionCode.PLAYER_NOT_FOUND);
      PLAYER_NOT_IN_GAME.unless(game.getHome().equals(player) || game.getAway().equals(player));

      final var fromGame = repository.findAll().stream().filter(a -> a.getGame().equals(game));
      final var last = fromGame.max(Comparator.comparing(Domain::getCreatedAt));
      ACTION_LAST_SAME_PLAYER.unless(last.filter(a -> a.getPlayer().equals(player)).isEmpty());

      final var state = Bitboard.of(bitboard);
      final var toCreate = Action.builder().state(state).game(game).player(player).build();
      final var created = repository.save(toCreate).getExternalId();
      gameService.calculate(game, state);
      return created;
    }
  }

  /**
   * DTO used as search criteria.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @Data
  @Builder
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Search {
    private final UUID game;
    private final UUID player;
  }
}
