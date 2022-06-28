package com.dhsrocha.kata.tictactoe;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application entry-point.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@SpringBootApplication
public class Application {

  public static void main(final String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @SuppressWarnings("unused")
  @Configuration
  static class DocumentationConfig {

    @Value("${maven.project.name}")
    private String name;

    @Value("${maven.project.name}")
    private String description;

    @Value("${maven.project.version}")
    private String version;

    @Value("${maven.project.organization.name}")
    private String organizationName;

    @Value("${dhsrocha.author.email}")
    private String email;

    @Bean
    OpenAPI openApi() {
      final var contact = new Contact().name(organizationName).email(email);
      return new OpenAPI()
          .info(new Info().title(name).description(description).version(version).contact(contact));
    }
  }
}
