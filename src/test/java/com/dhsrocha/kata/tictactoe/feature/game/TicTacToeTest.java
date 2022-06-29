package com.dhsrocha.kata.tictactoe.feature.game;

import com.dhsrocha.kata.tictactoe.helper.RandomStubExtension;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test suite for {@link Type#TIC_TAC_TOE}.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@DisplayName("Test suite for Bitboard#TIC_TAC_TOE class.")
final class TicTacToeTest implements RandomStubExtension {

  @ParameterizedTest
  @MethodSource("validStubsAndResults")
  @DisplayName(
      "GIVEN valid bitboard "
          + "WHEN calculating a bitboard with tic-tac-toe rule set "
          + "THEN return expected result accordingly.")
  void ticTacToeBitboard(final int bitboard, final Game.Result expected) {
    // Act
    final var actual = Type.TIC_TAC_TOE.process(bitboard);
    // Assert
    Assertions.assertEquals(expected, actual);
  }

  @ParameterizedTest
  @MethodSource("invalidStubsAndResults")
  @DisplayName(
      "GIVEN invalid bitboard "
          + "WHEN calculating a bitboard with tic-tac-toe rule set "
          + "THEN IllegalArgumentException is thrown.")
  void ticTacToeInvalidBitboard(final int invalidStub) {
    // Act - Assert
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> Type.TIC_TAC_TOE.process(invalidStub));
  }

  private static Stream<Arguments> validStubsAndResults() {
    return Stream.of(
        // Home
        Arguments.of(Integer.parseInt("100010001" + "010001010", 2), Game.Result.HOME),
        Arguments.of(Integer.parseInt("010010010" + "001001000", 2), Game.Result.HOME),
        Arguments.of(Integer.parseInt("111000010" + "000011001", 2), Game.Result.HOME),
        // Away
        Arguments.of(Integer.parseInt("010001010" + "100010001", 2), Game.Result.AWAY),
        Arguments.of(Integer.parseInt("001001000" + "010010010", 2), Game.Result.AWAY),
        Arguments.of(Integer.parseInt("000011001" + "111000010", 2), Game.Result.AWAY),
        // Tie
        Arguments.of(Integer.parseInt("010011100" + "101100011", 2), Game.Result.TIE),
        Arguments.of(Integer.parseInt("001110010" + "110001101", 2), Game.Result.TIE),
        Arguments.of(Integer.parseInt("101010010" + "010101101", 2), Game.Result.TIE),
        // Not over
        Arguments.of(Integer.parseInt("000000011" + "000000001", 2), Game.Result.NOT_OVER),
        Arguments.of(Integer.parseInt("001001010" + "000100101", 2), Game.Result.NOT_OVER),
        Arguments.of(Integer.parseInt("000110001" + "000000110", 2), Game.Result.NOT_OVER));
  }

  private static Stream<Integer> invalidStubsAndResults() {
    return Stream.of(
        FAKER.number().numberBetween(Integer.MIN_VALUE, -1), Integer.parseInt("1".repeat(10), 2));
  }
}
