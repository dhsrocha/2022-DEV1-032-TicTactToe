package com.dhsrocha.kata.tictactoe.feature;

import com.dhsrocha.kata.tictactoe.base.Domain;
import com.github.javafaker.Faker;
import java.util.EnumSet;
import java.util.NoSuchElementException;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import lombok.NonNull;

/**
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface RandomStubExtension {

  /** Validator factory.. */
  ValidatorFactory VALIDATOR = Validation.buildDefaultValidatorFactory();
  /** Faker agent. */
  Faker FAKER = new Faker();

  /**
   * Randomly pick an item from the provided {@link Enum} parameter. Like {@link Faker} for strings,
   * this is designed to populate enum attributes of related {@link Domain} type for stubbing.
   *
   * @param type Class reference to pick an item from.
   * @param <E> Enum type to loop on.
   * @return A randomly picked item.
   * @throws NoSuchElementException if provided enum has no items to fetch from.
   */
  static <E extends Enum<E>> @NonNull E randomOf(final @NonNull Class<E> type) {
    return EnumSet.allOf(type).stream().findAny().orElseThrow(NoSuchElementException::new);
  }
}
