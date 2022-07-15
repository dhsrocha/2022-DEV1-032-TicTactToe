package com.dhsrocha.kata.tictactoe.feature.turn;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dhsrocha.kata.tictactoe.base.BaseRepository;
import com.dhsrocha.kata.tictactoe.feature.player.Player;
import com.dhsrocha.kata.tictactoe.feature.player.PlayerTest;
import com.dhsrocha.kata.tictactoe.helper.BaseEndpointTest;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

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
final class TurnEndpointTest extends BaseEndpointTest {

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
      mvc.perform(withAdmin(get(BASE)).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
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
            + "WHEN finding turn "
            + "THEN return status 404.")
    void givenRandomId_whenRetrieve_thenReturnStatus404_TURN_NOT_FOUND() throws Exception {
      // Arrange
      final var req = get(BASE + '{' + Turn.ID + '}', UUID.randomUUID());
      // Act
      final var res =
          mvc.perform(withAdmin(req).contentType(APPLICATION_JSON).accept(APPLICATION_JSON));
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
      final var game = idFrom(game(opener));
      join(joiner, game).andExpect(status().isNoContent());
      // Act
      final var resOpener = notOverTurn(opener, game).andExpect(status().isCreated());
      final var resJoiner = notOverTurn(joiner, game).andExpect(status().isCreated());
      // Assert
      mvc.perform(withAdmin(get(BASE)).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(APPLICATION_JSON))
          .andExpect(jsonPath("$.page.totalElements", is(2)));
      fromLocation(resOpener)
          .andExpect(status().isOk())
          .andExpect(content().contentType(APPLICATION_JSON))
          .andExpect(jsonPath("$.game.id", is(game.toString())))
          .andExpect(jsonPath("$.player.id", is(opener.toString())))
          .andExpect(jsonPath("$.id", is(notNullValue(UUID.class))))
          .andExpect(jsonPath("$.externalId").doesNotExist())
          .andExpect(jsonPath("$.createdAt", is(notNullValue(OffsetDateTime.class))))
          .andExpect(jsonPath("$.updatedAt", is(nullValue())));
      fromLocation(resJoiner)
          .andExpect(status().isOk())
          .andExpect(content().contentType(APPLICATION_JSON))
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
        "GIVEN in progress game "
            + "AND away bitboard as winning one "
            + "WHEN creating a turn "
            + "THEN return status HTTP 204 "
            + "AND away as winner for the sending game.")
    void givenInProgressGame_andHomeWinningBitboard_whenCreate_thenReturnHomeAsWinnerForGame()
        throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      final var joiner = player().getExternalId();
      final var res = game(opener);
      final var game = idFrom(res);
      join(joiner, game).andExpect(status().isNoContent());
      // Act
      notOverTurn(opener, game).andExpect(status().isCreated());
      turn(joiner, game, 0b0_000_101_010__111_000_000).andExpect(status().isNoContent());
      fromLocation(res)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.winner.id", is(joiner.toString())));
    }

    @Test
    @DisplayName(
        "GIVEN in progress game "
            + "WHEN creating a turn with random game id "
            + "THEN return exception with code GAME_NOT_FOUND.")
    void givenInProgressGame_whenCreate_withRandomGameId_thenReturn409_GAME_NOT_FOUND()
        throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      final var joiner = player().getExternalId();
      final var game = idFrom(game(opener));
      join(joiner, game).andExpect(status().isNoContent());
      // Act - Assert
      notOverTurn(opener, UUID.randomUUID()).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName(
        "GIVEN in progress game "
            + "WHEN creating a turn "
            + "THEN return exception with code GAME_NOT_IN_PROGRESS.")
    void givenFinishedGame_whenCreate_thenReturn409_GAME_NOT_IN_PROGRESS() throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      final var game = idFrom(game(opener));
      // Act - Assert
      notOverTurn(opener, game).andExpect(status().isConflict());
    }

    @Test
    @DisplayName(
        "GIVEN in progress game "
            + "WHEN creating a turn with random player id "
            + "THEN return exception with code PLAYER_NOT_FOUND.")
    void givenInProgressGame_and2Players_whenCreate_wRandomPlayerId_thenReturn404_PLAYER_NOT_FOUND()
        throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      final var joiner = player().getExternalId();
      final var game = idFrom(game(opener));
      join(joiner, game).andExpect(status().isNoContent());
      // Act - Assert
      notOverTurn(UUID.randomUUID(), game).andExpect(status().isNotFound());
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
      final var game = idFrom(game(opener));
      join(joiner, game).andExpect(status().isNoContent());
      // Act - Assert
      notOverTurn(third, game).andExpect(status().isConflict());
    }

    @Test
    @DisplayName(
        "GIVEN in progress game "
            + "AND previous turns done by opener, joiner and then opener again "
            + "WHEN creating a turn "
            + "THEN return exception with code Turn_LAST_SAME_PLAYER.")
    void givenInProgressGame_andPreviousTurns_whenCreate_thenReturn409_Turn_LAST_SAME_PLAYER()
        throws Exception {
      // Arrange
      final var opener = player().getExternalId();
      final var joiner = player().getExternalId();
      final var game = idFrom(game(opener));
      join(joiner, game).andExpect(status().isNoContent());

      notOverTurn(opener, game).andExpect(status().isCreated());
      notOverTurn(joiner, game).andExpect(status().isCreated());
      notOverTurn(opener, game).andExpect(status().isCreated());
      // Act - Assert
      notOverTurn(opener, game).andExpect(status().isConflict());
      notOverTurn(joiner, game).andExpect(status().isCreated());
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
      final var game1 = idFrom(game(opener1));
      join(joiner1, game1).andExpect(status().isNoContent());
      notOverTurn(joiner1, game1);

      final var opener2 = player().getExternalId();
      final var joiner2 = player().getExternalId();
      final var game2 = idFrom(game(opener2));
      join(joiner2, game2).andExpect(status().isNoContent());
      final var toPreserve1 = notOverTurn(opener2, game2).andExpect(status().isCreated());
      final var toPreserve2 = notOverTurn(joiner2, game2).andExpect(status().isCreated());
      final var toPreserve3 = notOverTurn(opener2, game2).andExpect(status().isCreated());
      // Act
      turn(opener1, game1, 0b0_100_010_001__000_100_100).andExpect(status().isNoContent());
      // Assert
      fromLocation(toPreserve3)
          .andExpect(status().isOk())
          .andExpect(content().contentType(APPLICATION_JSON));
      final var hasItems =
          hasItems(
              idFrom(toPreserve1).toString(),
              idFrom(toPreserve2).toString(),
              idFrom(toPreserve3).toString());
      mvc.perform(withAdmin(get(BASE)))
          .andExpect(jsonPath("$.page.totalElements", is(3)))
          .andExpect(jsonPath("$.content..id", hasItems));
    }
  }

  @Test
  @DisplayName(
      "GIVEN anonymous user authentication in header "
          + "WHEN perform turn operations "
          + "THEN return HTTP status 401.")
  @WithAnonymousUser
  void givenAnonUserInHeader_whenOperatesTurn_thenReturnStatus401() throws Exception {
    // Arrange
    final var all = get(BASE);
    final var one = get(BASE + '/' + '{' + Turn.ID + '}', UUID.randomUUID());
    final var create = post(BASE);
    // Act - Assert
    mvc.perform(all).andExpect(status().isUnauthorized());
    mvc.perform(one).andExpect(status().isUnauthorized());
    mvc.perform(create).andExpect(status().isUnauthorized());
  }

  private Player player() {
    return playerRepository.save(PlayerTest.validStub().toBuilder().active(Boolean.TRUE).build());
  }
}
