package com.dhsrocha.kata.tictactoe.feature.turn;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dhsrocha.kata.tictactoe.base.BaseRepository;
import com.dhsrocha.kata.tictactoe.feature.game.Game;
import com.dhsrocha.kata.tictactoe.feature.player.Player;
import com.dhsrocha.kata.tictactoe.feature.player.PlayerTest;
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
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Test suite for features related to {@link Turn} domain.
 *
 * <p>It intends to load the least requirements to make the corresponding endpoints available and
 * functional.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Tag(Turn.TAG)
@DisplayName("Suite to test features related to turn domain, under integration testing strategy.")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
final class TurnEndpointTest {

  private static final String URI_OPEN = "/" + Game.TAG + "/" + '{' + Game.ID + '}';
  private static final String BASE = "/" + Turn.TAG;

  @Autowired MockMvc mvc;
  @Autowired BaseRepository<Player> playerRepository;

  @Test
  @DisplayName(
      "GIVEN no created resource "
          + "WHEN retrieving turn resources "
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
      "GIVEN in progress game " //
          + "WHEN creating an turn "
          + "THEN turn is created.")
  void givenInProgressGame_whenCreating_thenTurnIsCreated() throws Exception {
    final var opener = player().getExternalId();
    final var joiner = player().getExternalId();
    final var game = idFrom(gameFor(opener));
    joinIn(game, joiner).andExpect(status().isNoContent());
    // Act
    final var res = notOverTurnFor(game, opener).andExpect(status().isCreated());
    // Assert
    retrieve(res)
        .andExpect(jsonPath("$.game.externalId", is(game.toString())))
        .andExpect(jsonPath("$.player.externalId", is(opener.toString())));
  }

  @Test
  @DisplayName(
      "GIVEN in progress game " //
          + "AND away winning bitboard "
          + "WHEN creating an turn "
          + "THEN return away as winner for game.")
  void givenInProgressGame_andHomeWinningBitboard_whenCreating_thenReturnHomeAsWinnerForGame()
      throws Exception {
    // Arrange
    final var opener = player().getExternalId();
    final var joiner = player().getExternalId();
    final var res = gameFor(opener);
    final var game = idFrom(res);
    joinIn(game, joiner).andExpect(status().isNoContent());
    // Act
    notOverTurnFor(game, opener).andExpect(status().isCreated());
    turnFor(game, joiner, 0b000101010111000000).andExpect(status().isCreated());
    retrieve(res).andExpect(jsonPath("$.winner.externalId", is(joiner.toString())));
  }

  @Test
  @DisplayName(
      "GIVEN in progress game " //
          + "WHEN creating an turn with random game id "
          + "THEN return exception with code GAME_NOT_FOUND.")
  void givenInProgressGame_whenCreatingRandomGameId_thenReturn409_GAME_NOT_FOUND()
      throws Exception {
    // Arrange
    final var opener = player().getExternalId();
    final var joiner = player().getExternalId();
    final var res = gameFor(opener);
    final var game = idFrom(res);
    joinIn(game, joiner).andExpect(status().isNoContent());
    // Act - Assert
    notOverTurnFor(UUID.randomUUID(), opener).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName(
      "GIVEN in progress game " //
          + "WHEN creating an turn "
          + "THEN return exception with code GAME_NOT_IN_PROGRESS.")
  void givenFinishedGame_whenCreating_thenReturn409_GAME_NOT_IN_PROGRESS() throws Exception {
    // Arrange
    final var opener = player().getExternalId();
    final var res = gameFor(opener);
    final var game = idFrom(res);
    // Act - Assert
    notOverTurnFor(game, opener).andExpect(status().isConflict());
  }

  @Test
  @DisplayName(
      "GIVEN in progress game " //
          + "WHEN creating an turn "
          + "THEN return exception with code PLAYER_NOT_FOUND.")
  void givenInProgressGame_and2Players_whenCreatingRandomPlayerId_thenReturn404_PLAYER_NOT_FOUND()
      throws Exception {
    // Arrange
    final var opener = player().getExternalId();
    final var joiner = player().getExternalId();
    final var res = gameFor(opener);
    final var game = idFrom(res);
    joinIn(game, joiner).andExpect(status().isNoContent());
    // Act - Assert
    notOverTurnFor(game, UUID.randomUUID()).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName(
      "GIVEN in progress game " //
          + "WHEN creating an turn "
          + "THEN return exception with code PLAYER_NOT_IN_GAME.")
  void givenInProgressGame_and3Players_whenCreating_thenReturn409_PLAYER_NOT_IN_GAME()
      throws Exception {
    // Arrange
    final var opener = player().getExternalId();
    final var joiner = player().getExternalId();
    final var third = player().getExternalId();
    final var res = gameFor(opener);
    final var game = idFrom(res);
    joinIn(game, joiner).andExpect(status().isNoContent());
    // Act - Assert
    notOverTurnFor(game, third).andExpect(status().isConflict());
  }

  @Test
  @DisplayName(
      "GIVEN in progress game " //
          + "AND previous turn done by requester "
          + "WHEN creating an turn "
          + "THEN return exception with code Turn_LAST_SAME_PLAYER.")
  void givenInProgressGame_andPreviousTurn_whenCreating_thenReturn409_Turn_LAST_SAME_PLAYER()
      throws Exception {
    // Arrange
    final var opener = player().getExternalId();
    final var joiner = player().getExternalId();
    final var game = idFrom(gameFor(opener));
    joinIn(game, joiner).andExpect(status().isNoContent());
    // Act
    notOverTurnFor(game, opener).andExpect(status().isCreated());
    notOverTurnFor(game, opener).andExpect(status().isConflict());
  }

  private Player player() {
    return playerRepository.save(PlayerTest.validStub().toBuilder().active(Boolean.TRUE).build());
  }

  private ResultActions gameFor(@NonNull final UUID player) throws Exception {
    final var req = post("/" + Game.TAG);
    return mvc.perform(
        req.queryParam("type", "TIC_TAC_TOE").queryParam("requesterId", player.toString()));
  }

  private ResultActions notOverTurnFor(@NonNull final UUID game, @NonNull final UUID player)
      throws Exception {
    return turnFor(game, player, 0b0);
  }

  private ResultActions turnFor(
      @NonNull final UUID game, @NonNull final UUID player, final int bitboard) throws Exception {
    final var req = post(BASE);
    return mvc.perform(
        req.queryParam("type", "TIC_TAC_TOE")
            .queryParam("gameId", game.toString())
            .queryParam("requesterId", player.toString())
            .queryParam("bitboard", String.valueOf(bitboard)));
  }

  private ResultActions joinIn(@NonNull final UUID game, @NonNull final UUID requester)
      throws Exception {
    return mvc.perform(
        put(URI_OPEN + "/join", game)
            .param("type", "TIC_TAC_TOE")
            .param("requesterId", requester.toString()));
  }

  private ResultActions retrieve(@NonNull final ResultActions res) throws Exception {
    final var location = res.andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
    return mvc.perform(get(Objects.requireNonNull(location))).andExpect(status().isOk());
  }

  private UUID idFrom(@NonNull final ResultActions res) {
    final var location = res.andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
    final var uri = URI.create(Objects.requireNonNull(location));
    return UUID.fromString(uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1));
  }
}
