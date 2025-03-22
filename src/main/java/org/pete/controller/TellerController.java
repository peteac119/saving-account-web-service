package org.pete.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teller")
public class TellerController {

    @PostMapping
    public ResponseEntity<?> registerTeller() {
        return ResponseEntity.ok().build();
    }
}
