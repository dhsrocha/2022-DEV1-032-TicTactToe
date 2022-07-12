package com.dhsrocha.kata.tictactoe.base;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;

/**
 * Standard operations and resource implementations.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public abstract class BaseController<D extends Domain> {

  protected abstract PagedResourcesAssembler<D> getAssembler();

  /**
   * Retrieves a existing Domain resource, based on its external id.
   *
   * @param id Resource's external identification:
   *     <ul>
   *       <li>Must belong to an existing record.
   *     </ul>
   *
   * @return Resource found.
   */
  protected abstract ResponseEntity<EntityModel<D>> find(@NonNull final UUID id);

  protected final ResponseEntity<PagedModel<EntityModel<D>>> hateoasOf(@NonNull final Page<D> pg) {
    final var self = linkTo(getClass()).withSelfRel();
    return ResponseEntity.ok(getAssembler().toModel(pg, self));
  }

  protected final ResponseEntity<EntityModel<D>> hateoasOf(
      @NonNull final UUID id, @NonNull final D body) {
    final var self = linkTo(methodOn(getClass()).find(id)).withSelfRel();
    return ResponseEntity.ok(EntityModel.of(body, self));
  }
}
