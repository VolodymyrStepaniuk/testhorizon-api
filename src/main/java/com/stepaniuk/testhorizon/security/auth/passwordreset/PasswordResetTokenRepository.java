package com.stepaniuk.testhorizon.security.auth.passwordreset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long>, JpaSpecificationExecutor<PasswordResetToken> {

    Optional<PasswordResetToken> findByToken(String token);

}