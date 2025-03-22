package org.pete.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "SAVING_ACCOUNTS")
public class SavingAccounts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ACCOUNT_NUMBER", precision = 7, nullable = false)
    private String accountNumber;
    @Column(name = "BALANCE", precision = 18, scale = 2, nullable = false)
    private BigDecimal balance;
    @CreationTimestamp
    @Column(name = "CREATION_DATE", nullable = false)
    private LocalDateTime creationDate;
    @UpdateTimestamp
    @Column(name = "LAST_UPDATE_DATE", nullable = false)
    private LocalDateTime lastUpdateDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private Users users;
}
