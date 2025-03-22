package org.pete.controller;

import org.pete.model.request.CreateSavingAccountRequest;
import org.pete.model.request.DepositRequest;
import org.pete.model.response.CreateSavingAccountResponse;
import org.pete.model.result.CreateSavingAccountResult;
import org.pete.service.SavingAccountService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<CreateSavingAccountResponse> createSavingAccount(@RequestBody CreateSavingAccountRequest request) {
        CreateSavingAccountResult result = savingAccountService.createSavingAccount(request);
        return switch(result) {
            case CreateSavingAccountResult.CustNotFound custNotFound ->
                    ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(new CreateSavingAccountResponse(
                                    null,
                                    null,
                                    "Customer is not found."
                            ));
            case CreateSavingAccountResult.Success success->
                    ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(new CreateSavingAccountResponse(
                                    success.getAccountNumber(),
                                    success.getCurrentBalance(),
                                    null
                            ));
            default -> ResponseEntity.unprocessableEntity().build();
        };
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
