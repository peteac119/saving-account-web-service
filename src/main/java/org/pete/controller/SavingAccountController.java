package org.pete.controller;

import org.pete.model.request.CreateSavingAccountRequest;
import org.pete.model.request.DepositRequest;
import org.pete.service.SavingAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/saving-account")
public class SavingAccountController {

    private final SavingAccountService savingAccountService;

    public SavingAccountController(SavingAccountService savingAccountService) {
        this.savingAccountService = savingAccountService;
    }

    @PostMapping
    public ResponseEntity<?> createSavingAccount(@RequestBody CreateSavingAccountRequest request) {
        savingAccountService.createSavingAccount(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/deposit")
    public ResponseEntity<?> deposit(@RequestBody DepositRequest depositRequest) {
        savingAccountService.deposit(depositRequest);
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
