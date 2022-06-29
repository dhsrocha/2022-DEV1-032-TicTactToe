package com.dhsrocha.kata.tictactoe.feature.game;

import com.dhsrocha.kata.tictactoe.base.BaseController;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Handles Game resources.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@SuppressWarnings("unused")
@Tag(name = Game.TAG, description = "Handles Game resources.")
@RestController
@RequestMapping(Game.TAG)
@AllArgsConstructor
class GameController implements BaseController {

  static final String JOIN = ID + "/join";
  static final String SURRENDER = ID + "/surrender";

  private final GameService service;

  /**
   * Retrieves a page of Game resources, based on search criteria.
   *
   * @param criteria Search criteria with corresponding entity type's attributes.
   * @param pg Pagination set of parameters.
   * @return Pagination set of Game resources.
   */
  @GetMapping
  Page<Game> find(
      @ParameterObject final GameService.Search criteria, //
      @ParameterObject final Pageable pg) {
    return service.find(criteria, pg);
  }

  /**
   * Retrieves a Game resource, based on its external id.
   *
   * @param id Game's external identification.
   * @return Game found.
   */
  @ApiResponse(responseCode = "404", description = "Game not found.")
  @GetMapping(ID)
  Game find(@PathVariable final UUID id) {
    return service.find(id).orElseThrow(ResourceNotFoundException::new);
  }

  /**
   * Opens a game, by creating it as a resource, and adds the requesting player as the home one.
   *
   * @param type Type of game to create.
   * @param requesterId Requester's external identification:
   *     <ul>
   *       <li>Must belong to an existing player.
   *       <li>Must not be in an ongoing game.
   *     </ul>
   *
   * @return Resource's Location URI in proper header.
   */
  @ApiResponse(responseCode = "404", description = "Requesting player is not found.")
  @ApiResponse(responseCode = "409", description = "Requesting player is in a ongoing game.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  ResponseEntity<?> open(@RequestParam final Type type, @RequestParam final UUID requesterId) {
    final var created = service.open(type, requesterId);
    final var uri = ServletUriComponentsBuilder.fromCurrentRequest();
    final var location = uri.pathSegment(String.valueOf(created)).build().toUri();
    return ResponseEntity.created(location).build();
  }

  /**
   * Joins a Player to an awaiting game and sets it to the next stage.
   *
   * @param id Game's external identification:
   *     <ul>
   *       <li>Must exist.
   *       <li>Must be in the awaiting stage.
   *     </ul>
   *
   * @param requesterId Requesting player's external identification:
   *     <ul>
   *       <li>Must belong to an existing player.
   *       <li>Must not be in the sending game.
   *       <li>Must not be in an ongoing game.
   *     </ul>
   */
  @ApiResponse(responseCode = "404", description = "Game not found.")
  @ApiResponse(responseCode = "409", description = "Game is not in awaiting stage.")
  @ApiResponse(responseCode = "404", description = "Requesting player is not found.")
  @ApiResponse(responseCode = "409", description = "Requesting player is already in this game.")
  @ApiResponse(responseCode = "409", description = "Requesting player is in an ongoing game.")
  @PutMapping(JOIN)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void join(@PathVariable final UUID id, @RequestParam final UUID requesterId) {
    service.join(id, requesterId);
  }

  /**
   * Requesting Player gives up the sending Game and sets the opponent as winner.
   *
   * @param id Game's external identification:
   *     <ul>
   *       <li>Must exist.
   *       <li>Must be in the progress stage.
   *     </ul>
   *
   * @param requesterId Requesting player's external identification:
   *     <ul>
   *       <li>Must belong to an existing player.
   *       <li>Must be in the sending game.
   *     </ul>
   */
  @ApiResponse(responseCode = "404", description = "Game not found.")
  @ApiResponse(responseCode = "409", description = "Game is not in in progress stage.")
  @ApiResponse(responseCode = "404", description = "Requesting player is not found.")
  @ApiResponse(responseCode = "409", description = "Requesting player is not in the sending game.")
  @PutMapping(SURRENDER)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void surrender(@PathVariable final UUID id, @RequestParam final UUID requesterId) {
    service.surrender(id, requesterId);
  }
}
