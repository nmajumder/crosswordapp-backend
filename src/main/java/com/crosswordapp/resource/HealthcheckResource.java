package com.crosswordapp.resource;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class HealthcheckResource {
    final static String PATH = "/api/healthcheck";
    @GetMapping(PATH)
    public String checkHealth() {
        return "SUCCESS";
    }
}
