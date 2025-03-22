package org.pete.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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
    @Column(name = "PIN_NUM", length = 6, nullable = false)
    private String pinNum;
    @CreationTimestamp
    @Column(name = "CREATION_DATE", nullable = false)
    private LocalDateTime creationDate;
    @UpdateTimestamp
    @Column(name = "LAST_UPDATE_DATE", nullable = false)
    private LocalDateTime lastUpdateDate;
}
