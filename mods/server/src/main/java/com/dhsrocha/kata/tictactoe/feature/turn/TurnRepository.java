package com.dhsrocha.kata.tictactoe.feature.turn;

import com.dhsrocha.kata.tictactoe.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * Handles {@link Turn}'s entities in the persistence layer.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@SuppressWarnings("unused")
@Repository
interface TurnRepository extends BaseRepository<Turn> {}
