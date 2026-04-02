package com.gigshield;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GigShieldApplication {
    public static void main(String[] args) {
        SpringApplication.run(GigShieldApplication.class, args);
    }
}
