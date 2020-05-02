package com.crosswordapp;

import com.crosswordapp.generation.GenerationApp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CrosswordApplication {

    public static void main(String[] args) {
        StaticCrosswordService.initializeCrosswords();
        StaticMiniClueService.initializeMiniClueService();
        StaticMiniGridService.initializeMiniGridService();
        GenerationApp.initializeGenerationApp();
        SpringApplication.run(CrosswordApplication.class, args);
    }
}
