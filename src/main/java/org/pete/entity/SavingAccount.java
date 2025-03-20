package org.pete.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "SAVING_ACCOUNT")
public class SavingAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ACCOUNT_NUMBER", precision = 7, nullable = false)
    private String accountNumber;
    @Column(name = "BALANCE", precision = 18, scale = 2, nullable = false)
    private BigDecimal balance;
    @Column(name = "CREATION_DATE", nullable = false)
    private Timestamp creationDate;
    @Column(name = "LAST_UPDATE_DATE", nullable = false)
    private Timestamp lastUpdateDate;
    @ManyToOne
    @JoinColumn(name = "CUSTOMER_ID")
    private Customer customer;
}
