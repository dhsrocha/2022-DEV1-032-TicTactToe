package com.dhsrocha.kata.tictactoe.feature.game;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dhsrocha.kata.tictactoe.base.BaseRepository;
import com.dhsrocha.kata.tictactoe.feature.player.Player;
import com.dhsrocha.kata.tictactoe.feature.player.PlayerTest;
import com.dhsrocha.kata.tictactoe.feature.turn.Turn;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Test suite for features related to {@link Game} domain.
 *
 * <p>It intends to load the least requirements to make the corresponding endpoints available and
 * functional.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Tag(Game.TAG)
@DisplayName(
    "Suite to test features related to '"
        + Game.TAG
        + "' domain, under integration testing strategy.")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
final class GameEndpointTest {

  private static final String BASE = "/" + Game.TAG;
  private static final String URI_TURN = "/" + Turn.TAG;

  @Autowired MockMvc mvc;
  @Autowired BaseRepository<Player> playerRepository;

  @Nested
  @DisplayName("GET '" + BASE + "'")
  class Retrieve {
    @Test
    @DisplayName(
        "GIVEN no created resource "
            + "WHEN retrieving game resources "
            + "THEN return an empty list.")
    void givenNoCreated_whenRetrieve_returnEmptyList() throws Exception {
      // Act / Assert
      mvc.perform(get(BASE).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(APPLICATION_JSON))
          .andExpect(jsonPath("$.page.size", is(20)))
          .andExpect(jsonPath("$.page.number", is(0)))
          .andExpect(jsonPath("$.page.totalElements", is(0)))
          .andExpect(jsonPath("$.page.totalPages", is(0)))
          .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    @Test
    @DisplayName(
        "GIVEN random external id " //
            + "WHEN finding game "
            + "THEN return status 404.")
    void givenRandomId_whenRetrieve_thenReturnStatus404_GAME_NOT_FOUND() throws Exception {
      // Arrange
      final var req = get(BASE + '{' + Player.ID + '}', UUID.randomUUID());
      // Act
      final var res = mvc.perform(req.contentType(APPLICATION_JSON).accept(APPLICATION_JSON));
      // Assert
      res.andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("POST '" + BASE + '/' + Game.TAG + "'")
  class Open {
    @Test
    @DisplayName(
        "GIVEN a persisted player "
            + "WHEN opening a game "
            + "THEN return Game is created "
            + "AND stage is awaits.")
    void givenCreatedPlayer_whenOpen_thenReturnGameIsCreated_andStageIsAwaits() throws Exception {
      // Act
      final var res = openFor(player().getExternalId()).andExpect(status().isCreated());
      // Assert
      mvc.perform(get(BASE).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(APPLICATION_JSON))
          .andExpect(jsonPath("$.page.totalElements", is(1)));
      retrieve(res)
          .andExpect(status().isOk())
          .andExpect(content().contentType(APPLICATION_JSON))
          .andExpect(jsonPath("$.stage", is(Game.Stage.AWAITS.name())))
          .andExpect(jsonPath("$.id", is(notNullValue(UUID.class))))
          .andExpect(jsonPath("$.externalId").doesNotExist())
          .andExpect(jsonPath("$.createdAt", is(notNullValue(OffsetDateTime.class))))
          .andExpect(jsonPath("$.updatedAt", is(nullValue())));
    }

    @Test
    @DisplayName(
        "WHEN opening a game " //
            + "THEN return exception with code PLAYER_NOT_FOUND.")
    void whenOpen_thenReturn404_PLAYER_NOT_FOUND() throws Exception {
      // Act - Assert
      openFor(UUID.randomUUID()).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName(
        "GIVEN persisted game " //
            + "WHEN opening a game "
            + "THEN return exception with code PLAYER_ALREADY_IN_GAME.")
    void givenOpenedGame_whenOpenAgain_thenReturn409_PLAYER_ALREADY_IN_GAME() throws Exception {
      // Arrange
      final var created = player().getExternalId();
      openFor(created).andExpect(status().isCreated());
      // Act - Assert
      openFor(created).andExpect(status().isConflict());
    }
  }

  @Nested
  @DisplayName("PUT '" + BASE + '/' + GameController.JOIN + "'")
  class Join {
    @Test
    @DisplayName(
        "GIVEN 2 persisted players " //
            + "WHEN joining in a game "
            + "THEN game is in progress.")
    void given2Players_whenJoin_requesterIsJoinedToGame() throws Exception {
      // Arrange
      final var opener = player();
      final var res = openFor(opener.getExternalId()).andExpect(status().isCreated());
      final var game = idFrom(res);
      // Act
      final var joiner = player();
      joinOrSurrender(game, joiner.getExternalId(), GameController.JOIN)
          .andExpect(status().isNoContent());
      // Assert
      retrieve(res)
          .andExpect(status().isOk())
          .andExpect(content().contentType(APPLICATION_JSON))
          .andExpect(jsonPath("$.stage", is(Game.Stage.IN_PROGRESS.name())));
    }

    @Test
    @DisplayName(
        "WHEN joining in a game with random game id "
            + "THEN return exception with code GAME_NOT_FOUND.")
    void whenJoinRandomGameId_thenReturnStatus409_GAME_NOT_FOUND() throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      final var joiner = player().getExternalId();
      openFor(opener);
      // Act - Assert
      joinOrSurrender(UUID.randomUUID(), joiner, GameController.JOIN)
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName(
        "GIVEN persisted game in opened stage"
            + "WHEN joining in this game "
            + "THEN return exception with code GAME_NOT_IN_AWAITS.")
    void givenOpenedGame_whenJoin_thenReturn409_GAME_NOT_IN_AWAITS() throws Exception {
      // Arrange
      final var game = idFrom(openFor(player().getExternalId()).andExpect(status().isCreated()));
      joinOrSurrender(game, player().getExternalId(), GameController.JOIN)
          .andExpect(status().isNoContent());
      // Act - Assert
      joinOrSurrender(game, player().getExternalId(), GameController.JOIN)
          .andExpect(status().isConflict());
    }

    @Test
    @DisplayName(
        "GIVEN opened game "
            + "WHEN joining in this game with random player id "
            + "THEN return exception with code PLAYER_NOT_FOUND.")
    void givenOpenedGame_whenJoinRandomPlayerId_thenReturnStatus409_PLAYER_NOT_FOUND()
        throws Exception {
      // Arrange
      final var game = idFrom(openFor(player().getExternalId()).andExpect(status().isCreated()));
      // Act - Assert
      joinOrSurrender(game, UUID.randomUUID(), GameController.JOIN)
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName(
        "GIVEN joined game " //
            + "WHEN joining in the already joined game "
            + "THEN return exception with code PLAYER_ALREADY_IN_GAME.")
    void givenJoinedGame_whenJoinAlreadyJoined_thenReturnStatus409_PLAYER_ALREADY_IN_GAME()
        throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      final var joiner = player().getExternalId();
      final var someone = player().getExternalId();
      final var game = idFrom(openFor(opener).andExpect(status().isCreated()));
      joinOrSurrender(game, joiner, GameController.JOIN).andExpect(status().isNoContent());
      // Act - Assert
      joinOrSurrender(game, opener, GameController.JOIN).andExpect(status().isConflict());
      joinOrSurrender(game, joiner, GameController.JOIN).andExpect(status().isConflict());
      joinOrSurrender(game, someone, GameController.JOIN).andExpect(status().isConflict());
    }

    @Test
    @DisplayName(
        "GIVEN 2 opened games " //
            + "WHEN joining in the other one "
            + "THEN return exception with code PLAYER_IN_AN_ONGOING_GAME.")
    void given2OpenedGames_whenJoinToOther_thenReturnStatus409_PLAYER_IN_AN_ONGOING_GAME()
        throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      final var joiner = player().getExternalId();
      final var res1 = openFor(opener);
      joinOrSurrender(idFrom(res1), joiner, GameController.JOIN).andExpect(status().isNoContent());
      // Act - Assert
      final var opener3 = player().getExternalId();
      final var res2 = openFor(opener3);
      joinOrSurrender(idFrom(res2), joiner, GameController.JOIN).andExpect(status().isConflict());
    }
  }

  @Nested
  @DisplayName("PUT '" + BASE + '/' + GameController.SURRENDER + "'")
  class Surrender {
    @Test
    @DisplayName(
        "GIVEN game with in progress state "
            + "WHEN surrendering this game "
            + "THEN return finished game "
            + "AND winner is the opponent.")
    void givenInProgressGame_whenSurrender_thenReturnFinishedGame_andWinnerIsOpponent()
        throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      final var joiner = player().getExternalId();
      final var res = openFor(opener);
      joinOrSurrender(idFrom(res), joiner, GameController.JOIN).andExpect(status().isNoContent());
      // Act
      joinOrSurrender(idFrom(res), joiner, GameController.SURRENDER)
          .andExpect(status().isNoContent());
      // Assert
      retrieve(res)
          .andExpect(status().isOk())
          .andExpect(content().contentType(APPLICATION_JSON))
          .andExpect(jsonPath("$.winner.id", is(opener.toString())));
    }

    @Test
    @DisplayName(
        "GIVEN game with in progress state "
            + "AND some turns created "
            + "WHEN surrendering this game "
            + "THEN return finished game "
            + "AND winner is the opponent.")
    void givenInProgressGame_andSomeTurns_whenSurrender_thenReturnFinishedGame_andNoRelatedTurns()
        throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      final var joiner = player().getExternalId();
      final var res = openFor(opener);
      final var game = idFrom(res);
      joinOrSurrender(idFrom(res), joiner, GameController.JOIN).andExpect(status().isNoContent());
      turnFor(game, opener);
      turnFor(game, joiner);
      // Act
      joinOrSurrender(idFrom(res), joiner, GameController.SURRENDER)
          .andExpect(status().isNoContent());
      // Assert
      mvc.perform(get(URI_TURN)).andExpect(jsonPath("$.page.totalElements", is(0)));
    }

    @Test
    @DisplayName(
        "GIVEN game with in progress state "
            + "WHEN surrendering this game "
            + "THEN return exception with code GAME_NOT_FOUND.")
    void givenInProgressGame_whenSurrender_withRandomGameId_thenReturnStatus409_GAME_NOT_FOUND()
        throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      final var joiner = player().getExternalId();
      final var game = openFor(opener);
      joinOrSurrender(idFrom(game), joiner, GameController.JOIN).andExpect(status().isNoContent());
      // Act - Assert
      joinOrSurrender(UUID.randomUUID(), joiner, GameController.SURRENDER)
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName(
        "GIVEN game with opened state "
            + "WHEN surrendering this game "
            + "THEN return exception with code GAME_NOT_IN_PROGRESS.")
    void givenFinishedGame_whenSurrender_thenReturnStatus409_GAME_NOT_IN_PROGRESS()
        throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      final var game = openFor(opener);
      // Act
      final var surrendered = joinOrSurrender(idFrom(game), opener, GameController.SURRENDER);
      // Assert
      surrendered.andExpect(status().isConflict());
    }

    @Test
    @DisplayName(
        "GIVEN game with in progress state "
            + "WHEN surrendering this game with random player id "
            + "THEN return exception with code PLAYER_NOT_FOUND.")
    void givenInProgressGame_whenSurrenderWithRandomPlayer_thenReturnStatus409_PLAYER_NOT_FOUND()
        throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      final var joiner = player().getExternalId();
      final var game = openFor(opener);
      joinOrSurrender(idFrom(game), joiner, GameController.JOIN).andExpect(status().isNoContent());
      // Act
      final var surrendered =
          joinOrSurrender(idFrom(game), UUID.randomUUID(), GameController.SURRENDER);
      // Assert
      surrendered.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName(
        "GIVEN game with in progress state "
            + "AND 3 players "
            + "WHEN surrendering this game "
            + "THEN return exception with code PLAYER_NOT_IN_GAME.")
    void givenInProgressGame_and3Players_whenSurrender_thenReturnStatus409_PLAYER_NOT_IN_GAME()
        throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      final var joiner = player().getExternalId();
      final var third = player().getExternalId();
      final var game = openFor(opener);
      joinOrSurrender(idFrom(game), joiner, GameController.JOIN).andExpect(status().isNoContent());
      // Act
      final var surrendered = joinOrSurrender(idFrom(game), third, GameController.SURRENDER);
      // Assert
      surrendered.andExpect(status().isConflict());
    }
  }

  @Nested
  @DisplayName("DELETE '" + BASE + "'")
  class Close {
    @Test
    @DisplayName(
        "GIVEN game in awaiting stage "
            + "WHEN closing a game "
            + "THEN return status HTTP 204 "
            + "AND this is no longer found.")
    void givenAwaitingGame_whenClose_thenReturnStatus204_andGameIsNotFound() throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      final var game = openFor(opener);
      // Act
      final var closed = close(idFrom(game), opener);
      // Assert
      closed.andExpect(status().isNoContent());
      retrieve(game)
          .andExpect(content().contentType(APPLICATION_JSON))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName(
        "GIVEN game in progress stage "
            + "WHEN closing a game with random game id "
            + "THEN return status HTTP 404 with code GAME_NOT_FOUND.")
    void givenInProgressGame_whenCloseWithRandomGameId_thenReturnStatus409_GAME_NOT_FOUND()
        throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      openFor(opener);
      // Act
      final var closed = close(UUID.randomUUID(), opener);
      // Assert
      closed.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName(
        "GIVEN game in progress stage "
            + "WHEN closing a game "
            + "THEN return status HTTP 409 with code GAME_NOT_IN_AWAITS.")
    void givenInProgressGame_whenClose_thenReturnStatus409_GAME_NOT_IN_AWAITS() throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      final var joiner = player().getExternalId();
      final var game = openFor(opener);
      joinOrSurrender(idFrom(game), joiner, GameController.JOIN);
      // Act
      final var closed = close(idFrom(game), opener);
      // Assert
      closed.andExpect(status().isConflict());
    }

    @Test
    @DisplayName(
        "GIVEN game in awaiting stage "
            + "WHEN closing a game with another player "
            + "THEN return status HTTP 404 with code PLAYER_NOT_FOUND.")
    void givenOpeningGame_whenClose_wAnotherPlayer_thenReturnStatus409_PLAYER_NOT_FOUND()
        throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      final var game = openFor(opener);
      // Act
      final var closed = close(idFrom(game), UUID.randomUUID());
      // Assert
      closed.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName(
        "GIVEN game in awaiting stage "
            + "AND some other active player "
            + "WHEN closing a game "
            + "THEN return status HTTP 404 with code PLAYER_NOT_IN_GAME.")
    void givenAwaitingGame_andAnotherPlayer_whenClose_thenReturnStatus409_PLAYER_NOT_IN_GAME()
        throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      final var another = player().getExternalId();
      final var game = openFor(opener);
      // Act
      final var closed = close(idFrom(game), another);
      // Assert
      closed.andExpect(status().isConflict());
    }
  }

  private Player player() {
    return playerRepository.save(PlayerTest.validStub().toBuilder().active(Boolean.TRUE).build());
  }

  private ResultActions openFor(@NonNull final UUID player) throws Exception {
    final var type = Type.TIC_TAC_TOE.name();
    final var req = post(BASE);
    return mvc.perform(req.queryParam("type", type).queryParam("requesterId", player.toString()));
  }

  private ResultActions close(@NonNull final UUID game, @NonNull final UUID player)
      throws Exception {
    final var req = delete(BASE + '/' + '{' + Game.ID + '}', game);
    return mvc.perform(req.queryParam("requesterId", player.toString()));
  }

  private ResultActions joinOrSurrender(
      @NonNull final UUID game, @NonNull final UUID requester, @NonNull final String uri)
      throws Exception {
    return mvc.perform(
        put(BASE + "/" + uri, game)
            .param("type", Type.TIC_TAC_TOE.name())
            .param("requesterId", requester.toString()));
  }

  private void turnFor(@NonNull final UUID game, @NonNull final UUID player) throws Exception {
    final var req = post(BASE);
    mvc.perform(
        req.queryParam("type", "TIC_TAC_TOE")
            .queryParam("gameId", game.toString())
            .queryParam("requesterId", player.toString())
            .queryParam("bitboard", String.valueOf(0b0_100_000_000__000_000_000)));
  }

  private ResultActions retrieve(@NonNull final ResultActions res) throws Exception {
    final var location = res.andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
    return mvc.perform(get(Objects.requireNonNull(location)));
  }

  private UUID idFrom(@NonNull final ResultActions res) {
    final var location = res.andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
    final var uri = URI.create(Objects.requireNonNull(location));
    return UUID.fromString(uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1));
  }
}
