package org.pete.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.pete.constant.Channel;
import org.pete.constant.TransactionAction;
import org.pete.entity.SavingAccounts;
import org.pete.entity.TransactionAuditLog;
import org.pete.entity.Users;
import org.pete.model.response.TransactionHistoryRecord;
import org.pete.model.result.TransactionHistoryResult;
import org.pete.repository.SavingAccountRepository;
import org.pete.repository.TransactionAuditLogRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionAuditLogServiceTest {

    private final TransactionAuditLogRepository mockRepository = Mockito.mock(TransactionAuditLogRepository.class);
    private final SavingAccountRepository mockSavingAccountRepository = Mockito.mock(SavingAccountRepository.class);
    private final TransactionAuditLogService service = new TransactionAuditLogService(mockRepository, mockSavingAccountRepository);

    @Nested
    public class LogTransactionTestSuite {
        @Test
        public void should_log_with_positive_amount_when_action_is_deposit() {
            SavingAccounts mockSavingAccount = mockSavingAccount(1L, "testAccountNumber", 4L);
            ArgumentCaptor<TransactionAuditLog> captor = ArgumentCaptor.forClass(TransactionAuditLog.class);
            when(mockRepository.save(any(TransactionAuditLog.class))).thenReturn(null);

            service.logTransaction(
                    mockSavingAccount,
                    TransactionAction.DEPOSIT,
                    Channel.TELLER,
                    mockSavingAccount.getBalance(),
                    BigDecimal.TWO,
                    "random remarks"
            );

            verify(mockRepository, times(1)).save(captor.capture());
            TransactionAuditLog actualLog = captor.getValue();
            assertEquals(mockSavingAccount, actualLog.getSavingAccounts());
            assertEquals(TransactionAction.DEPOSIT.getCode(), actualLog.getCode());
            assertEquals(Channel.TELLER.getCode(), actualLog.getChannel());
            assertEquals(mockSavingAccount.getBalance(), actualLog.getBalance());
            assertEquals(BigDecimal.TWO, actualLog.getTransactionAmount());
            assertEquals("random remarks", actualLog.getRemarks());
            assertNotNull(actualLog.getTransactionDate());
            assertNotNull(actualLog.getTransactionTime());
        }

        @Test
        public void should_log_with_negative_amount_when_action_is_transfer() {
            SavingAccounts mockSavingAccount = mockSavingAccount(2L, "testAccountNumber", 5L);
            ArgumentCaptor<TransactionAuditLog> captor = ArgumentCaptor.forClass(TransactionAuditLog.class);
            when(mockRepository.save(any(TransactionAuditLog.class))).thenReturn(null);

            service.logTransaction(
                    mockSavingAccount,
                    TransactionAction.TRANSFER,
                    Channel.CUSTOMER,
                    mockSavingAccount.getBalance(),
                    BigDecimal.TWO,
                    "random remarks"
            );

            verify(mockRepository, times(1)).save(captor.capture());
            TransactionAuditLog actualLog = captor.getValue();
            assertEquals(mockSavingAccount, actualLog.getSavingAccounts());
            assertEquals(TransactionAction.TRANSFER.getCode(), actualLog.getCode());
            assertEquals(Channel.CUSTOMER.getCode(), actualLog.getChannel());
            assertEquals(mockSavingAccount.getBalance(), actualLog.getBalance());
            assertEquals(BigDecimal.TWO.negate(), actualLog.getTransactionAmount());
            assertEquals("random remarks", actualLog.getRemarks());
            assertNotNull(actualLog.getTransactionDate());
            assertNotNull(actualLog.getTransactionTime());
        }
    }

    @Nested
    public class ListTransactionTestSuite {
        @Test
        public void should_retrieve_transaction_history_successfully() {
            Long mockRequesterId = 3L;
            int mockYear = 2025;
            int mockMonth = 3;
            String mockAccountNumber = "testAccountNumber";
            ArgumentCaptor<LocalDate> startDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
            ArgumentCaptor<LocalDate> endDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
            List<TransactionAuditLog> mockTransactionAuditLogs = mockTransactionAuditLogs();
            SavingAccounts mockSavingAccount = mockSavingAccount(2L, mockAccountNumber, mockRequesterId);
            when(mockSavingAccountRepository.findOneByAccountNumber(mockSavingAccount.getAccountNumber())).thenReturn(mockSavingAccount);
            when(mockRepository.findByTransactionDateBetweenAndSavingAccounts(any(LocalDate.class), any(LocalDate.class), eq(mockSavingAccount)))
                    .thenReturn(mockTransactionAuditLogs);

            TransactionHistoryResult actualResult = service.listTransaction(mockAccountNumber, mockYear, mockMonth, mockRequesterId);

            assertThat(actualResult, instanceOf(TransactionHistoryResult.Success.class));
            TransactionHistoryResult.Success successResult = (TransactionHistoryResult.Success) actualResult;
            List<TransactionHistoryRecord> transactionHistoryRecords = successResult.getTransactionHistoryRecords();
            assertTransactionHistoryRecord(mockTransactionAuditLogs, transactionHistoryRecords);
            verify(mockRepository, times(1)).findByTransactionDateBetweenAndSavingAccounts(
                    startDateCaptor.capture(), endDateCaptor.capture(), eq(mockSavingAccount));
            assertEquals(LocalDate.of(mockYear, mockMonth, 1), startDateCaptor.getValue());
            assertEquals(LocalDate.of(mockYear, mockMonth, 31), endDateCaptor.getValue());
        }

        @Test
        public void should_return_account_not_found_if_the_account_does_not_exist() {
            Long mockRequesterId = 3L;
            int mockYear = 2025;
            int mockMonth = 3;
            String mockAccountNumber = "invalidAccountNumber";
            when(mockSavingAccountRepository.findOneByAccountNumber(mockAccountNumber)).thenReturn(null);

            TransactionHistoryResult actualResult = service.listTransaction(mockAccountNumber, mockYear, mockMonth, mockRequesterId);

            assertThat(actualResult, instanceOf(TransactionHistoryResult.AccountNotFound.class));
        }

        @Test
        public void should_return_wrong_account_if_requester_id_does_not_match_with_user_id_in_the_account() {
            Long mockRequesterId = 1L;
            int mockYear = 2025;
            int mockMonth = 3;
            String mockAccountNumber = "anyAccountNumber";
            SavingAccounts mockSavingAccount = mockSavingAccount(10L, mockAccountNumber, 5L);
            when(mockSavingAccountRepository.findOneByAccountNumber(mockSavingAccount.getAccountNumber())).thenReturn(mockSavingAccount);

            TransactionHistoryResult actualResult = service.listTransaction(mockAccountNumber, mockYear, mockMonth, mockRequesterId);

            assertThat(actualResult, instanceOf(TransactionHistoryResult.WrongAccountNumber.class));
        }

        private void assertTransactionHistoryRecord(List<TransactionAuditLog> mockTransactionAuditLogs, List<TransactionHistoryRecord> transactionHistoryRecords) {
            TransactionAuditLog mockAuditLog = mockTransactionAuditLogs.getFirst();
            TransactionHistoryRecord actualRecord = transactionHistoryRecords.getFirst();

            assertEquals(mockAuditLog.getTransactionDate(), actualRecord.transactionDate());
            assertEquals(mockAuditLog.getTransactionTime(), actualRecord.transactionTime());
            assertEquals(mockAuditLog.getChannel(), actualRecord.channel());
            assertEquals(mockAuditLog.getCode(), actualRecord.code());
            assertEquals(mockAuditLog.getBalance(), actualRecord.balance());
            assertEquals(mockAuditLog.getTransactionAmount(), actualRecord.transactionAmount());
            assertEquals(mockAuditLog.getRemarks(), actualRecord.remarks());
        }

        private List<TransactionAuditLog> mockTransactionAuditLogs() {
            TransactionAuditLog transactionAuditLog = new TransactionAuditLog();
            transactionAuditLog.setTransactionDate(LocalDate.now());
            transactionAuditLog.setTransactionTime(LocalTime.now());
            transactionAuditLog.setChannel(Channel.TELLER.getCode());
            transactionAuditLog.setCode(TransactionAction.DEPOSIT.getCode());
            transactionAuditLog.setTransactionAmount(BigDecimal.TWO);
            transactionAuditLog.setBalance(BigDecimal.TEN);
            transactionAuditLog.setRemarks("testRemarks");

            return List.of(transactionAuditLog);
        }
    }

    private SavingAccounts mockSavingAccount(Long accountId,
                                             String accountNumber,
                                             Long userId) {
        Users mockUser = new Users();
        mockUser.setId(userId);

        SavingAccounts savingAccounts = new SavingAccounts();
        savingAccounts.setId(accountId);
        savingAccounts.setAccountNumber(accountNumber);
        savingAccounts.setBalance(BigDecimal.TEN);
        savingAccounts.setUsers(mockUser);

        return savingAccounts;
    }
}