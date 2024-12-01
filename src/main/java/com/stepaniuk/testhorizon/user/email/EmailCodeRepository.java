package com.stepaniuk.testhorizon.user.email;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailCodeRepository extends JpaRepository<EmailCode, Long> {
    void deleteByCode(String code);
}