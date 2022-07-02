package com.dhsrocha.kata.tictactoe.feature.game;

import static com.dhsrocha.kata.tictactoe.feature.game.Game.Stage.AWAITS;
import static com.dhsrocha.kata.tictactoe.feature.game.Game.Stage.IN_PROGRESS;
import static com.dhsrocha.kata.tictactoe.system.ExceptionCode.PLAYER_NOT_IN_GAME;

import com.dhsrocha.kata.tictactoe.base.Domain;
import com.dhsrocha.kata.tictactoe.feature.player.Player;
import com.dhsrocha.kata.tictactoe.feature.player.PlayerService;
import com.dhsrocha.kata.tictactoe.vo.Bitboard;
import com.dhsrocha.kata.tictactoe.system.ExceptionCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
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
 * Handles features related to {@link Game} concerns.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public abstract class GameService {

  /**
   * Process game result according to corresponding bitboard.
   *
   * @param game A game to process on:
   *     <ul>
   *       <li>Must be in {@link Game.Stage#IN_PROGRESS}.
   *     </ul>
   *
   * @param bitboard Bitboard sent from some action.
   */
  public abstract void calculate(@NonNull final Game game, @NonNull final Bitboard bitboard);

  /**
   * Retrieves a {@link Game} resource, based on its external id.
   *
   * @param id {@link Game}'s external identification.
   * @return {@link Game} found.
   */
  public abstract @NonNull Optional<Game> find(@NonNull final UUID id);

  /**
   * Retrieves a page of {@link Game} resources, based on search criteria.
   *
   * @param criteria Search criteria with corresponding {@link Game}'s attributes.
   * @param pageable Pagination set of parameters.
   * @return Pagination set of {@link Game} resources.
   */
  abstract @NonNull Page<Game> find(
      @NonNull final Search criteria, @NonNull final Pageable pageable);

  /**
   * Opens a {@link Game}, by creating it as a resource, and adds the requesting {@link Player} as
   * the home one.
   *
   * @param type {@link Type} of game to create.
   * @param requesterId {@link Player Requester}'s external identification:
   *     <ul>
   *       <li>Must belong to an existing {@link Player}.
   *       <li>Must not be in an {@link Game.Stage#AWAITS} or {@link Game.Stage#IN_PROGRESS} {@link
   *           Game}.
   *     </ul>
   *
   * @return {@link Game}'s external identification.
   */
  abstract @NonNull UUID open(@NonNull final Type type, @NonNull final UUID requesterId);

  /**
   * Joins a {@link Player} to an awaiting game and sets it to the next stage.
   *
   * @param gameId {@link Game}'s external identification:
   *     <ul>
   *       <li>Must exist it existing {@link Game}.
   *       <li>Must be in the {@link Game.Stage#AWAITS}.
   *     </ul>
   *
   * @param requesterId Requesting {@link Player}'s external identification:
   *     <ul>
   *       <li>Must belong to an existing {@link Player}.
   *       <li>Must not be in the sending {@link Game}.
   *       <li>Must not be in an ongoing {@link Game}.
   *     </ul>
   */
  abstract void join(@NonNull final UUID gameId, @NonNull final UUID requesterId);

  /**
   * Closes an awaiting {@link Game}.
   *
   * @param gameId {@link Game}'s external identification:
   *     <ul>
   *       <li>Must belong to an existing {@link Game}.
   *       <li>Must be in the {@link Game.Stage#IN_PROGRESS}.
   *     </ul>
   *
   * @param requesterId Requesting {@link Player}'s external identification:
   *     <ul>
   *       <li>Must belong to an existing active {@link Player}.
   *       <li>Must be in the sending {@link Game}.
   *     </ul>
   */
  abstract void close(@NonNull final UUID gameId, @NonNull final UUID requesterId);

  /**
   * Requesting {@link Player} gives up the sending {@link Game} and sets the opponent as winner.
   *
   * @param gameId {@link Game}'s external identification:
   *     <ul>
   *       <li>Must exist it existing {@link Game}.
   *       <li>Must be in the {@link Game.Stage#IN_PROGRESS}.
   *     </ul>
   *
   * @param requesterId Requesting {@link Player}'s external identification:
   *     <ul>
   *       <li>Must belong to an existing {@link Player}.
   *       <li>Must be in the sending {@link Game}.
   *     </ul>
   */
  abstract void surrender(@NonNull final UUID gameId, @NonNull final UUID requesterId);

  /** {@inheritDoc} */
  @SuppressWarnings("unused")
  @Validated
  @Service
  @AllArgsConstructor
  private static class Impl extends GameService {

    private final GameRepository repository;
    private final PlayerService playerService;

    @Override
    public @NonNull Page<Game> find(
        @NonNull final Search criteria, @NonNull final Pageable pageable) {
      return repository.findAll(
          (r, cq, cb) ->
              cb.or(
                  cb.conjunction(),
                  cb.equal(r.get(Search.TYPE), criteria.type),
                  cb.equal(r.get(Search.STAGE), criteria.stage),
                  cb.equal(r.get(Search.HOME), criteria.homeId),
                  cb.equal(r.get(Search.AWAY), criteria.awayId),
                  cb.equal(r.get(Search.WINNER), criteria.winnerId)),
          pageable);
    }

    @Override
    public @NonNull Optional<Game> find(@NonNull final UUID id) {
      return repository.findOne((r, cq, cb) -> cb.equal(r.get(Domain.EXTERNAL_ID), id));
    }

    @Override
    public @NonNull UUID open(@NonNull final Type type, @NonNull final UUID requesterId) {
      final var opt = playerService.find(requesterId);
      final var player = opt.orElseThrow(ExceptionCode.PLAYER_NOT_FOUND);

      final var games = repository.findAll();
      final Predicate<Game> isOngoing = g -> g.getStage() == AWAITS || g.getStage() == IN_PROGRESS;
      final Predicate<Game> isIn =
          g -> g.getHome() == player || null != g.getAway() && g.getAway() == player;
      ExceptionCode.PLAYER_IN_AN_ONGOING_GAME.unless(games.stream().noneMatch(isOngoing.and(isIn)));

      final var toCreate = Game.builder().type(type).stage(AWAITS).home(player);
      return repository.save(toCreate.build()).getExternalId();
    }

    @Override
    public void calculate(@NonNull final Game game, @NonNull final Bitboard bitboard) {
      repository.save(game.resultFrom(bitboard));
    }

    @Override
    void join(@NonNull final UUID gameId, @NonNull final UUID requesterId) {
      final var game = find(gameId).orElseThrow(ExceptionCode.GAME_NOT_FOUND);
      ExceptionCode.GAME_NOT_IN_AWAITS.unless(game.getStage() == AWAITS);

      final var opt = playerService.find(requesterId);
      final var player = opt.orElseThrow(ExceptionCode.PLAYER_NOT_FOUND);
      ExceptionCode.PLAYER_ALREADY_IN_GAME.unless(game.getHome() != player);

      final var games = repository.findAll();
      final Predicate<Game> isOngoing = g -> g.getStage() == AWAITS || g.getStage() == IN_PROGRESS;
      final Predicate<Game> isIn =
          g -> g.getHome() == player || null != g.getAway() && g.getAway() == player;
      ExceptionCode.PLAYER_IN_AN_ONGOING_GAME.unless(games.stream().noneMatch(isOngoing.and(isIn)));

      game.setAway(player);
      game.setStage(game.getStage().getNext());
      repository.save(game);
    }

    @Override
    void surrender(@NonNull final UUID gameId, @NonNull final UUID requesterId) {
      final var game = find(gameId).orElseThrow(ExceptionCode.GAME_NOT_FOUND);
      ExceptionCode.GAME_NOT_IN_PROGRESS.unless(game.getStage() == IN_PROGRESS);

      final var opt = playerService.find(requesterId);
      final var player = opt.orElseThrow(ExceptionCode.PLAYER_NOT_FOUND);
      PLAYER_NOT_IN_GAME.unless(game.getHome() == player || game.getAway() == player);

      repository.save(game.finish(player, Boolean.TRUE));
    }

    @Override
    void close(@NonNull final UUID gameId, @NonNull final UUID requesterId) {
      final var game = find(gameId).orElseThrow(ExceptionCode.GAME_NOT_FOUND);
      ExceptionCode.GAME_NOT_IN_PROGRESS.unless(game.getStage() == AWAITS);

      final var opt = playerService.find(requesterId);
      final var player = opt.orElseThrow(ExceptionCode.PLAYER_NOT_FOUND);
      PLAYER_NOT_IN_GAME.unless(game.getHome() == player);

      repository.delete(game);
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

    private static final String TYPE = "type";
    private static final String STAGE = "stage";
    private static final String HOME = "home";
    private static final String AWAY = "away";
    private static final String WINNER = "winner";
    private final Type type;
    private final Game.Stage stage;
    private final UUID homeId;
    private final UUID awayId;
    private final UUID winnerId;
  }
}
