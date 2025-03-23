package org.pete.controller;

import org.pete.model.principle.UserPrinciple;
import org.pete.model.response.TransactionHistoryResponse;
import org.pete.model.result.TransactionHistoryResult;
import org.pete.service.TransactionAuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/transaction-log")
public class TransactionAuditLogController {

    private final TransactionAuditLogService transactionAuditLogService;


    public TransactionAuditLogController(TransactionAuditLogService transactionAuditLogService) {
        this.transactionAuditLogService = transactionAuditLogService;
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TransactionHistoryResponse> listTransactions(@RequestParam Integer year,
                                                                       @RequestParam Integer month,
                                                                       @RequestParam String accountNumber,
                                                                       Authentication authentication) {
        if (userPrincipleIsInvalid(authentication)) {
            return ResponseEntity.unprocessableEntity().build();
        }

        Long requesterId = ((UserPrinciple) authentication.getPrincipal()).getUsers().getId();

        TransactionHistoryResult result = transactionAuditLogService.listTransaction(
                accountNumber, year, month, requesterId);

        return switch (result) {
            case TransactionHistoryResult.Success success ->
                    ResponseEntity.ok(
                        new TransactionHistoryResponse(success.getTransactionHistoryRecords())
                    );
            default -> ResponseEntity.notFound().build();
        };
    }

    private boolean userPrincipleIsInvalid(Authentication authentication) {
        return Objects.isNull(authentication) || !(authentication.getPrincipal() instanceof UserPrinciple);
    }
}
