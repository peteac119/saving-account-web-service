package org.pete.repository;

import org.pete.entity.SavingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingAccountRepository extends JpaRepository<SavingAccount, Long> {
    @Query(value = "select nextval('account_num_seq')", nativeQuery = true)
    Long nextAccountNumber();

    List<SavingAccount> findByCustomerId(Long customerId);

    SavingAccount findOneByAccountNumber(String accountNumber);
}
