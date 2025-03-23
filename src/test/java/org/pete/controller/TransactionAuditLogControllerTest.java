package org.pete.controller;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.pete.entity.Users;
import org.pete.model.principle.UserPrinciple;
import org.pete.model.response.TransactionHistoryRecord;
import org.pete.model.response.TransactionHistoryResponse;
import org.pete.model.result.TransactionHistoryResult;
import org.pete.service.TransactionAuditLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class TransactionAuditLogControllerTest {

    private final TransactionAuditLogService mockTransactionAuditLogService = Mockito.mock(TransactionAuditLogService.class);
    private final TransactionAuditLogController controller = new TransactionAuditLogController(mockTransactionAuditLogService);

    @Nested
    public class ListTransactionTestSuite {
        @Test
        public void should_return_ok_status_when_retrieving_transaction_list_successfully() {
            Long mockUserId = 4L;
            Integer mockYear = 2025;
            Integer mockMonth = 3;
            String mockAccountNumber = "testAccountNumber";
            Users mockUser = mockUser(mockUserId);
            UserPrinciple mockUserPrinciple = new UserPrinciple(mockUser);
            Authentication mockAuthentication = Mockito.mock(Authentication.class);
            List<TransactionHistoryRecord> mockTransactionHistoryRecords = mockTransactionHistoryRecords();
            TransactionHistoryResult successResult = new TransactionHistoryResult.Success(mockTransactionHistoryRecords);
            when(mockAuthentication.getPrincipal()).thenReturn(mockUserPrinciple);
            when(mockTransactionAuditLogService.listTransaction(mockAccountNumber, mockYear, mockMonth, mockUserId)).thenReturn(successResult);

            ResponseEntity<TransactionHistoryResponse> actualResponse = controller.listTransactions(
                    mockYear, mockMonth, mockAccountNumber, mockAuthentication);

            assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
            assertNotNull(actualResponse.getBody());
        }

        @ParameterizedTest
        @MethodSource("notPassValidationResults")
        public void should_return_not_found_status_if_validation_does_not_pass(TransactionHistoryResult transactionHistoryResult) {
            Long mockUserId = 4L;
            Integer mockYear = 2025;
            Integer mockMonth = 3;
            String mockAccountNumber = "testAccountNumber";
            Users mockUser = mockUser(mockUserId);
            UserPrinciple mockUserPrinciple = new UserPrinciple(mockUser);
            Authentication mockAuthentication = Mockito.mock(Authentication.class);
            when(mockAuthentication.getPrincipal()).thenReturn(mockUserPrinciple);
            when(mockTransactionAuditLogService.listTransaction(mockAccountNumber, mockYear, mockMonth, mockUserId))
                    .thenReturn(transactionHistoryResult);

            ResponseEntity<TransactionHistoryResponse> actualResponse = controller.listTransactions(
                    mockYear, mockMonth, mockAccountNumber, mockAuthentication);

            assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
            assertNull(actualResponse.getBody());
        }

        static List<TransactionHistoryResult> notPassValidationResults() {
            return List.of(
                    new TransactionHistoryResult.WrongAccountNumber(),
                    new TransactionHistoryResult.AccountNotFound()
            );
        }

        private List<TransactionHistoryRecord> mockTransactionHistoryRecords() {
            TransactionHistoryRecord record = new TransactionHistoryRecord(
                    LocalDate.now(),
                    LocalTime.now(),
                    "testCode",
                    "testChannel",
                    BigDecimal.TWO,
                    BigDecimal.TWO,
                    "testRemarks"
            );
            return List.of(record);
        }
    }

    private Users mockUser(Long userId) {
        Users users = new Users();
        users.setId(userId);

        return users;
    }
}