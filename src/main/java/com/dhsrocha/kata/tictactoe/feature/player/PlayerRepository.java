package com.dhsrocha.kata.tictactoe.feature.player;

import com.dhsrocha.kata.tictactoe.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * Handles {@link Player}'s entities in the persistence layer.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@SuppressWarnings("unused")
@Repository
interface PlayerRepository extends BaseRepository<Player> {}
