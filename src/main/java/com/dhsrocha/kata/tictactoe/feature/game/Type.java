package com.dhsrocha.kata.tictactoe.feature.game;

import com.dhsrocha.kata.tictactoe.system.ExceptionCode;
import com.dhsrocha.kata.tictactoe.vo.Bitboard;
import com.dhsrocha.kata.tictactoe.vo.Bitboard.Processor;
import com.dhsrocha.kata.tictactoe.vo.Bitboard.Result;
import com.dhsrocha.kata.tictactoe.vo.Bitboard.Validator;
import java.util.BitSet;
import java.util.Optional;
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
enum Type implements Processor, Validator {
  TIC_TAC_TOE(2, 9) {
    @Override
    public Optional<ExceptionCode> validate(@NonNull final Bitboard bitboard) {
      final var self = TIC_TAC_TOE;
      final var set = BitSet.valueOf(new long[] {bitboard.getState()});
      if (set.cardinality() > self.getTiles()) {
        return Optional.of(ExceptionCode.BITBOARD_EXCESSIVE_BITS);
      }
      final var home = set.get(0, self.getTiles());
      final var away = set.get(self.getTiles(), self.getTiles() * self.getStates());
      if (home.intersects(away)) {
        return Optional.of(ExceptionCode.BITBOARD_PIECE_IN_SAME_TILE);
      }
      return Optional.empty();
    }

    @Override
    public Bitboard.Result resultOf(@NonNull final Bitboard.Processed processed) {
      final var rounds = Long.bitCount(processed.getState());
      if (rounds < 5) {
        return Bitboard.Result.NOT_OVER;
      }
      for (final var win : WIN_STATES) {
        if (win == (win & (processed.getState() >> TIC_TAC_TOE.getTiles()))) {
          return Result.HOME;
        }
        if (win == (win & (processed.getState() & (1L << TIC_TAC_TOE.getTiles()) - 1))) {
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
}
