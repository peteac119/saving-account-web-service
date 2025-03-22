package org.pete.repository;

import org.pete.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Users findOneByEmail(String email);
    Users findOneByEmailOrCitizenId(String email, String citizenId);
    Users findOneByThaiNameAndEnglishNameAndCitizenId(String thaiName, String englishName, String citizenId);
}
