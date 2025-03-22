package org.pete.service;

import org.pete.constant.Channel;
import org.pete.constant.TransactionAction;
import org.pete.entity.SavingAccounts;
import org.pete.entity.TransactionAuditLog;
import org.pete.repository.TransactionAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class TransactionAuditLogService {

    private final TransactionAuditLogRepository transactionAuditLogRepository;

    public TransactionAuditLogService(TransactionAuditLogRepository transactionAuditLogRepository) {
        this.transactionAuditLogRepository = transactionAuditLogRepository;
    }

    @Transactional
    public void logTransaction(SavingAccounts account,
                               TransactionAction action,
                               Channel channel,
                               BigDecimal currentBalance,
                               BigDecimal amount,
                               String remarks) {

        BigDecimal actualAmount = formatAmount(action, amount);

        TransactionAuditLog log = new TransactionAuditLog();
        log.setTransactionDate(LocalDate.now());
        log.setTransactionTime(LocalTime.now());
        log.setCode(action.getCode());
        log.setChannel(channel.getCode());
        log.setTransactionAmount(actualAmount);
        log.setBalance(currentBalance);
        log.setRemarks(remarks);
        log.setSavingAccounts(account);


        transactionAuditLogRepository.save(log);
    }

    private BigDecimal formatAmount(TransactionAction action, BigDecimal amount) {
        return action == TransactionAction.DEPOSIT ? amount.abs() : amount.negate();
    }
}
