package com.dhsrocha.kata.tictactoe.feature.turn;

import com.dhsrocha.kata.tictactoe.base.BaseController;
import com.dhsrocha.kata.tictactoe.system.ExceptionCode;
import com.dhsrocha.kata.tictactoe.vo.Bitboard;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URL;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpHeaders;
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
@Getter
@AllArgsConstructor
class TurnController extends BaseController<Turn> {

  private final PagedResourcesAssembler<Turn> assembler;
  private final TurnService service;

  /**
   * Retrieves a page of Turn resources, based on search criteria.
   *
   * @param criteria Search criteria with corresponding entity type's attributes.
   * @param pageable Pagination set of parameters.
   * @return Paginated set of resources, with additional information about it.
   */
  @ApiResponse(responseCode = "200", description = "Turn page is retrieved.")
  @GetMapping
  ResponseEntity<PagedModel<EntityModel<Turn>>> find(
      @ParameterObject final TurnService.Search criteria,
      @ParameterObject final Pageable pageable) {
    return hateoasOf(service.find(criteria, pageable));
  }

  /**
   * Retrieves a Turn resource, based on its external id.
   *
   * @param turnId Resource's external identification.
   * @return Resource found.
   */
  @ApiResponse(responseCode = "200", description = "Turn is found.")
  @ApiResponse(responseCode = "404", description = "Turn not found.", content = @Content)
  @GetMapping('{' + Turn.ID + '}')
  @Override
  protected ResponseEntity<EntityModel<Turn>> find(@PathVariable(Turn.ID) final UUID turnId) {
    return hateoasOf(turnId, service.find(turnId).orElseThrow(ExceptionCode.GAME_NOT_FOUND));
  }

  /**
   * Creates a Turn resource and attaches it to the Game in progress the requester is in.
   *
   * @param gameId Game's external identification:
   *     <ul>
   *       <li>Must belong to an existing game.
   *       <li>Must be in the in-progress stage.
   *     </ul>
   *
   * @param requesterId Requesting player's external identification:
   *     <ul>
   *       <li>Must belong to an existing active player.
   *       <li>Must be in the sending game.
   *       <li>Must be different from the one who created last action for the sending game.
   *     </ul>
   *
   * @param bitboard Representation of Game's state in bitboard notation.
   * @return Resource's location URI in proper header, if not finished the game.
   */
  @ApiResponse(
      content = @Content,
      responseCode = "201",
      description = "Turn created.",
      headers =
          @Header(
              name = HttpHeaders.LOCATION,
              description = "Resource's location, if sending game is not finished.",
              schema = @Schema(implementation = URL.class)))
  @ApiResponse(
      content = @Content,
      responseCode = "204",
      description = "Turn finished the sending Game.")
  @ApiResponse(content = @Content, responseCode = "404", description = "Game not found.")
  @ApiResponse(
      content = @Content,
      responseCode = "409",
      description = "Game is not in in progress stage.")
  @ApiResponse(
      content = @Content,
      responseCode = "404",
      description = "Requesting player is not found.")
  @ApiResponse(
      content = @Content,
      responseCode = "409",
      description = "Requesting player is not in the sending game.")
  @ApiResponse(
      content = @Content,
      responseCode = "409",
      description = "Same requesting player in game's last action.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  ResponseEntity<?> create(
      @RequestParam final UUID gameId,
      @RequestParam final UUID requesterId,
      @RequestParam final long bitboard) {
    final var created = service.create(gameId, requesterId, Bitboard.of(bitboard));
    if (created.isPresent()) {
      final var uri = ServletUriComponentsBuilder.fromCurrentRequest();
      final var location = uri.pathSegment(String.valueOf(created.get())).build().toUri();
      return ResponseEntity.created(location).build();
    }
    return ResponseEntity.noContent().build();
  }
}
