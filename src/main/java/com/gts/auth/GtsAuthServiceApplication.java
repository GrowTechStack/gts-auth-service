package com.gts.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class GtsAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GtsAuthServiceApplication.class, args);
    }

}
