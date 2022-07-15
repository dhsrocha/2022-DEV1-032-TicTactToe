package com.dhsrocha.kata.tictactoe.feature.email;

import com.dhsrocha.kata.tictactoe.feature.player.Player;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * .
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public abstract class EmailService {

  /**
   * .
   *
   * @return .
   */
  public abstract boolean send(@NonNull final Player destination, final int token);

  /** {@inheritDoc} */
  @SuppressWarnings("unused")
  @Validated
  @Service
  @AllArgsConstructor
  private static class Impl extends EmailService {

    @Value("${dhsrocha.author.email}")
    private final String from;

    private final JavaMailSender mailer;

    @Override
    public boolean send(@NonNull final Player destination, final int token) {
      final var msg = new SimpleMailMessage();
      msg.setFrom(from);
      msg.setTo(destination.getEmail());
      msg.setSubject(destination.getFirstName());
      msg.setText("");
      mailer.send(msg);
      return Boolean.TRUE;
    }
  }
}
