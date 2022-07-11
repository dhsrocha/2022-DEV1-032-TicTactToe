package com.dhsrocha.kata.tictactoe.helper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.dhsrocha.kata.tictactoe.feature.game.Game;
import com.dhsrocha.kata.tictactoe.feature.turn.Turn;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

public abstract class BaseEndpointTest {

  private static final String TYPE = "TIC_TAC_TOE";

  protected @Autowired MockMvc mvc;

  protected UUID idFrom(@NonNull final ResultActions res) {
    final var location = res.andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
    final var uri = URI.create(Objects.requireNonNull(location));
    return UUID.fromString(uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1));
  }

  protected ResultActions findOne(@NonNull final ResultActions res) throws Exception {
    final var location = res.andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
    return mvc.perform(get(Objects.requireNonNull(location)));
  }

  protected ResultActions game(@NonNull final UUID player) throws Exception {
    final var req = post('/' + Game.TAG);
    return mvc.perform(req.queryParam("type", TYPE).queryParam("requesterId", player.toString()));
  }

  protected ResultActions join(@NonNull final UUID game, @NonNull final UUID requester)
      throws Exception {
    final var req = put('/' + Game.TAG + '/' + '{' + Game.ID + '}' + "/join", game);
    return mvc.perform(req.param("type", TYPE).param("requesterId", requester.toString()));
  }

  protected ResultActions surrender(@NonNull final UUID game, @NonNull final UUID requester)
      throws Exception {
    final var req = put('/' + Game.TAG + '/' + '{' + Game.ID + '}' + "/surrender", game);
    return mvc.perform(req.param("type", TYPE).param("requesterId", requester.toString()));
  }

  protected ResultActions close(@NonNull final UUID game, @NonNull final UUID player)
      throws Exception {
    final var req = delete('/' + Game.TAG + '/' + '{' + Game.ID + '}', game);
    return mvc.perform(req.queryParam("requesterId", player.toString()));
  }

  protected ResultActions turn(
      @NonNull final UUID game, @NonNull final UUID player, final int bitboard) throws Exception {
    final var req = post('/' + Turn.TAG);
    return mvc.perform(
        req.queryParam(Game.ID, game.toString())
            .queryParam("requesterId", player.toString())
            .queryParam("bitboard", String.valueOf(bitboard)));
  }

  protected void turn(@NonNull final UUID game, @NonNull final UUID player) throws Exception {
    turn(game, player, 0b0_100_000_000__000_000_000);
  }

  protected ResultActions notOverTurn(@NonNull final UUID game, @NonNull final UUID player)
      throws Exception {
    return turn(game, player, 0b0_100_000_000__000_000_000);
  }
}
