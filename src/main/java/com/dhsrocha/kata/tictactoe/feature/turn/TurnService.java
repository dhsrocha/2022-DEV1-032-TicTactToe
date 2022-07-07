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
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
   * Creates a {@link Turn} resource and attaches it to the {@link Game} in progress the requester
   * is in.
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
      @NonNull final UUID gameId,
      @NonNull final UUID requesterId,
      @NonNull final Bitboard bitboard);

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
    UUID create(
        @NonNull final UUID gameId,
        @NonNull final UUID requester,
        @NonNull final Bitboard bitboard) {
      final var game = gameService.find(gameId).orElseThrow(ExceptionCode.GAME_NOT_FOUND);
      ExceptionCode.GAME_NOT_IN_PROGRESS.unless(game.getStage() == IN_PROGRESS);

      final var player = playerService.find(requester).orElseThrow(ExceptionCode.PLAYER_NOT_FOUND);
      PLAYER_NOT_IN_GAME.unless(game.getHome() == player || game.getAway() == player);

      final var lastRound =
          repository.findAll(turnsFrom(game), Pageable.ofSize(1)).getContent().stream().findFirst();
      TURN_LAST_SAME_PLAYER.unless(lastRound.filter(t -> t.getPlayer() == player).isEmpty());

      final var toCreate = Turn.builder().state(bitboard).game(game).player(player).build();
      final var created = repository.save(toCreate).getExternalId();
      if (gameService.calculate(game, bitboard)) {
        repository.deleteAll(repository.findAll(turnsFrom(game)).stream().toList());
      }
      return created;
    }

    private static Specification<Turn> turnsFrom(@NonNull final Game game) {
      return (r, cq, cb) -> {
        cq.orderBy(cb.desc(r.get(Domain.CREATED_AT)));
        return cb.equal(r.get(Search.GAME), game);
      };
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
