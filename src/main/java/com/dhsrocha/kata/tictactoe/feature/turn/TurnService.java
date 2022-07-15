package com.dhsrocha.kata.tictactoe.feature.turn;

import static com.dhsrocha.kata.tictactoe.feature.game.Game.Stage.IN_PROGRESS;
import static com.dhsrocha.kata.tictactoe.system.ExceptionCode.PLAYER_NOT_IN_GAME;
import static com.dhsrocha.kata.tictactoe.system.ExceptionCode.TURN_LAST_SAME_PLAYER;

import com.dhsrocha.kata.tictactoe.base.BaseService;
import com.dhsrocha.kata.tictactoe.base.Domain;
import com.dhsrocha.kata.tictactoe.feature.game.Game;
import com.dhsrocha.kata.tictactoe.feature.game.GameService;
import com.dhsrocha.kata.tictactoe.feature.player.Player;
import com.dhsrocha.kata.tictactoe.feature.player.PlayerService;
import com.dhsrocha.kata.tictactoe.feature.turn.TurnService.Search;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Handles features related to {@link Turn} concerns.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public abstract class TurnService implements BaseService<Search, Turn> {

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
   * @return {@link Turn}'s external identification, if not finished the game.
   */
  abstract @NonNull Optional<UUID> create(
      @NonNull final UUID gameId,
      @NonNull final UUID requesterId,
      @NonNull final Bitboard bitboard);

  /** {@inheritDoc} */
  @SuppressWarnings("unused")
  @Validated
  @Service
  @AllArgsConstructor
  private static class Impl extends TurnService {

    private static final Sort LAST_CREATED = Sort.by(Direction.DESC, Domain.CREATED_AT);

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
    Optional<UUID> create(
        @NonNull final UUID gameId,
        @NonNull final UUID requester,
        @NonNull final Bitboard bitboard) {
      final var game = gameService.find(gameId).orElseThrow(ExceptionCode.GAME_NOT_FOUND);
      ExceptionCode.GAME_NOT_IN_PROGRESS.unless(game.getStage() == IN_PROGRESS);

      final var player = playerService.find(requester).orElseThrow(ExceptionCode.PLAYER_NOT_FOUND);
      PLAYER_NOT_IN_GAME.unless(game.getHome().equals(player) || game.getAway().equals(player));

      final var turns =
          repository.findAll((r, cq, cb) -> cb.equal(r.get(Search.GAME), game), LAST_CREATED);
      turns.stream()
          .max(Comparator.comparing(Domain::getCreatedAt))
          .ifPresent(t -> TURN_LAST_SAME_PLAYER.unless(!t.getPlayer().equals(player)));

      if (gameService.calculate(game, bitboard)) {
        repository.deleteAll(turns);
        return Optional.empty();
      }

      final var toCreate = Turn.builder().state(bitboard).game(game).player(player).build();
      return Optional.of(repository.save(toCreate).getExternalId());
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
