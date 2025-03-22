package org.pete.repository;

import org.pete.entity.SavingAccounts;
import org.pete.entity.TransactionAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionAuditLogRepository extends JpaRepository<TransactionAuditLog, Long> {
    List<TransactionAuditLog> findByTransactionDateBetweenAndSavingAccounts(LocalDate startDate, LocalDate endDate, SavingAccounts savingAccount);
}
