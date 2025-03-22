package org.pete.controller;

import org.pete.model.principle.UserPrinciple;
import org.pete.model.request.CreateSavingAccountRequest;
import org.pete.model.request.DepositRequest;
import org.pete.model.request.TransferRequest;
import org.pete.model.response.CreateSavingAccountResponse;
import org.pete.model.response.DepositResponse;
import org.pete.model.response.FindSavingAccountInfoResponse;
import org.pete.model.response.TransferResponse;
import org.pete.model.result.CreateSavingAccountResult;
import org.pete.model.result.DepositResult;
import org.pete.model.result.FindSavingAccountInfoResult;
import org.pete.model.result.TransferResult;
import org.pete.service.SavingAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Month;
import java.util.Objects;

@RestController
@RequestMapping(path = "/saving-account")
public class SavingAccountController {

    private final SavingAccountService savingAccountService;

    public SavingAccountController(SavingAccountService savingAccountService) {
        this.savingAccountService = savingAccountService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('TELLER')")
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
    @PreAuthorize("hasRole('TELLER')")
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
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TransferResponse> transfer(@RequestBody TransferRequest transferRequest, Authentication authentication) {
        if (userPrincipleIsInvalid(authentication)) {
            return ResponseEntity.unprocessableEntity().build();
        }

        Long senderId = ((UserPrinciple) authentication.getPrincipal()).getUsers().getId();
        TransferResult result = savingAccountService.transfer(transferRequest, senderId);

        return switch (result) {
            case TransferResult.Success success ->
                    ResponseEntity.ok(
                            new TransferResponse(
                                    success.getSenderAccountNumber(),
                                    success.getBeneficiaryAccountNumber(),
                                    success.getCurrentSenderBalance(),
                                    success.getCurrentBeneficiaryBalance(),
                                    null
                            )
                    );
            case TransferResult.SavingAccountNotFound savingAccountNotFound ->
                    ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(
                                    new TransferResponse(
                                            null,
                                            null,
                                            null,
                                            null,
                                            savingAccountNotFound.getMessage()
                                    )
                            );
            case TransferResult.NotEnoughBalance notEnoughBalance ->
                    ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(
                                    new TransferResponse(
                                            notEnoughBalance.getSenderAccountNumber(),
                                            null,
                                            notEnoughBalance.getCurrentSenderBalance(),
                                            null,
                                            null
                                    )
                            );
            default -> ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(
                                    new TransferResponse(
                                            null,
                                            null,
                                            null,
                                            null,
                                            TransferResult.getErrorMessageFromResultType(result)
                                    )
                            );
        };
    }

    @GetMapping("/{accountNumber}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<FindSavingAccountInfoResponse> findSavingAccountInfo(@PathVariable String accountNumber, Authentication authentication) {
        if (userPrincipleIsInvalid(authentication)) {
            return ResponseEntity.unprocessableEntity().build();
        }

        Long requesterId = ((UserPrinciple) authentication.getPrincipal()).getUsers().getId();
        FindSavingAccountInfoResult result = savingAccountService.findSavingAccountInfo(accountNumber, requesterId);

        return switch (result) {
            case FindSavingAccountInfoResult.Success success ->
                    ResponseEntity.ok(
                            new FindSavingAccountInfoResponse(
                                    success.getAccountNumber(),
                                    success.getCurrentBalance(),
                                    success.getCreationDate(),
                                    success.getLatestUpdateDate(),
                                    null
                            )
                    );
            default -> ResponseEntity.notFound().build();
        };
    }

    private boolean userPrincipleIsInvalid(Authentication authentication) {
        return Objects.isNull(authentication) || !(authentication.getPrincipal() instanceof UserPrinciple);
    }
}
