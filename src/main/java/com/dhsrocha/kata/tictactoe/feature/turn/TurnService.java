package com.dhsrocha.kata.tictactoe.feature.turn;

import static com.dhsrocha.kata.tictactoe.feature.game.Game.Stage.IN_PROGRESS;
import static com.dhsrocha.kata.tictactoe.system.ExceptionCode.PLAYER_NOT_IN_GAME;
import static com.dhsrocha.kata.tictactoe.system.ExceptionCode.TURN_LAST_SAME_PLAYER;

import com.dhsrocha.kata.tictactoe.base.Domain;
import com.dhsrocha.kata.tictactoe.feature.game.Game;
import com.dhsrocha.kata.tictactoe.feature.game.GameService;
import com.dhsrocha.kata.tictactoe.feature.player.Player;
import com.dhsrocha.kata.tictactoe.feature.player.PlayerService;
import com.dhsrocha.kata.tictactoe.system.ExceptionCode;
import com.dhsrocha.kata.tictactoe.vo.Bitboard;
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
 * Handles features related to {@link Turn} concerns.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public abstract class TurnService {

  /**
   * Retrieves a page of {@link Turn} resources, based on search criteria.
   *
   * @param criteria Search criteria with corresponding {@link Turn}'s attributes.
   * @param pageable Pagination set of parameters.
   * @return Pagination set of {@link Turn} resources.
   */
  abstract @NonNull Page<Turn> find(
      @NonNull final Search criteria, @NonNull final Pageable pageable);

  /**
   * Retrieves a {@link Turn} resource, based on its external id.
   *
   * @param turnId {@link Turn}'s external identification:
   *     <ul>
   *       <li>Must belong to an existing turn.
   *     </ul>
   *
   * @return {@link Turn} found.
   */
  abstract @NonNull Optional<Turn> find(@NonNull final UUID turnId);

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
   *       <li>Must belong to an existing active {@link Player}.
   *       <li>Must be in the sending {@link Game}.
   *       <li>Must be different from the {@link Player one} who created last {@link Turn} for the
   *           sending {@link Game}.
   *     </ul>
   *
   * @param bitboard Representation of {@link Game}'s state in {@link Bitboard} notation.
   * @return {@link Turn}'s external identification.
   */
  abstract @NonNull UUID create(
      @NonNull final UUID gameId, @NonNull final UUID requesterId, final Bitboard bitboard);

  /** {@inheritDoc} */
  @SuppressWarnings("unused")
  @Validated
  @Service
  @AllArgsConstructor
  private static class Impl extends TurnService {

    private final TurnRepository repository;
    private final GameService gameService;
    private final PlayerService playerService;

    @Override
    public @NonNull Page<Turn> find(
        @NonNull final Search criteria, @NonNull final Pageable pageable) {
      return repository.findAll(
          (r, cq, cb) ->
              cb.or(
                  cb.conjunction(),
                  cb.equal(r.get(Search.GAME), criteria.gameId),
                  cb.equal(r.get(Search.PLAYER), criteria.playerId)),
          pageable);
    }

    @Override
    public @NonNull Optional<Turn> find(@NonNull final UUID turnId) {
      return repository.findAll((r, cq, cb) -> cb.equal(r.get(Domain.EXTERNAL_ID), turnId)).stream()
          .findFirst();
    }

    @Override
    @NonNull
    UUID create(@NonNull final UUID gameId, @NonNull final UUID requester, final Bitboard bitboard) {
      final var game = gameService.find(gameId).orElseThrow(ExceptionCode.GAME_NOT_FOUND);
      ExceptionCode.GAME_NOT_IN_PROGRESS.unless(game.getStage() == IN_PROGRESS);

      final var player = playerService.find(requester).orElseThrow(ExceptionCode.PLAYER_NOT_FOUND);
      PLAYER_NOT_IN_GAME.unless(game.getHome() == player || game.getAway() == player);

      final var fromGame = repository.findAll().stream().filter(a -> a.getGame() == game);
      final var last = fromGame.max(Comparator.comparing(Domain::getCreatedAt));
      TURN_LAST_SAME_PLAYER.unless(last.filter(a -> a.getPlayer() == player).isEmpty());

      final var toCreate = Turn.builder().state(bitboard).game(game).player(player).build();
      final var created = repository.save(toCreate).getExternalId();
      gameService.calculate(game, bitboard);
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

    private static final String GAME = "game";
    private static final String PLAYER = "player";
    private final UUID gameId;
    private final UUID playerId;
  }
}
