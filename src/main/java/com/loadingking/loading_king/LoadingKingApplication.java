package com.loadingking.loading_king;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@EnableJpaAuditing
@SpringBootApplication
public class LoadingKingApplication {

    public static void main(String[] args) {

        SpringApplication.run(LoadingKingApplication.class, args);
    }

}
