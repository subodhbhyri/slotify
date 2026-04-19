package com.slotify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SlotifyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SlotifyApplication.class, args);
    }
}