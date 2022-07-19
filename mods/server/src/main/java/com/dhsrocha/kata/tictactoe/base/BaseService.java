package com.dhsrocha.kata.tictactoe.base;

import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Standard {@link Service} operations for {@link Domain} implementations.
 *
 * @param <S> Data transfer object used to hold search criteria parameters.
 * @param <D> A representing {@link Domain domain} in the system.
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface BaseService<S, D extends Domain> {

  /**
   * Retrieves a page of {@link Domain} resources, based on search criteria.
   *
   * @param criteria Search criteria with corresponding entity type's attributes.
   * @param pageable Pagination set of parameters.
   * @return Pagination set of {@link Domain} resources.
   */
  @NonNull
  Page<D> find(@NonNull final S criteria, @NonNull final Pageable pageable);

  /**
   * Retrieves an extending {@link Domain} resource, based on its external id.
   *
   * @param playerId Resource's external identification:
   *     <ul>
   *       <li>Must belong to an existing record.
   *     </ul>
   *
   * @return {@link Domain} implementation found.
   */
  @NonNull
  Optional<D> find(@NonNull final UUID playerId);
}
