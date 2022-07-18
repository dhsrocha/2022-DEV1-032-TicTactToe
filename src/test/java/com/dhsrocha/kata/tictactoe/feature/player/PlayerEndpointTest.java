package com.dhsrocha.kata.tictactoe.feature.player;

import static java.time.OffsetDateTime.now;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
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
import com.dhsrocha.kata.tictactoe.feature.turn.Turn;
import com.dhsrocha.kata.tictactoe.helper.BaseEndpointTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.annotation.DirtiesContext;

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
final class PlayerEndpointTest extends BaseEndpointTest {

  private static final String BASE = "/" + Player.TAG;

  @Autowired ObjectMapper mapper;
  @Autowired BaseRepository<Player> repository;

  @Nested
  @DisplayName("POST '" + BASE + "'")
  class Create {
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
      final var birthDate = is(stub.getBirthDate().format(EXPECTED_FORMAT));
      mvc.perform(withAdmin(get(location)).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(APPLICATION_JSON))
          .andExpect(jsonPath("$.id", is(notNullValue(UUID.class))))
          .andExpect(jsonPath("$.externalId").doesNotExist())
          .andExpect(jsonPath("$.active", is(Boolean.TRUE)))
          .andExpect(jsonPath("$.username", is(stub.getUsername())))
          .andExpect(jsonPath("$.gender", is(stub.getGender().name())))
          .andExpect(jsonPath("$.firstName", is(stub.getFirstName())))
          .andExpect(jsonPath("$.lastName", is(stub.getLastName())))
          .andExpect(jsonPath("$.birthDate", birthDate))
          .andExpect(jsonPath("$.createdAt", is(notNullValue(OffsetDateTime.class))))
          .andExpect(jsonPath("$.updatedAt", is(nullValue())));
    }

    @Test
    @DisplayName(
        "GIVEN one created resource "
            + "AND request with same username "
            + "WHEN creating player resource "
            + "THEN return HTTP status 422.")
    void given1Created_andRequestWithSameUsername_whenCreate_thenReturnStatus422()
        throws Exception {
      // Arrange
      final var toCreateAndThenFail = PlayerTest.validStub();
      create(toCreateAndThenFail);
      final var body = mapper.writeValueAsString(toCreateAndThenFail);
      // Act
      final var req =
          post(BASE).content(body).contentType(APPLICATION_JSON).accept(APPLICATION_JSON);
      final var res = mvc.perform(withAdmin(req));
      // Assert
      res.andExpect(status().is(422)).andExpect(content().contentType(APPLICATION_JSON));
    }

    @ParameterizedTest
    @MethodSource("invalidStubs")
    @DisplayName(
        "GIVEN invalid resource body "
            + "WHEN creating player resource "
            + "THEN return HTTP status 422.")
    void givenInvalidRequest_whenCreate_thenReturnStatus422(Player invalidStub) throws Exception {
      // Arrange
      final var json = mapper.writeValueAsString(invalidStub);
      // Act
      final var req =
          post(BASE).contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(json);
      final var res = mvc.perform(withAdmin(req));
      // Assert
      res.andExpect(status().is(422)).andExpect(content().contentType(APPLICATION_JSON));
    }

    private static Stream<Player> invalidStubs() {
      return Stream.of(
          // username
          PlayerTest.validStub().toBuilder().username("player2").build(),
          // first name
          PlayerTest.validStub().toBuilder().firstName("first").build(),
          // last name
          PlayerTest.validStub().toBuilder().lastName("last").build(),
          // email
          PlayerTest.validStub().toBuilder().email("string").build(),
          // birthdate
          PlayerTest.validStub().toBuilder()
              .birthDate(OffsetDateTime.now().plusMinutes(1))
              .build());
    }
  }

  @Nested
  @DisplayName("GET '" + BASE + "'")
  class Retrieve {
    @Test
    @DisplayName(
        "GIVEN no created resource "
            + "WHEN retrieving player resources "
            + "THEN return an empty list.")
    void givenNoCreated_whenRetrieve_returnEmptyList() throws Exception {
      // Act / Assert
      mvc.perform(withAdmin(get(BASE)).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(APPLICATION_JSON))
          .andExpect(jsonPath("$.page.number", is(0)))
          .andExpect(jsonPath("$.page.size", is(20)))
          .andExpect(jsonPath("$.page.totalElements", is(0)))
          .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    @Test
    @DisplayName(
        "GIVEN three created resources "
            + "WHEN retrieving all player resources "
            + "THEN return active resources with the created identities.")
    void given3created_whenRetrieve_thenReturn3activeIdsCreated() throws Exception {
      // Arrange
      final var set =
          Set.of(PlayerTest.validStub(), PlayerTest.validStub(), PlayerTest.validStub());
      final var created = new HashSet<URI>();
      for (final var c : set) {
        created.add(create(c));
      }
      final Function<URI, String> fun =
          p -> p.getPath().substring(p.getPath().lastIndexOf('/') + 1);
      final var ids = created.stream().map(fun).toArray(String[]::new);
      // Act
      final var res =
          mvc.perform(withAdmin(get(BASE)).contentType(APPLICATION_JSON).accept(APPLICATION_JSON));
      // Assert
      final var usernames = set.stream().map(Player::getUsername).toArray();
      final var birthDate = everyItem(lessThan(now().format(EXPECTED_FORMAT)));
      res.andExpect(status().isOk())
          .andExpect(content().contentType(APPLICATION_JSON))
          .andExpect(jsonPath("$.page.number", is(0)))
          .andExpect(jsonPath("$.page.size", is(20)))
          .andExpect(jsonPath("$.page.totalElements", is(3)))
          .andExpect(jsonPath("$.content..id", hasItems(ids)))
          .andExpect(jsonPath("$.content..username", hasItems(usernames)))
          .andExpect(jsonPath("$.content..active", everyItem(is(Boolean.TRUE))))
          .andExpect(jsonPath("$.content..createdAt", birthDate))
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
      final var res =
          mvc.perform(withAdmin(req).contentType(APPLICATION_JSON).accept(APPLICATION_JSON));
      // Assert
      res.andExpect(status().isNotFound());
    }
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
        toCreate.toBuilder()
            .active(Boolean.TRUE)
            .username(modified.getUsername())
            .gender(modified.getGender())
            .firstName(modified.getFirstName())
            .lastName(modified.getLastName())
            .birthDate(modified.getBirthDate())
            .build();
    final var body = mapper.writeValueAsString(toUpdate);
    // Act
    final var req =
        put(location).content(body).contentType(APPLICATION_JSON).accept(APPLICATION_JSON);
    mvc.perform(withAdmin(req)).andExpect(status().isNoContent());
    // Assert
    final var birthDate = is(toUpdate.getBirthDate().format(EXPECTED_FORMAT));
    mvc.perform(withAdmin(get(BASE)).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$.page.totalElements", is(1)));
    mvc.perform(withAdmin(get(location)).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is(notNullValue(UUID.class))))
        .andExpect(jsonPath("$.externalId").doesNotExist())
        .andExpect(jsonPath("$.active", is(toUpdate.isActive())))
        .andExpect(jsonPath("$.username", is(toUpdate.getUsername())))
        .andExpect(jsonPath("$.gender", is(toUpdate.getGender().name())))
        .andExpect(jsonPath("$.firstName", is(toUpdate.getFirstName())))
        .andExpect(jsonPath("$.lastName", is(toUpdate.getLastName())))
        .andExpect(jsonPath("$.email", is(toUpdate.getEmail())))
        .andExpect(jsonPath("$.birthDate", birthDate))
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
    mvc.perform(withAdmin(delete(uri)).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
        .andExpect(status().isNoContent());
    // Assert
    mvc.perform(withAdmin(get(uri)).contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
        .andExpect(status().isNotFound());
    final var stream = repository.findAll().stream();
    assertEquals(1, stream.filter(p -> p.getUsername().equals(stub.getUsername())).count());
  }

  @Test
  @DisplayName(
      "GIVEN anonymous user authentication in header "
          + "WHEN perform player operations "
          + "THEN return HTTP status 401.")
  @WithAnonymousUser
  void givenAnonUserInHeader_whenOperatesPlayer_thenReturnStatus401() throws Exception {
    // Arrange
    final var all = get(BASE);
    final var one = get(BASE + '/' + '{' + Turn.ID + '}', UUID.randomUUID());
    final var create = post(BASE);
    final var update = put(BASE + '/' + '{' + Turn.ID + '}', UUID.randomUUID());
    final var remove = delete(BASE + '/' + '{' + Turn.ID + '}', UUID.randomUUID());
    // Act - Assert
    mvc.perform(all).andExpect(status().isUnauthorized());
    mvc.perform(one).andExpect(status().isUnauthorized());
    mvc.perform(create).andExpect(status().isUnauthorized());
    mvc.perform(update).andExpect(status().isUnauthorized());
    mvc.perform(remove).andExpect(status().isUnauthorized());
  }

  private URI create(@NonNull final Player toCreate) throws Exception {
    final var body = mapper.writeValueAsString(toCreate);
    final var auth =
        withAdmin(post(BASE)).content(body).contentType(APPLICATION_JSON).accept(APPLICATION_JSON);
    final var res =
        mvc.perform(auth).andExpect(status().isCreated()).andExpect(header().exists(LOCATION));
    return URI.create(Objects.requireNonNull(res.andReturn().getResponse().getHeader(LOCATION)));
  }
}
