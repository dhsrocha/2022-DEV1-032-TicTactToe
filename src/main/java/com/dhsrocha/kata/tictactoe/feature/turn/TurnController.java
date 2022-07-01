package com.dhsrocha.kata.tictactoe.feature.turn;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Represents a event which depicts the state of a Game in time.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@SuppressWarnings("unused")
@Tag(name = Turn.TAG, description = "Represents a event which depicts the state of a Game in time.")
@RestController
@RequestMapping(Turn.TAG)
@AllArgsConstructor
class TurnController implements BaseController {

  private final TurnService service;

  /**
   * Retrieves a page of Action resources, based on search criteria.
   *
   * @param criteria Search criteria with corresponding entity type's attributes.
   * @param pg Pagination set of parameters.
   * @return Paginated set of resources, with additional information about it.
   */
  @GetMapping
  Page<Turn> find(
      @ParameterObject final TurnService.Search criteria, //
      @ParameterObject final Pageable pg) {
    return service.find(criteria, pg);
  }

  /**
   * Retrieves a Action resource, based on its external id.
   *
   * @param turnId Resource's external identification.
   * @return Resource found.
   */
  @ApiResponse(responseCode = "404", description = "Action not found.")
  @GetMapping('{' + Turn.ID + '}')
  Turn find(@PathVariable(Turn.ID) final UUID turnId) {
    return service.find(turnId).orElseThrow(ResourceNotFoundException::new);
  }

  /**
   * Opens a Action, by creating it as a resource, and adds the requesting player as the home one.
   *
   * @param gameId Game's external identification:
   *     <ul>
   *       <li>Must exist.
   *       <li>Must be in the in-progress stage.
   *     </ul>
   *
   * @param requesterId Requesting player's external identification:
   *     <ul>
   *       <li>Must belong to an existing player.
   *       <li>Must be in the sending game.
   *       <li>Must be different from the one who created last action for the sending game.
   *     </ul>
   *
   * @param bitboard Representation of Game's state in bitboard notation.
   * @return Resource's location URI in proper header.
   */
  @ApiResponse(responseCode = "404", description = "Game not found.")
  @ApiResponse(responseCode = "409", description = "Game is not in in progress stage.")
  @ApiResponse(responseCode = "404", description = "Requesting player is not found.")
  @ApiResponse(responseCode = "409", description = "Requesting player is not in the sending game.")
  @ApiResponse(responseCode = "409", description = "Same requesting player in game's last action.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  ResponseEntity<?> create(
      @RequestParam final UUID gameId,
      @RequestParam final UUID requesterId,
      @RequestParam final int bitboard) {
    final var created = service.create(gameId, requesterId, bitboard);
    final var uri = ServletUriComponentsBuilder.fromCurrentRequest();
    final var location = uri.pathSegment(String.valueOf(created)).build().toUri();
    return ResponseEntity.created(location).build();
  }
}
