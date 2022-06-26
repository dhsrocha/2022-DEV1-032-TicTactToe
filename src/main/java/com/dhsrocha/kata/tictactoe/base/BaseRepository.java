package com.dhsrocha.kata.tictactoe.base;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Standard repository operations for all extending features.
 *
 * @param <D> A representing {@link Domain domain} in the system.
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@NoRepositoryBean
public interface BaseRepository<D extends Domain>
    extends JpaRepository<D, Long>, JpaSpecificationExecutor<D> {}
