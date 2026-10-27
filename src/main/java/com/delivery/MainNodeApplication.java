package com.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MainNodeApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainNodeApplication.class, "--spring.profiles.active=main");
    }
}
