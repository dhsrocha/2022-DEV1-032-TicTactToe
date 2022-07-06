package com.dhsrocha.kata.tictactoe.feature.player;

import static java.time.OffsetDateTime.now;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dhsrocha.kata.tictactoe.base.BaseRepository;
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
@DisplayName(
    "Suite to test features related to '"
        + Player.TAG
        + "' domain, under integration testing strategy.")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
final class PlayerEndpointTest {

  private static final String BASE = "/" + Player.TAG;

  @Autowired ObjectMapper mapper;
  @Autowired MockMvc mvc;
  @Autowired BaseRepository<Player> repository;

  @Test
  @DisplayName(
      "GIVEN valid request body "
          + "WHEN creating player resource "
          + "THEN resource from extracted location is inactive "
          + "AND other attributes are same as the one provided "
          + "AND created at attribute is not null "
          + "AND updated at attribute is null.")
  void validBody_whenCreate_isActive_attrsAreSame_createdNotNull_updatedNull() throws Exception {
    // Arrange
    final var stub = PlayerTest.validStub();
    // Act
    final var location = create(stub);
    // Assert
    mvc.perform(get(location).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$.active", is(Boolean.TRUE)))
        .andExpect(jsonPath("$.username", is(stub.getUsername())))
        .andExpect(jsonPath("$.id", is(notNullValue(UUID.class))))
        .andExpect(jsonPath("$.externalId").doesNotExist())
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
    final var req = post(BASE).content(body).contentType(APPLICATION_JSON).accept(APPLICATION_JSON);
    final var res = mvc.perform(req);
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
    final var req = post(BASE).contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(json);
    final var res = mvc.perform(req);
    // Assert
    res.andExpect(status().is(422)).andExpect(content().contentType(APPLICATION_JSON));
  }

  @Test
  @DisplayName(
      "GIVEN no created resource "
          + "WHEN retrieving player resources "
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
        .andExpect(jsonPath("$.content..id", hasItems(ids)))
        .andExpect(jsonPath("$.content..username", hasItems(usernames)))
        .andExpect(jsonPath("$.content..active", everyItem(is(Boolean.TRUE))))
        .andExpect(jsonPath("$.content..createdAt", everyItem(lessThan(now().toString()))))
        .andExpect(jsonPath("$.content..updatedAt", everyItem(nullValue())));
  }

  @Test
  @DisplayName(
      "GIVEN random external id " //
          + "WHEN finding player "
          + "THEN return status 404.")
  void givenRandomId_whenRetrieve_thenReturnStatus404_PLAYER_NOT_FOUND() throws Exception {
    // Arrange
    final var req = get(BASE + '{' + Player.ID + '}', UUID.randomUUID());
    // Act
    final var res = mvc.perform(req.contentType(APPLICATION_JSON).accept(APPLICATION_JSON));
    // Assert
    res.andExpect(status().isNotFound());
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
        toCreate.toBuilder().active(Boolean.TRUE).username(modified.getUsername()).build();
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
        .andExpect(jsonPath("$.id", is(notNullValue(UUID.class))))
        .andExpect(jsonPath("$.externalId").doesNotExist())
        .andExpect(jsonPath("$.active", is(toUpdate.isActive())))
        .andExpect(jsonPath("$.username", is(toUpdate.getUsername())))
        .andExpect(jsonPath("$.createdAt", is(notNullValue(OffsetDateTime.class))))
        .andExpect(jsonPath("$.updatedAt", is(notNullValue(OffsetDateTime.class))));
  }

  @Test
  @DisplayName(
      "GIVEN created player resource "
          + "WHEN deleting the created resource "
          + "THEN return no content status "
          + "AND not found status after retrieve with id.")
  void givenCreatedStub_whenDelete_thenReturnStatus204_andStatus404Afterwards() throws Exception {
    // Arrange
    final var stub = PlayerTest.validStub();
    final var uri = create(stub);
    // Act
    mvc.perform(delete(uri).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
        .andExpect(status().isNoContent());
    // Assert
    mvc.perform(get(uri).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
        .andExpect(status().isNotFound());
    final var stream = repository.findAll().stream();
    assertEquals(1, stream.filter(p -> p.getUsername().equals(stub.getUsername())).count());
  }

  private URI create(final Player toCreate) throws Exception {
    final var body = mapper.writeValueAsString(toCreate);
    final var req = post(BASE).content(body).contentType(APPLICATION_JSON).accept(APPLICATION_JSON);
    final var res =
        mvc.perform(req).andExpect(status().isCreated()).andExpect(header().exists(LOCATION));
    return URI.create(Objects.requireNonNull(res.andReturn().getResponse().getHeader(LOCATION)));
  }
}
