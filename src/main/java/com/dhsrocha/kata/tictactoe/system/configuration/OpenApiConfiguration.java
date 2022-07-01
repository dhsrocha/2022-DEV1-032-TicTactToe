package com.dhsrocha.kata.tictactoe.system.configuration;

import com.dhsrocha.kata.tictactoe.feature.game.Game;
import com.dhsrocha.kata.tictactoe.feature.player.Player;
import com.dhsrocha.kata.tictactoe.feature.turn.Turn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import java.util.Comparator;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAPI generation concerns.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@SuppressWarnings("unused")
@Configuration
@SecurityScheme(
    scheme = "basic",
    in = SecuritySchemeIn.HEADER,
    type = SecuritySchemeType.HTTP,
    name = OpenApiConfiguration.NAME)
class OpenApiConfiguration {

  static final String NAME = "Basic Authentication (RFC 7617)";

  private static final List<String> TAGS = List.of(Player.TAG, Game.TAG, Turn.TAG);

  @Bean
  OpenAPI openApi(
      final @Value("${maven.project.name}") String name,
      final @Value("${maven.project.name}") String desc,
      final @Value("${maven.project.version}") String version,
      final @Value("${maven.project.organization.name}") String organization,
      final @Value("${dhsrocha.author.email}") String email) {
    final var contact = new Contact().name(organization).email(email);
    final var info = new Info().title(name).description(desc).version(version).contact(contact);
    return new OpenAPI().addSecurityItem(new SecurityRequirement().addList(NAME)).info(info);
  }

  @Bean
  OpenApiCustomiser sortTags() {
    return api -> api.getTags().sort(Comparator.comparingInt(t -> TAGS.indexOf(t.getName())));
  }
}
