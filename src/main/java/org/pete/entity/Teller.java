package org.pete.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "TELLER")
public class Teller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "FIRST_NAME", length = 50, nullable = false)
    private String firstName;
    @Column(name = "LAST_NAME", length = 50, nullable = false)
    private String lastName;
    @Column(name = "USERNAME", length = 20, nullable = false)
    private String username;
    @Column(name = "PASSWORD", length = 20, nullable = false)
    private String password;
    @Column(name = "CREATION_DATE", nullable = false)
    private LocalDateTime creationDate;
    @Column(name = "LAST_UPDATE_DATE", nullable = false)
    private LocalDateTime lastUpdateDate;
}
