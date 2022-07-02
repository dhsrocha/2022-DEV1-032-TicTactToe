package com.dhsrocha.kata.tictactoe.feature.game;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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
import com.dhsrocha.kata.tictactoe.helper.ConfigurationHelper;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;
import lombok.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
@DisplayName("Suite to test features related to Game domain, under integration testing strategy.")
@SpringBootTest(
    properties = { //
      "logging.level.org.springframework.transaction.interceptor=TRACE"
    })
@AutoConfigureMockMvc
@Import(ConfigurationHelper.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
final class GameEndpointTest {

  private static final String BASE = "/" + Game.TAG;

  @Autowired MockMvc mvc;
  @Autowired BaseRepository<Player> playerRepository;

  @Test
  @DisplayName(
      "GIVEN no created resource "
          + "WHEN retrieving game resources "
          + "THEN return an empty list.")
  void givenNoCreated_whenRetrieving_returnEmptyList() throws Exception {
    // Act / Assert
    mvc.perform(get(BASE).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$.number", is(0)))
        .andExpect(jsonPath("$.size", is(20)))
        .andExpect(jsonPath("$.totalElements", is(0)))
        .andExpect(jsonPath("$.content", hasSize(0)));
  }

  @Test
  @DisplayName(
      "GIVEN a persisted player "
          + "WHEN opening a game "
          + "THEN return Game is created "
          + "AND stage is awaits.")
  void givenCreatedPlayer_whenOpening_thenReturnGameIsCreated_andStageIsAwaits() throws Exception {
    // Act
    final var res = openFor(player().getExternalId()).andExpect(status().isCreated());
    // Assert
    retrieve(res).andExpect(jsonPath("$.stage", is(Game.Stage.AWAITS.name())));
  }

  @Test
  @DisplayName(
      "WHEN opening a game " //
          + "THEN return exception with code PLAYER_NOT_FOUND.")
  void whenOpening_thenReturn404_PLAYER_NOT_FOUND() throws Exception {
    // Act - Assert
    openFor(UUID.randomUUID()).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName(
      "GIVEN persisted game " //
          + "WHEN opening a game "
          + "THEN return exception with code PLAYER_ALREADY_IN_GAME.")
  void givenOpenedGame_whenOpeningAgain_thenReturn409_PLAYER_ALREADY_IN_GAME() throws Exception {
    // Arrange
    final var created = player().getExternalId();
    openFor(created).andExpect(status().isCreated());
    // Act - Assert
    openFor(created).andExpect(status().isConflict());
  }

  @Test
  @DisplayName(
      "GIVEN 2 persisted players " //
          + "WHEN joining in a game "
          + "THEN game is in progress.")
  void given2Players_whenJoining_requesterIsJoinedToGame() throws Exception {
    // Arrange
    final var opener = player();
    final var res = openFor(opener.getExternalId()).andExpect(status().isCreated());
    final var game = idFrom(res);
    // Act
    final var joiner = player();
    joinOrSurrender(game, joiner.getExternalId(), GameController.JOIN)
        .andExpect(status().isNoContent());
    // Assert
    retrieve(res).andExpect(jsonPath("$.stage", is(Game.Stage.IN_PROGRESS.name())));
  }

  @Test
  @DisplayName(
      "WHEN joining in a game with random game id "
          + "THEN return exception with code GAME_NOT_FOUND.")
  void whenJoiningRandomGameId_thenReturnStatus409_GAME_NOT_FOUND() throws Exception {
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
  void givenOpenedGame_whenJoining_thenReturn409_GAME_NOT_IN_AWAITS() throws Exception {
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
  void givenOpenedGame_whenJoiningRandomPlayerId_thenReturnStatus409_PLAYER_NOT_FOUND()
      throws Exception {
    // Arrange
    final var game = idFrom(openFor(player().getExternalId()).andExpect(status().isCreated()));
    // Act - Assert
    joinOrSurrender(game, UUID.randomUUID(), GameController.JOIN).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName(
      "GIVEN joined game " //
          + "WHEN joining in the already joined game "
          + "THEN return exception with code PLAYER_ALREADY_IN_GAME.")
  void givenJoinedGame_whenJoiningAlreadyJoined_thenReturnStatus409_PLAYER_ALREADY_IN_GAME()
      throws Exception {
    // Arrange
    final var opener = player().getExternalId();
    final var joiner = player().getExternalId();
    final var res = openFor(opener);
    final var game = idFrom(res.andExpect(status().isCreated()));
    joinOrSurrender(game, joiner, GameController.JOIN).andExpect(status().isNoContent());
    // Act - Assert
    joinOrSurrender(game, opener, GameController.JOIN).andExpect(status().isConflict());
    joinOrSurrender(game, joiner, GameController.JOIN).andExpect(status().isConflict());
  }

  @Test
  @DisplayName(
      "GIVEN 2 opened games " //
          + "WHEN joining in the other one "
          + "THEN return exception with code PLAYER_IN_AN_ONGOING_GAME.")
  void given2OpenedGames_whenJoiningToOther_thenReturnStatus409_PLAYER_IN_AN_ONGOING_GAME()
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

  @Test
  @DisplayName(
      "GIVEN game with in progress state "
          + "WHEN surrendering this game "
          + "THEN return finished game "
          + "AND winner is the opponent.")
  void givenInProgressGame_whenSurrendering_thenReturnFinishedGame_andWinnerIsOpponent()
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
    retrieve(res).andExpect(jsonPath("$.winner.externalId", is(opener.toString())));
  }

  @Test
  @DisplayName(
      "GIVEN game with in progress state "
          + "WHEN surrendering this game "
          + "THEN return exception with code GAME_NOT_FOUND.")
  void givenInProgressGame_whenSurrendering_withRandomGameId_thenReturnStatus409_GAME_NOT_FOUND()
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
  void givenFinishedGame_whenSurrendering_thenReturnStatus409_GAME_NOT_IN_PROGRESS()
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
  void givenInProgressGame_whenSurrenderingWithRandomPlayer_thenReturnStatus409_PLAYER_NOT_FOUND()
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
  void givenInProgressGame_and3Players_whenSurrendering_thenReturnStatus409_PLAYER_NOT_IN_GAME()
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

  @Test
  @DisplayName(
      "GIVEN game in awaiting stage "
          + "WHEN closing a game "
          + "THEN return status HTTP 204 "
          + "AND this is no longer found.")
  void givenAwaitingGame_whenClosing_thenReturnStatus204_andGameIsNotFound() throws Exception {
    // Arrange
    final var opener = player().getExternalId();
    final var game = openFor(opener);
    // Act
    final var closed = close(idFrom(game), opener);
    // Assert
    closed.andExpect(status().isNoContent());
    retrieve(game).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName(
      "GIVEN game in progress stage "
          + "WHEN closing a game with random game id "
          + "THEN return status HTTP 404 with code GAME_NOT_FOUND.")
  void givenInProgressGame_whenClosingWithRandomGameId_thenReturnStatus409_GAME_NOT_FOUND()
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
  void givenInProgressGame_whenClosing_thenReturnStatus409_GAME_NOT_IN_AWAITS() throws Exception {
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
  void givenOpeningGame_whenClosingWithAnotherPlayer_thenReturnStatus409_PLAYER_NOT_FOUND()
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
  void givenAwaitingGame_andAnotherPlayer_whenClosing_thenReturnStatus409_PLAYER_NOT_IN_GAME()
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
