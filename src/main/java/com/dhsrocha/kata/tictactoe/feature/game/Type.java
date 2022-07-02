package com.dhsrocha.kata.tictactoe.feature.game;

import static java.util.Arrays.stream;

/**
 * Game type, holds a game's rule set to process a game in given state.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
enum Type {
  TIC_TAC_TOE {
    @Override
    public Game.Result process(final long bitboard) {
      if (bitboard < 0) {
        throw new IllegalArgumentException(MSG_POSITIVE);
      }
      final var rounds = Long.bitCount(bitboard);
      if (rounds > 9) {
        throw new IllegalArgumentException(MSG_BITS);
      }
      if (rounds < 5) {
        return Game.Result.NOT_OVER;
      }
      final var homeWon = stream(WIN_STATES).anyMatch(w -> w == (w & (bitboard >> 9)));
      final var awayWon = stream(WIN_STATES).anyMatch(w -> w == (w & (bitboard & (1 << 9) - 1)));
      return rounds == 9 && homeWon == awayWon
          ? Game.Result.TIE
          : homeWon ? Game.Result.HOME : awayWon ? Game.Result.AWAY : Game.Result.NOT_OVER;
    }

    /** Winning states. */
    private static final int[] WIN_STATES =
        new int[] {
          /*
           * |o o o|
           * |     |
           * |     |
           */
          0b111000000,
          /*
           * |     |
           * |o o o|
           * |     |
           */
          0b000111000,
          /*
           * |     |
           * |     |
           * |o o o|
           */
          0b000000111,
          /*
           * |o    |
           * |o    |
           * |o    |
           */
          0b100100100,
          /*
           * |  o  |
           * |  o  |
           * |  o  |
           */
          0b010010010,
          /*
           * |    o|
           * |    o|
           * |    o|
           */
          0b001001001,
          /*
           * |o    |
           * |  o  |
           * |    o|
           */
          0b100010001,
          /*
           * |    o|
           * |  o  |
           * |o    |
           */
          0b001010100
        };
  },
  ;

  private static final String MSG_POSITIVE = "Bitboard must be positive or zero";
  private static final String MSG_BITS = "Bitboard bit count must be 18 bits at most";

  /**
   * Calculates if an bitboard has a winning state, according to the provide rule set.
   *
   * @param bitboard The boards' state in bitboard notation.
   * @return A outgoing result of the state.
   */
  abstract Game.Result process(final long bitboard);
}
