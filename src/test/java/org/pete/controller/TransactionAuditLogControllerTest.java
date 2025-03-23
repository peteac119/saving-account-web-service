//package org.pete.controller;
//
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.MethodSource;
//import org.mockito.Mockito;
//import org.pete.model.result.TransactionHistoryResult;
//import org.pete.model.result.TransferResult;
//import org.pete.service.TransactionAuditLogService;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class TransactionAuditLogControllerTest {
//
//    private final TransactionAuditLogService mockTransactionAuditLogService = Mockito.mock(TransactionAuditLogService.class);
//    private final TransactionAuditLogController controller = new TransactionAuditLogController(mockTransactionAuditLogService);
//
//    @Nested
//    public class ListTransactionTestSuite {
//        @Test
//        public void should_return_ok_status_when_retrieving_transaction_list_successfully() {
//
//        }
//
//        @ParameterizedTest
//        @MethodSource("notPassValidationResults")
//        public void should_return_bad_request_status_if_validation_does_not_pass(TransferResult transferResult) {
//
//        }
//
//        static List<TransactionHistoryResult> notPassValidationResults() {
//            return null;
//        }
//    }
//
//}