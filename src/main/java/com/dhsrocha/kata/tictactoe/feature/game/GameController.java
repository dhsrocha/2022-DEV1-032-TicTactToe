package com.dhsrocha.kata.tictactoe.feature.game;

import com.dhsrocha.kata.tictactoe.base.BaseController;
import com.dhsrocha.kata.tictactoe.system.ExceptionCode;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URL;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Holds a match between two Player entities.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@SuppressWarnings("unused")
@Tag(name = Game.TAG, description = "Holds a match between two Player entities.")
@RestController
@RequestMapping(Game.TAG)
@AllArgsConstructor
class GameController implements BaseController {

  static final String JOIN = '{' + Game.ID + '}' + "/join";
  static final String SURRENDER = '{' + Game.ID + '}' + "/surrender";

  private final GameService service;

  /**
   * Retrieves a page of Game resources, based on search criteria.
   *
   * @param criteria Search criteria with corresponding entity type's attributes.
   * @param pg Pagination set of parameters.
   * @return Paginated set of resources, with additional information about it.
   */
  @ApiResponse(responseCode = "200", description = "Game page is retrieved.")
  @GetMapping
  Page<Game> find(
      @ParameterObject final GameService.Search criteria, //
      @ParameterObject final Pageable pg) {
    return service.find(criteria, pg);
  }

  /**
   * Retrieves a Game resource, based on its external id.
   *
   * @param gameId Resource's external identification:
   *     <ul>
   *       <li>Must belong to an existing record.
   *     </ul>
   *
   * @return Resource found.
   */
  @ApiResponse(responseCode = "200", description = "Game is found.")
  @ApiResponse(responseCode = "404", description = "Game not found.")
  @GetMapping('{' + Game.ID + '}')
  Game find(@PathVariable(Game.ID) final UUID gameId) {
    return service.find(gameId).orElseThrow(ExceptionCode.GAME_NOT_FOUND);
  }

  /**
   * Opens a Game, by creating it as a resource and adds the requesting Player as the home one.
   *
   * @param type Type of Game to create.
   * @param requesterId Requester's external identification:
   *     <ul>
   *       <li>Must belong to an existing player.
   *       <li>Must not be in an ongoing game.
   *     </ul>
   *
   * @return Resource's location URI in proper header.
   */
  @ApiResponse(
      responseCode = "201",
      description = "Game opened.",
      headers =
          @Header(
              name = HttpHeaders.LOCATION,
              description = "Resource's location.",
              schema = @Schema(implementation = URL.class)))
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
   * Joins a Player to an awaiting Game and sets it to the next stage.
   *
   * @param gameId Game's external identification:
   *     <ul>
   *       <li>Must belong to an existing record.
   *       <li>Must be in the awaiting stage.
   *     </ul>
   *
   * @param requesterId Requesting Player's external identification:
   *     <ul>
   *       <li>Must belong to an existing player.
   *       <li>Must not be in the sending game.
   *       <li>Must not be in an ongoing game.
   *     </ul>
   */
  @ApiResponse(responseCode = "204", description = "Joined to the sending game.")
  @ApiResponse(responseCode = "404", description = "Game not found.")
  @ApiResponse(responseCode = "409", description = "Game is not in awaiting stage.")
  @ApiResponse(responseCode = "404", description = "Requesting player is not found.")
  @ApiResponse(responseCode = "409", description = "Requesting player is already in this game.")
  @ApiResponse(responseCode = "409", description = "Requesting player is in an ongoing game.")
  @PutMapping(JOIN)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void join(@PathVariable(Game.ID) final UUID gameId, @RequestParam final UUID requesterId) {
    service.join(gameId, requesterId);
  }

  /**
   * Requesting Player gives up the sending Game and sets the opponent as winner.
   *
   * @param gameId Game's external identification:
   *     <ul>
   *       <li>Must belong to an existing game.
   *       <li>Must be in the progress stage.
   *     </ul>
   *
   * @param requesterId Requesting player's external identification:
   *     <ul>
   *       <li>Must belong to an existing active player.
   *       <li>Must be in the sending game.
   *     </ul>
   */
  @ApiResponse(responseCode = "204", description = "Surrendered on sending game.")
  @ApiResponse(responseCode = "404", description = "Game not found.")
  @ApiResponse(responseCode = "409", description = "Game is not in in progress stage.")
  @ApiResponse(responseCode = "404", description = "Requesting player is not found.")
  @ApiResponse(responseCode = "409", description = "Requesting player is not in the sending game.")
  @PutMapping(SURRENDER)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void surrender(@PathVariable(Game.ID) final UUID gameId, @RequestParam final UUID requesterId) {
    service.surrender(gameId, requesterId);
  }

  /**
   * Closes an awaiting Game.
   *
   * @param gameId Game's external identification:
   *     <ul>
   *       <li>Must belong to an existing game.
   *       <li>Must be in the awaiting stage.
   *     </ul>
   *
   * @param requesterId Requesting player's external identification:
   *     <ul>
   *       <li>Must belong to an existing active Player.
   *       <li>Must be in the sending Game.
   *     </ul>
   */
  @ApiResponse(responseCode = "204", description = "Closed the sending game.")
  @ApiResponse(responseCode = "404", description = "Game not found.")
  @ApiResponse(responseCode = "409", description = "Game is not in in awaiting stage.")
  @ApiResponse(responseCode = "404", description = "Requesting player is not found.")
  @ApiResponse(responseCode = "409", description = "Requesting player is not in the sending game.")
  @DeleteMapping('{' + Game.ID + '}')
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void close(@PathVariable(Game.ID) final UUID gameId, @RequestParam final UUID requesterId) {
    service.close(gameId, requesterId);
  }
}
