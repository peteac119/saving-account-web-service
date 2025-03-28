package org.pete.service;

import org.pete.constant.Channel;
import org.pete.constant.TransactionAction;
import org.pete.entity.SavingAccounts;
import org.pete.entity.TransactionAuditLog;
import org.pete.model.response.TransactionHistoryRecord;
import org.pete.model.result.TransactionHistoryResult;
import org.pete.repository.SavingAccountRepository;
import org.pete.repository.TransactionAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

@Service
public class TransactionAuditLogService {

    private final TransactionAuditLogRepository transactionAuditLogRepository;
    private final SavingAccountRepository savingAccountRepository;

    public TransactionAuditLogService(TransactionAuditLogRepository transactionAuditLogRepository,
                                      SavingAccountRepository savingAccountRepository) {
        this.transactionAuditLogRepository = transactionAuditLogRepository;
        this.savingAccountRepository = savingAccountRepository;
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

    @Transactional(readOnly = true)
    public TransactionHistoryResult listTransaction(String accountNumber,
                                                    Integer year,
                                                    Integer month,
                                                    Long requesterId) {
        SavingAccounts savingAccounts = savingAccountRepository.findOneByAccountNumber(accountNumber);

        if (Objects.isNull(savingAccounts)) {
            return new TransactionHistoryResult.AccountNotFound();
        }

        if (!Objects.equals(savingAccounts.getUsers().getId(), requesterId)) {
            return new TransactionHistoryResult.WrongAccountNumber();
        }

        YearMonth requestedYearMonth = YearMonth.of(year, month);
        LocalDate startDate = getStartDate(requestedYearMonth);
        LocalDate endDate = getEndDate(requestedYearMonth);

        List<TransactionAuditLog> auditLogs = transactionAuditLogRepository.findByTransactionDateBetweenAndSavingAccounts(startDate, endDate, savingAccounts);
        List<TransactionHistoryRecord> transactionHistoryRecords = transformToHistoryRecord(auditLogs);

        return new TransactionHistoryResult.Success(transactionHistoryRecords);
    }

    private List<TransactionHistoryRecord> transformToHistoryRecord(List<TransactionAuditLog> auditLogs) {
        if (Objects.isNull(auditLogs) || auditLogs.isEmpty()) {
            return List.of();
        }

        return auditLogs.stream()
                .map(auditLog -> new TransactionHistoryRecord(
                        auditLog.getTransactionDate(),
                        auditLog.getTransactionTime(),
                        auditLog.getCode(),
                        auditLog.getChannel(),
                        auditLog.getTransactionAmount(),
                        auditLog.getBalance(),
                        auditLog.getRemarks()
                ))
                .toList();
    }

    private LocalDate getEndDate(YearMonth requestedYearMonth) {
        return LocalDate.of(
                requestedYearMonth.getYear(),
                requestedYearMonth.getMonth(),
                requestedYearMonth.atEndOfMonth().getDayOfMonth()
        );
    }

    private LocalDate getStartDate(YearMonth requestedYearMonth) {
        return LocalDate.of(
                requestedYearMonth.getYear(),
                requestedYearMonth.getMonth(),
                1
        );
    }
}
