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
import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public abstract class BaseEndpointTest {

  protected static final DateTimeFormatter EXPECTED_FORMAT =
      DateTimeFormatter.ofPattern(StdDateFormat.DATE_FORMAT_STR_ISO8601);

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

  protected final MockHttpServletRequestBuilder withAuth(
      @NonNull final UUID uuid, @NonNull final MockHttpServletRequestBuilder req) {
    return req.with(user(uuid.toString()).password(uuid.toString()));
  }

  protected final ResultActions game(@NonNull final UUID player) throws Exception {
    return mvc.perform(withAuth(player, post('/' + Game.TAG)).queryParam("type", TYPE));
  }

  protected final ResultActions join(@NonNull final UUID player, @NonNull final UUID game)
      throws Exception {
    return mvc.perform(
        withAuth(player, put('/' + Game.TAG + '/' + '{' + Game.ID + '}' + "/join", game)));
  }

  protected final ResultActions surrender(@NonNull final UUID player, @NonNull final UUID game)
      throws Exception {
    return mvc.perform(
        withAuth(player, put('/' + Game.TAG + '/' + '{' + Game.ID + '}' + "/surrender", game)));
  }

  protected final ResultActions close(@NonNull final UUID player, @NonNull final UUID game)
      throws Exception {
    return mvc.perform(withAuth(player, delete('/' + Game.TAG + '/' + '{' + Game.ID + '}', game)));
  }

  protected final ResultActions turn(
      @NonNull final UUID player, @NonNull final UUID game, final int bitboard) throws Exception {
    final var req = withAuth(player, post('/' + Turn.TAG));
    return mvc.perform(
        req.queryParam(Game.ID, game.toString()).queryParam("bitboard", String.valueOf(bitboard)));
  }

  protected final void turn(@NonNull final UUID player, @NonNull final UUID game) throws Exception {
    turn(player, game, 0b0_100_000_000__000_000_000);
  }

  protected final ResultActions notOverTurn(@NonNull final UUID player, @NonNull final UUID game)
      throws Exception {
    return turn(player, game, 0b0_100_000_000__000_000_000);
  }
}
