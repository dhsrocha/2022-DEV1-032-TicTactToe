package com.dhsrocha.kata.tictactoe.feature.game;

import com.dhsrocha.kata.tictactoe.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * Handles {@link Game}'s entities in the persistence layer.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@SuppressWarnings("unused")
@Repository
interface GameRepository extends BaseRepository<Game> {}
