package com.dhsrocha.kata.tictactoe.feature.player;

import com.dhsrocha.kata.tictactoe.base.BaseController;
import com.dhsrocha.kata.tictactoe.system.ExceptionCode;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Represents a person who plays a Game."
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@SuppressWarnings("unused")
@Tag(name = Player.TAG, description = "Represents a person who plays a Game.")
@RestController
@RequestMapping(Player.TAG)
@AllArgsConstructor
class PlayerController implements BaseController {

  private final PlayerService service;

  /**
   * Retrieves a page of Player resources, based on search criteria.
   *
   * @param criteria Search criteria with corresponding entity type's attributes.
   * @param pg Pagination set of parameters.
   * @return Paginated set of resources, with additional information about it.
   */
  @GetMapping
  Page<Player> find(
      @ParameterObject final PlayerService.Search criteria, //
      @ParameterObject final Pageable pg) {
    return service.find(criteria, pg);
  }

  /**
   * Retrieves a Player resource, based on its external id.
   *
   * @param playerId Resource's external identification.
   * @return Resource found.
   */
  @ApiResponse(responseCode = "404", description = "Player not found.")
  @GetMapping('{' + Player.ID + '}')
  Player find(@PathVariable(Player.ID) final UUID playerId) {
    return service.find(playerId).orElseThrow(ResourceNotFoundException::new);
  }

  /**
   * Creates a Player resource.
   *
   * @param toCreate Resource to persist.
   * @return Resource's location URI in proper header.
   */
  @ApiResponse(responseCode = "422", description = "Constraint violation in request body.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  ResponseEntity<?> create(@RequestBody final Player toCreate) {
    final var created = service.save(toCreate);
    final var uri = ServletUriComponentsBuilder.fromCurrentRequest();
    final var location = uri.pathSegment(String.valueOf(created.getExternalId())).build().toUri();
    return ResponseEntity.created(location).build();
  }

  /**
   * Updates a Player resource, if exists.
   *
   * @param playerId Resource's external identification.
   * @param toUpdate Attributes to update.
   */
  @ApiResponse(responseCode = "404", description = "Player not found.")
  @ApiResponse(responseCode = "422", description = "Constraint violation in request body.")
  @PutMapping('{' + Player.ID + '}')
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void update(@PathVariable(Player.ID) final UUID playerId, @RequestBody final Player toUpdate) {
    ExceptionCode.PLAYER_NOT_FOUND.unless(service.update(playerId, toUpdate));
  }

  /**
   * Removes a Player resource, if exists.
   *
   * @param playerId Resource's external identification.
   */
  @ApiResponse(responseCode = "404", description = "Player not found.")
  @DeleteMapping('{' + Player.ID + '}')
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void remove(@PathVariable(Player.ID) final UUID playerId) {
    ExceptionCode.PLAYER_NOT_FOUND.unless(service.remove(playerId));
  }
}
