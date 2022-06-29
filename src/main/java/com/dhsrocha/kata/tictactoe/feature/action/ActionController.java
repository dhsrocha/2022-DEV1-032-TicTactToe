package com.dhsrocha.kata.tictactoe.feature.action;

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
 * Handles Action resources.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@SuppressWarnings("unused")
@Tag(name = Action.TAG, description = "Handles Action resources.")
@RestController
@RequestMapping(Action.TAG)
@AllArgsConstructor
class ActionController implements BaseController {

  private final ActionService service;

  /**
   * Retrieves a page of Action resources, based on search criteria.
   *
   * @param criteria Search criteria with corresponding entity type's attributes.
   * @param pg Pagination set of parameters.
   * @return Pagination set of Action resources.
   */
  @GetMapping
  Page<Action> find(
      @ParameterObject final ActionService.Search criteria, //
      @ParameterObject final Pageable pg) {
    return service.find(criteria, pg);
  }

  /**
   * Retrieves a Action resource, based on its external id.
   *
   * @param id Action's external identification.
   * @return Action found.
   */
  @ApiResponse(responseCode = "404", description = "Action not found.")
  @GetMapping(ID)
  Action find(@PathVariable final UUID id) {
    return service.find(id).orElseThrow(ResourceNotFoundException::new);
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
