package org.pete.repository;

import org.pete.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findOneByEmailOrCitizenId(String email, String citizenId);
}
