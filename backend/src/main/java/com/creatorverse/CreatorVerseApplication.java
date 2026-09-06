package com.creatorverse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CreatorVerseApplication {
    public static void main(String[] args) {
        SpringApplication.run(CreatorVerseApplication.class, args);
    }
}
