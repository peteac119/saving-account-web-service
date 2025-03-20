package org.pete.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "CUSTOMER")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "THAI_NAME", length = 100, nullable = false)
    private String thaiName;
    @Column(name = "ENGLISH_NAME", length = 100, nullable = false)
    private String englishName;
    @Column(name = "EMAIL", length = 100, nullable = false)
    private String email;
    @Column(name = "PASSWORD", length = 20, nullable = false)
    private String password;
    @Column(name = "CITIZEN_ID", length = 13, nullable = false)
    private String citizenId;
    @Column(name = "PIN_NUM", nullable = false, precision = 6)
    private Integer pinNum;
    @Column(name = "CREATION_DATE", nullable = false)
    private Timestamp creationDate;
    @Column(name = "LAST_UPDATE_DATE", nullable = false)
    private Timestamp lastUpdateDate;
}
