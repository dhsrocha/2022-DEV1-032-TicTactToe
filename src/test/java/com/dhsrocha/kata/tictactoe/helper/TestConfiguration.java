package com.dhsrocha.kata.tictactoe.helper;

import javax.sql.DataSource;
import org.h2.Driver;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;

@SuppressWarnings("unused")
@org.springframework.boot.test.context.TestConfiguration
public class TestConfiguration {

  @Bean
  DataSource getDataSource() {
    return DataSourceBuilder.create()
        .driverClassName(Driver.class.getName())
        .url("jdbc:h2:mem:test")
        .username("sa")
        .password("")
        .build();
  }
}
