package com.dhsrocha.kata.tictactoe.helper;

import static java.util.Objects.requireNonNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.dhsrocha.kata.tictactoe.feature.auth.Auth;
import com.dhsrocha.kata.tictactoe.feature.game.Game;
import com.dhsrocha.kata.tictactoe.feature.turn.Turn;
import java.net.URI;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public abstract class BaseEndpointTest {

  private static final String TYPE = "TIC_TAC_TOE";

  protected @Autowired MockMvc mvc;

  protected UUID idFrom(@NonNull final ResultActions res) {
    final var location = res.andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
    final var uri = URI.create(requireNonNull(location));
    return UUID.fromString(uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1));
  }

  protected ResultActions fromLocation(@NonNull final ResultActions res) throws Exception {
    final var loc = requireNonNull(res.andReturn().getResponse().getHeader(HttpHeaders.LOCATION));
    return mvc.perform(withAdmin(get(loc)));
  }

  protected final MockHttpServletRequestBuilder withAdmin(
      @NonNull final MockHttpServletRequestBuilder req) {
    return req.with(user(Auth.ADMIN).password(Auth.ADMIN));
  }

  protected final ResultActions game(@NonNull final UUID player) throws Exception {
    final var req = withAdmin(post('/' + Game.TAG));
    return mvc.perform(req.queryParam("type", TYPE).queryParam("requesterId", player.toString()));
  }

  protected final ResultActions join(@NonNull final UUID game, @NonNull final UUID requester)
      throws Exception {
    final var req = withAdmin(put('/' + Game.TAG + '/' + '{' + Game.ID + '}' + "/join", game));
    return mvc.perform(req.param("type", TYPE).param("requesterId", requester.toString()));
  }

  protected final ResultActions surrender(@NonNull final UUID game, @NonNull final UUID requester)
      throws Exception {
    final var req = withAdmin(put('/' + Game.TAG + '/' + '{' + Game.ID + '}' + "/surrender", game));
    return mvc.perform(req.param("type", TYPE).param("requesterId", requester.toString()));
  }

  protected final ResultActions close(@NonNull final UUID game, @NonNull final UUID player)
      throws Exception {
    final var req = withAdmin(delete('/' + Game.TAG + '/' + '{' + Game.ID + '}', game));
    return mvc.perform(req.queryParam("requesterId", player.toString()));
  }

  protected final ResultActions turn(
      @NonNull final UUID game, @NonNull final UUID player, final int bitboard) throws Exception {
    final var req = withAdmin(post('/' + Turn.TAG));
    return mvc.perform(
        req.queryParam(Game.ID, game.toString())
            .queryParam("requesterId", player.toString())
            .queryParam("bitboard", String.valueOf(bitboard)));
  }

  protected final void turn(@NonNull final UUID game, @NonNull final UUID player) throws Exception {
    turn(game, player, 0b0_100_000_000__000_000_000);
  }

  protected final ResultActions notOverTurn(@NonNull final UUID game, @NonNull final UUID player)
      throws Exception {
    return turn(game, player, 0b0_100_000_000__000_000_000);
  }
}
