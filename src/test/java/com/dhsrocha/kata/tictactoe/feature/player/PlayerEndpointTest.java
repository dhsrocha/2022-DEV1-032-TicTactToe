package com.dhsrocha.kata.tictactoe.feature.player;

import static java.time.OffsetDateTime.now;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dhsrocha.kata.tictactoe.helper.ConfigurationHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
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

/**
 * Test suite for features related to {@link Player} domain.
 *
 * <p>It intends to load the least requirements to make the corresponding endpoints available and
 * functional.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Tag(Player.TAG)
@DisplayName("Suite to test features related to Player domain, under integration testing strategy.")
@SpringBootTest(
    properties = { //
      "logging.level.org.springframework.transaction.interceptor=TRACE"
    })
@AutoConfigureMockMvc
@Import(ConfigurationHelper.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
final class PlayerEndpointTest {

  private static final String BASE = "/" + Player.TAG;

  @Autowired ObjectMapper mapper;
  @Autowired MockMvc mvc;

  @Test
  @DisplayName(
      "GIVEN valid request body "
          + "WHEN creating player resource "
          + "THEN resource from extracted location is inactive "
          + "AND other attributes are same as the one provided "
          + "AND created at attribute is not null "
          + "AND updated at attribute is null.")
  void validBody_whenCreating_isActive_attrsAreSame_createdNotNull_updatedNull() throws Exception {
    // Arrange
    final var stub = PlayerTest.validStub();
    // Act
    final var location = create(stub);
    // Assert
    mvc.perform(get(location).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$.id").doesNotExist())
        .andExpect(jsonPath("$.active", is(Boolean.TRUE)))
        .andExpect(jsonPath("$.username", is(stub.getUsername())))
        .andExpect(jsonPath("$.externalId", is(notNullValue(UUID.class))))
        .andExpect(jsonPath("$.createdAt", is(notNullValue(OffsetDateTime.class))))
        .andExpect(jsonPath("$.updatedAt", is(nullValue())));
  }

  @Test
  @DisplayName(
      "GIVEN one created resource "
          + "AND request with same username "
          + "WHEN creating player resource "
          + "THEN return HTTP status 422.")
  void given1Created_andRequestWithSameUsername_whenCreate_thenReturnStatus422() throws Exception {
    // Arrange
    final var toCreateAndThenFail = PlayerTest.validStub();
    create(toCreateAndThenFail);
    final var body = mapper.writeValueAsString(toCreateAndThenFail);
    // Act
    final var res =
        mvc.perform(
            post(BASE).content(body).contentType(APPLICATION_JSON).accept(APPLICATION_JSON));
    // Assert
    res.andExpect(status().is(422)).andExpect(content().contentType(APPLICATION_JSON));
  }

  @Test
  @DisplayName(
      "GIVEN invalid resource body "
          + "WHEN creating player resource "
          + "THEN return HTTP status 422.")
  void givenInvalidRequest_whenCreate_thenReturnStatus422() throws Exception {
    // Arrange
    final var invalidStub = PlayerTest.validStub().toBuilder().username("2").build();
    final var json = mapper.writeValueAsString(invalidStub);
    // Act
    final var res =
        mvc.perform(
            post(BASE).contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(json));
    // Assert
    res.andExpect(status().is(422)).andExpect(content().contentType(APPLICATION_JSON));
  }

  @Test
  @DisplayName(
      "GIVEN no created resource "
          + "WHEN retrieving player resources "
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
      "GIVEN three created resources "
          + "WHEN retrieving all player resources "
          + "THEN return resources with created identities.")
  void given3created_whenRetrieve_thenHave3idsCreated() throws Exception {
    // Arrange
    final var set = Set.of(PlayerTest.validStub(), PlayerTest.validStub(), PlayerTest.validStub());
    final var created = new HashSet<URI>();
    for (final var c : set) {
      created.add(create(c));
    }
    final Function<URI, String> fun = p -> p.getPath().substring(p.getPath().lastIndexOf('/') + 1);
    final var ids = created.stream().map(fun).toArray(String[]::new);
    // Act
    final var res = mvc.perform(get(BASE).contentType(APPLICATION_JSON).accept(APPLICATION_JSON));
    // Assert
    final var usernames = set.stream().map(Player::getUsername).toArray();
    res.andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$.number", is(0)))
        .andExpect(jsonPath("$.size", is(20)))
        .andExpect(jsonPath("$.totalElements", is(3)))
        .andExpect(jsonPath("$.content..externalId", hasItems(ids)))
        .andExpect(jsonPath("$.content..username", hasItems(usernames)))
        .andExpect(jsonPath("$.content..active", everyItem(is(Boolean.TRUE))))
        .andExpect(jsonPath("$.content..createdAt", everyItem(lessThan(now().toString()))))
        .andExpect(jsonPath("$.content..updatedAt", everyItem(nullValue())));
  }

  @Test
  @DisplayName(
      "GIVEN created player resource "
          + "WHEN updating player resource "
          + "THEN return resource with updated attributes "
          + "AND updated at attribute is not null.")
  void givenCreated_whenUpdate_thenReturnWithUpdatedAttributes_updatedAtNotNull() throws Exception {
    // Arrange
    final var toCreate = PlayerTest.validStub();
    final var modified = PlayerTest.validStub();
    final var location = create(toCreate);
    final var toUpdate =
        toCreate.toBuilder().active(modified.isActive()).username(modified.getUsername()).build();
    final var body = mapper.writeValueAsString(toUpdate);
    // Act
    final var req =
        put(location).content(body).contentType(APPLICATION_JSON).accept(APPLICATION_JSON);
    mvc.perform(req).andExpect(status().isNoContent());
    // Assert
    mvc.perform(get(BASE).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$.totalElements", is(1)));
    mvc.perform(get(location).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$.id").doesNotExist())
        .andExpect(jsonPath("$.active", is(toUpdate.isActive())))
        .andExpect(jsonPath("$.username", is(toUpdate.getUsername())))
        .andExpect(jsonPath("$.externalId", is(notNullValue(UUID.class))))
        .andExpect(jsonPath("$.createdAt", is(notNullValue(OffsetDateTime.class))))
        .andExpect(jsonPath("$.updatedAt", is(notNullValue(OffsetDateTime.class))));
  }

  @Test
  @DisplayName(
      "GIVEN created player resource "
          + "WHEN deleting the created resource "
          + "THEN return no content status "
          + "AND not found status after retrieve with id.")
  void givenCreatedStub_whenDelete_thenReturnStatus204_andNotFoundAfterGetId() throws Exception {
    // Arrange
    final var stub = create(PlayerTest.validStub());
    // Act
    mvc.perform(delete(stub).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
        .andExpect(status().isNoContent());
    // Assert
    mvc.perform(get(stub).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().string(""));
  }

  private URI create(final Player toCreate) throws Exception {
    final var body = mapper.writeValueAsString(toCreate);
    final var resp =
        mvc.perform(post(BASE).content(body).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(header().exists(HttpHeaders.LOCATION))
            .andReturn();
    return URI.create(Objects.requireNonNull(resp.getResponse().getHeader(HttpHeaders.LOCATION)));
  }
}
