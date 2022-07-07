package com.dhsrocha.kata.tictactoe.feature.game;

import com.dhsrocha.kata.tictactoe.system.ExceptionCode;
import com.dhsrocha.kata.tictactoe.vo.Bitboard;
import com.dhsrocha.kata.tictactoe.vo.Bitboard.Processor;
import com.dhsrocha.kata.tictactoe.vo.Bitboard.Result;
import java.util.BitSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * Game type, holds a game's rule set to process a game in given state.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@Getter(AccessLevel.PRIVATE)
@AllArgsConstructor
enum Type implements Processor {
  TIC_TAC_TOE(2, 9) {
    @Override
    public Bitboard.Result process(@NonNull final Bitboard bitboard) {
      super.validate(bitboard);
      final var rounds = Long.bitCount(bitboard.getState());
      if (rounds < 5) {
        return Bitboard.Result.NOT_OVER;
      }
      for (final var win : WIN_STATES) {
        if (win == (win & (bitboard.getState() >> TIC_TAC_TOE.getTiles()))) {
          return Result.HOME;
        }
        if (win == (win & (bitboard.getState() & (1L << TIC_TAC_TOE.getTiles()) - 1))) {
          return Result.AWAY;
        }
      }
      return rounds == TIC_TAC_TOE.getTiles() ? Bitboard.Result.TIE : Bitboard.Result.NOT_OVER;
    }

    /** Winning states. */
    private static final int[] WIN_STATES =
        new int[] {
          0b0__111_000_000,
          0b0__000_111_000,
          0b0__000_000_111,
          0b0__100_100_100,
          0b0__010_010_010,
          0b0__001_001_001,
          0b0__100_010_001,
          0b0__001_010_100
        };
  },
  ;
  /** Depicts possible non-empty states the {@link Game} can support. */
  private final int states;
  /** Measures the board's size in tiles. */
  private final int tiles;

  private void validate(final Bitboard bitboard) {
    final var state = bitboard.getState();
    ExceptionCode.BITBOARD_UNSET_STATE.unless(state != 0);
    final var set = BitSet.valueOf(new long[] {state});
    ExceptionCode.BITBOARD_EXCESSIVE_BITS.unless(set.cardinality() <= tiles);
    final var home = set.get(0, getTiles());
    final var away = set.get(getTiles(), getTiles() * getStates());
    ExceptionCode.BITBOARD_PIECE_IN_SAME_TILE.unless(!home.intersects(away));
  }
}
