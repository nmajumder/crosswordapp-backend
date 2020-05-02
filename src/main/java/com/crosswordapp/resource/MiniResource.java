package com.crosswordapp.resource;

import com.crosswordapp.object.Mini;
import com.crosswordapp.object.MiniDifficulty;
import com.crosswordapp.rep.MiniRep;
import com.crosswordapp.service.MiniService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:4200" })
@RestController
public class MiniResource {
    final static String PATH = "/api/mini";
    Logger logger = LoggerFactory.getLogger(MiniResource.class);

    @Autowired
    private MiniService miniService;

    @GetMapping(PATH + "/{size}/{difficulty}/generate")
    public MiniRep generateMini(@PathVariable Integer size, @PathVariable MiniDifficulty difficulty) {
        return miniService.generateMini(size, difficulty);
    }
}
