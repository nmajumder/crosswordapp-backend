package com.crosswordapp;

import com.crosswordapp.generation.GenerationApp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class CrosswordApplication {

    public static void main(String[] args) {
        // full sized themed crossword service
        StaticCrosswordService.initializeCrosswords();

        // mini generation services
        StaticMiniClueService.initializeMiniClueService();
        StaticMiniGridService.initializeMiniGridService();
        GenerationApp.initializeGenerationApp();

        // mini board per user cache
        MiniBoardCache.initializeMiniBoardCache();

        // leaderboard of all stats over all users cache
        LeaderboardCache.initializeLeaderboardCache();

        SpringApplication.run(CrosswordApplication.class, args);
    }

    @Bean
    PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
