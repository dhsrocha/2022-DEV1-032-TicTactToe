package com.dhsrocha.kata.tictactoe.feature.player;

import com.dhsrocha.kata.tictactoe.base.BaseController;
import com.dhsrocha.kata.tictactoe.system.ExceptionCode;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URL;
import java.util.UUID;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Positive;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
 * Represents a person who plays a Game."
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@SuppressWarnings("unused")
@Tag(name = Player.TAG, description = "Represents a person who plays a Game.")
@RestController
@RequestMapping(Player.TAG)
@Getter
@AllArgsConstructor
class PlayerController extends BaseController<PlayerService.Search, Player> {

  private final PagedResourcesAssembler<Player> assembler;
  private final PlayerService service;

  /**
   * Retrieves a page of Player resources, based on search criteria.
   *
   * @param criteria Search criteria with corresponding entity type's attributes.
   * @param pageable Pagination set of parameters.
   * @return Paginated set of resources, with additional information about it.
   */
  @ApiResponse(responseCode = "200", description = "Player page is retrieved.")
  @GetMapping
  protected ResponseEntity<PagedModel<EntityModel<Player>>> find(
      @ParameterObject final PlayerService.Search criteria,
      @ParameterObject final Pageable pageable) {
    return super.find(criteria, pageable);
  }

  /**
   * Retrieves a Player resource, based on its external id.
   *
   * @param playerId Resource's external identification:
   *     <ul>
   *       <li>Must belong to an existing active player.
   *     </ul>
   *
   * @return Resource found.
   */
  @ApiResponse(responseCode = "200", description = "Player is found.")
  @ApiResponse(responseCode = "404", description = "Player not found.", content = @Content)
  @GetMapping('{' + Player.ID + '}')
  @Override
  protected ResponseEntity<EntityModel<Player>> find(@PathVariable(Player.ID) final UUID playerId) {
    return hateoasOf(playerId, service.find(playerId).orElseThrow(ExceptionCode.GAME_NOT_FOUND));
  }

  /**
   * Creates a Player resource.
   *
   * @param toCreate Resource to persist.
   * @return Resource's location URI in proper header.
   */
  @ApiResponse(
      responseCode = "201",
      description = "Player created.",
      content = @Content,
      headers =
          @Header(
              name = HttpHeaders.LOCATION,
              description = "Resource's location.",
              schema = @Schema(implementation = URL.class)))
  @ApiResponse(
      responseCode = "422",
      description = "Constraint violation in request body.",
      content = @Content)
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
   * @param playerId Resource's external identification:
   *     <ul>
   *       <li>Must belong to an existing active player.
   *     </ul>
   *
   * @param toUpdate Attributes to update.
   */
  @ApiResponse(responseCode = "204", description = "Updated Player.", content = @Content)
  @ApiResponse(responseCode = "404", description = "Player not found.", content = @Content)
  @ApiResponse(
      responseCode = "422",
      description = "Constraint violation in request body.",
      content = @Content)
  @PutMapping('{' + Player.ID + '}')
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void update(@PathVariable(Player.ID) final UUID playerId, @RequestBody final Player toUpdate) {
    ExceptionCode.PLAYER_NOT_FOUND.unless(service.update(playerId, toUpdate));
  }

  /**
   * Removes a Player resource, if exists.
   *
   * @param playerId Resource's external identification:
   *     <ul>
   *       <li>Must belong to an existing active player.
   *     </ul>
   */
  @ApiResponse(responseCode = "204", description = "Removed Player.", content = @Content)
  @ApiResponse(responseCode = "404", description = "Player not found.", content = @Content)
  @DeleteMapping('{' + Player.ID + '}')
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void remove(@PathVariable(Player.ID) final UUID playerId) {
    ExceptionCode.PLAYER_NOT_FOUND.unless(service.remove(playerId));
  }

  /**
   * Activates an inactive Player if the sent confirmation code matches to the cached one at record
   * creation.
   *
   * @param playerId Player's id who wants to be activated.
   * @param token Confirmation which must be equal to the cached one to let the operation succeed.
   */
  @PatchMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void activate(
      @PathVariable(Player.ID) final UUID playerId,
      @RequestParam @Positive @Digits(integer = 12, fraction = 0) final int token) {
    ExceptionCode.PLAYER_ACTIVATION_FAILED.unless(service.enable(playerId, token));
  }
}
