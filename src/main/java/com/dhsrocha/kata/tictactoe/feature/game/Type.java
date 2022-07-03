package com.dhsrocha.kata.tictactoe.feature.game;

import static java.util.Arrays.stream;

import com.dhsrocha.kata.tictactoe.vo.Bitboard;

/**
 * Game type, holds a game's rule set to process a game in given state.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
enum Type {
  TIC_TAC_TOE {
    @Override
    public Bitboard.Result process(final long bitboard) {
      if (bitboard < 0) {
        throw new IllegalArgumentException(MSG_POSITIVE);
      }
      final var rounds = Long.bitCount(bitboard);
      if (rounds > 9) {
        throw new IllegalArgumentException(MSG_BITS);
      }
      if (rounds < 5) {
        return Bitboard.Result.NOT_OVER;
      }
      final var homeWon = stream(WIN_STATES).anyMatch(w -> w == (w & (bitboard >> 9)));
      final var awayWon = stream(WIN_STATES).anyMatch(w -> w == (w & (bitboard & (1 << 9) - 1)));
      return rounds == 9 && homeWon == awayWon
          ? Bitboard.Result.TIE
          : homeWon
              ? Bitboard.Result.HOME
              : awayWon ? Bitboard.Result.AWAY : Bitboard.Result.NOT_OVER;
    }

    /** Winning states. */
    private static final int[] WIN_STATES =
        new int[] {
          /*
           * |o o o|
           * |     |
           * |     |
           */
          0b111_000_000,
          /*
           * |     |
           * |o o o|
           * |     |
           */
          0b000_111_000,
          /*
           * |     |
           * |     |
           * |o o o|
           */
          0b000_000_111,
          /*
           * |o    |
           * |o    |
           * |o    |
           */
          0b100_100_100,
          /*
           * |  o  |
           * |  o  |
           * |  o  |
           */
          0b010_010_010,
          /*
           * |    o|
           * |    o|
           * |    o|
           */
          0b001_001_001,
          /*
           * |o    |
           * |  o  |
           * |    o|
           */
          0b100_010_001,
          /*
           * |    o|
           * |  o  |
           * |o    |
           */
          0b001_010_100
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
  abstract Bitboard.Result process(final long bitboard);
}
