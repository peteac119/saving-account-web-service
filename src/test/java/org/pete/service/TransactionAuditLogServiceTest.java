package org.pete.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.pete.constant.Channel;
import org.pete.constant.TransactionAction;
import org.pete.entity.SavingAccounts;
import org.pete.entity.TransactionAuditLog;
import org.pete.repository.SavingAccountRepository;
import org.pete.repository.TransactionAuditLogRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionAuditLogServiceTest {

    private final TransactionAuditLogRepository mockRepository = Mockito.mock(TransactionAuditLogRepository.class);
    private final SavingAccountRepository mockSavingAccountRepository = Mockito.mock(SavingAccountRepository.class);
    private final TransactionAuditLogService service = new TransactionAuditLogService(mockRepository, mockSavingAccountRepository);

    @Test
    public void should_log_with_positive_amount_when_action_is_deposit() {
        SavingAccounts mockSavingAccount = mockSavingAccount(1L, BigDecimal.TEN);
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
        SavingAccounts mockSavingAccount = mockSavingAccount(2L, BigDecimal.TEN);
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

    private SavingAccounts mockSavingAccount(Long accountId, BigDecimal currentAmount) {
        SavingAccounts savingAccounts = new SavingAccounts();
        savingAccounts.setId(accountId);
        savingAccounts.setBalance(currentAmount);

        return savingAccounts;
    }
}