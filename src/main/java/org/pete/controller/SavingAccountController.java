package org.pete.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/saving-account")
public class SavingAccountController {

    @PostMapping(path = "/deposit")
    public ResponseEntity<?> deposit() {
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/transfer")
    public ResponseEntity<?> transfer() {
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<?> listTransactions() {
        return ResponseEntity.ok().build();
    }
}
