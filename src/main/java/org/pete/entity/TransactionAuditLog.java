package org.pete.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "TRANSACTION_AUDIT_LOG")
public class TransactionAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "TRANSACTION_DATE", nullable = false)
    private LocalDate transactionDate;
    @Column(name = "TRANSACTION_TIME", nullable = false)
    private LocalTime transactionTime;
    @Column(name = "CODE", nullable = false)
    private String code;
    @Column(name = "CHANNEL", nullable = false)
    private String channel;
    @Column(name = "TRANSACTION_AMOUNT", precision = 18, scale = 2, nullable = false)
    private BigDecimal transactionAmount;
    @Column(name = "BALANCE", precision = 18, scale = 2, nullable = false)
    private BigDecimal balance;
    @Column(name = "REMARKS")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_ID")
    private SavingAccounts savingAccounts;
}
