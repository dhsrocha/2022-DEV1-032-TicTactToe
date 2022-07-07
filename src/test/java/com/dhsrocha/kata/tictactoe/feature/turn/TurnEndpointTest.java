package com.dhsrocha.kata.tictactoe.feature.turn;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
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
 * Test suite for features related to {@link Turn} domain.
 *
 * <p>It intends to load the least requirements to make the corresponding endpoints available and
 * functional.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Tag(Turn.TAG)
@DisplayName(
    "Suite to test features related to '"
        + Turn.TAG
        + "' domain, under integration testing strategy.")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
final class TurnEndpointTest {

  private static final String URI_OPEN = "/" + Game.TAG + "/" + '{' + Game.ID + '}';
  private static final String BASE = "/" + Turn.TAG;

  @Autowired MockMvc mvc;
  @Autowired BaseRepository<Player> playerRepository;

  @Nested
  @DisplayName("GET '" + BASE + "'")
  class Retrieve {
    @Test
    @DisplayName(
        "GIVEN no created resource "
            + "WHEN retrieving turn resources "
            + "THEN return an empty list.")
    void givenNoCreated_whenRetrieve_returnEmptyList() throws Exception {
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
        "GIVEN random external id " //
            + "WHEN finding turn "
            + "THEN return status 404.")
    void givenRandomId_whenRetrieve_thenReturnStatus404_TURN_NOT_FOUND() throws Exception {
      // Arrange
      final var req = get(BASE + '{' + Turn.ID + '}', UUID.randomUUID());
      // Act
      final var res = mvc.perform(req.contentType(APPLICATION_JSON).accept(APPLICATION_JSON));
      // Assert
      res.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName(
        "GIVEN in progress game " //
            + "WHEN creating a turn "
            + "THEN turn is created.")
    void givenInProgressGame_whenCreate_thenTurnIsCreated() throws Exception {
      final var opener = player().getExternalId();
      final var joiner = player().getExternalId();
      final var game = idFrom(gameFor(opener));
      joinIn(game, joiner).andExpect(status().isNoContent());
      // Act
      final var resOpener = notOverTurnFor(game, opener).andExpect(status().isCreated());
      final var resJoiner = notOverTurnFor(game, joiner).andExpect(status().isCreated());
      // Assert
      mvc.perform(get(BASE).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(APPLICATION_JSON))
          .andExpect(jsonPath("$.totalElements", is(2)));
      retrieve(resOpener)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.game.id", is(game.toString())))
          .andExpect(jsonPath("$.player.id", is(opener.toString())))
          .andExpect(jsonPath("$.id", is(notNullValue(UUID.class))))
          .andExpect(jsonPath("$.externalId").doesNotExist())
          .andExpect(jsonPath("$.createdAt", is(notNullValue(OffsetDateTime.class))))
          .andExpect(jsonPath("$.updatedAt", is(nullValue())));
      retrieve(resJoiner)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.game.id", is(game.toString())))
          .andExpect(jsonPath("$.player.id", is(joiner.toString())))
          .andExpect(jsonPath("$.id", is(notNullValue(UUID.class))))
          .andExpect(jsonPath("$.externalId").doesNotExist())
          .andExpect(jsonPath("$.createdAt", is(notNullValue(OffsetDateTime.class))))
          .andExpect(jsonPath("$.updatedAt", is(nullValue())));
    }
  }

  @Nested
  @DisplayName("POST '" + BASE + "'")
  class Create {
    @Test
    @DisplayName(
        "GIVEN in progress game " //
            + "AND away bitboard as winning one "
            + "WHEN creating a turn "
            + "THEN return status HTTP 204 "
            + "AND away as winner for the sending game.")
    void givenInProgressGame_andHomeWinningBitboard_whenCreate_thenReturnHomeAsWinnerForGame()
        throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      final var joiner = player().getExternalId();
      final var res = gameFor(opener);
      final var game = idFrom(res);
      joinIn(game, joiner).andExpect(status().isNoContent());
      // Act
      notOverTurnFor(game, opener).andExpect(status().isCreated());
      turnFor(game, joiner, 0b0_000_101_010__111_000_000).andExpect(status().isNoContent());
      retrieve(res)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.winner.id", is(joiner.toString())));
    }

    @Test
    @DisplayName(
        "GIVEN in progress game " //
            + "WHEN creating a turn with random game id "
            + "THEN return exception with code GAME_NOT_FOUND.")
    void givenInProgressGame_whenCreate_withRandomGameId_thenReturn409_GAME_NOT_FOUND()
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
            + "WHEN creating a turn "
            + "THEN return exception with code GAME_NOT_IN_PROGRESS.")
    void givenFinishedGame_whenCreate_thenReturn409_GAME_NOT_IN_PROGRESS() throws Exception {
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
            + "WHEN creating a turn with random player id "
            + "THEN return exception with code PLAYER_NOT_FOUND.")
    void givenInProgressGame_and2Players_whenCreate_wRandomPlayerId_thenReturn404_PLAYER_NOT_FOUND()
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
            + "WHEN creating a turn "
            + "THEN return exception with code PLAYER_NOT_IN_GAME.")
    void givenInProgressGame_and3Players_whenCreate_thenReturn409_PLAYER_NOT_IN_GAME()
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
            + "AND previous turns done by opener, joiner and then opener again "
            + "WHEN creating a turn "
            + "THEN return exception with code Turn_LAST_SAME_PLAYER.")
    void givenInProgressGame_andPreviousTurns_whenCreate_thenReturn409_Turn_LAST_SAME_PLAYER()
        throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      final var joiner = player().getExternalId();
      final var game = idFrom(gameFor(opener));
      joinIn(game, joiner).andExpect(status().isNoContent());

      notOverTurnFor(game, opener).andExpect(status().isCreated());
      notOverTurnFor(game, joiner).andExpect(status().isCreated());
      notOverTurnFor(game, opener).andExpect(status().isCreated());
      // Act - Assert
      notOverTurnFor(game, opener).andExpect(status().isConflict());
      notOverTurnFor(game, joiner).andExpect(status().isCreated());
    }

    @Test
    @DisplayName(
        "GIVEN two games in progress with turns "
            + "WHEN create winning turn for one of them "
            + "THEN return HTTP status 204 for it "
            + "AND all corresponding turns are removed."
            + "AND the unrelated ones are preserved")
    void given2GamesWithTurns_whenCreateWinTurn_thenReturn204_andRelatedRemoved_andUnrelatedAreNot()
        throws Exception {
      // Arrange
      final var opener1 = player().getExternalId();
      final var joiner1 = player().getExternalId();
      final var game1 = idFrom(gameFor(opener1));
      joinIn(game1, joiner1).andExpect(status().isNoContent());
      notOverTurnFor(game1, joiner1);

      final var opener2 = player().getExternalId();
      final var joiner2 = player().getExternalId();
      final var game2 = idFrom(gameFor(opener2));
      joinIn(game2, joiner2).andExpect(status().isNoContent());
      final var toPreserve1 = notOverTurnFor(game2, opener2).andExpect(status().isCreated());
      final var toPreserve2 = notOverTurnFor(game2, joiner2).andExpect(status().isCreated());
      final var toPreserve3 = notOverTurnFor(game2, opener2).andExpect(status().isCreated());
      // Act
      turnFor(game1, opener1, 0b0_100_010_001__000_100_100).andExpect(status().isNoContent());
      // Assert
      retrieve(toPreserve3).andExpect(status().isOk());
      final var hasItems =
          hasItems(
              idFrom(toPreserve1).toString(),
              idFrom(toPreserve2).toString(),
              idFrom(toPreserve3).toString());
      mvc.perform(get(BASE))
          .andExpect(jsonPath("$.totalElements", is(3)))
          .andExpect(jsonPath("$.content..id", hasItems));
    }
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
    return turnFor(game, player, 0b0_100_000_000__000_000_000);
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
    return mvc.perform(get(Objects.requireNonNull(location)));
  }

  private UUID idFrom(@NonNull final ResultActions res) {
    final var location = res.andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
    final var uri = URI.create(Objects.requireNonNull(location));
    return UUID.fromString(uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1));
  }
}
