package com.ptjcoding.nbcampspringnewsfeed;

import lombok.Generated;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.PropertySource;

@Generated
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@PropertySource(value = {"classpath:db.properties", "classpath:jwt.properties"})
public class NbcampSpringNewsfeedApplication {

  public static void main(String[] args) {
    SpringApplication.run(NbcampSpringNewsfeedApplication.class, args);
  }

}
