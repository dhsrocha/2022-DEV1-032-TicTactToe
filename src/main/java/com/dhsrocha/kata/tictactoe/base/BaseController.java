package com.dhsrocha.kata.tictactoe.base;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

/**
 * Standard {@link Controller} operations for {@link Domain} implementations.
 *
 * @param <S> Data transfer object used to hold search criteria parameters.
 * @param <D> A representing {@link Domain domain} in the system.
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public abstract class BaseController<S, D extends Domain> {

  /** Spring's paged resources assembler for the domain used in the . */
  protected abstract PagedResourcesAssembler<D> getAssembler();

  /** Basic abstraction for {@link Service} instances, with some finding operations. */
  protected abstract BaseService<S, D> getService();

  /**
   * Retrieves a page of Domain resources, based on search criteria.
   *
   * @param criteria Search criteria with corresponding entity type's attributes.
   * @param page Pagination set of parameters.
   * @return Paginated set of resources, with additional information about it.
   */
  protected ResponseEntity<PagedModel<EntityModel<D>>> find(final S criteria, final Pageable page) {
    return hateoasOf(getService().find(criteria, page));
  }

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

  protected final ResponseEntity<EntityModel<D>> hateoasOf(
      @NonNull final UUID id, @NonNull final D body) {
    final var self = linkTo(methodOn(getClass()).find(id)).withSelfRel();
    return ResponseEntity.ok(EntityModel.of(body, self));
  }

  private ResponseEntity<PagedModel<EntityModel<D>>> hateoasOf(@NonNull final Page<D> pg) {
    final var self = linkTo(getClass()).withSelfRel();
    return ResponseEntity.ok(getAssembler().toModel(pg, self));
  }
}
