package org.pete.controller;

import org.pete.model.request.CreateSavingAccountRequest;
import org.pete.model.request.DepositRequest;
import org.pete.model.response.CreateSavingAccountResponse;
import org.pete.model.response.DepositResponse;
import org.pete.model.result.CreateSavingAccountResult;
import org.pete.model.result.DepositResult;
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
    public ResponseEntity<DepositResponse> deposit(@RequestBody DepositRequest depositRequest) {
        DepositResult result = savingAccountService.deposit(depositRequest);
        return switch (result) {
            case DepositResult.SavingAccountNotFound notFound ->
                    ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(new DepositResponse(
                                    null,
                                    null ,
                                    "Invalid account number."));
            case DepositResult.DepositAmountIsLessThanOne lessThanOne->
                    ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(new DepositResponse(
                                    null,
                                    null,
                                    "Deposit amount must be more than one."));
            case DepositResult.Success success ->
                    ResponseEntity.ok(
                        new DepositResponse(
                                success.getAccountNumber(),
                                success.getNewBalance(),
                                null
                        )
                    );
            default -> ResponseEntity.unprocessableEntity().build();
        };
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
