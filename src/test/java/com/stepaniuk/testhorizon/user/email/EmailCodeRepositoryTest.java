package com.stepaniuk.testhorizon.user.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.stepaniuk.testhorizon.testspecific.JpaLevelTest;
import com.stepaniuk.testhorizon.user.User;

import com.stepaniuk.testhorizon.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@JpaLevelTest
@Sql(scripts = {"classpath:sql/user/users.sql", "classpath:sql/user/authorities.sql", "classpath:sql/user/users_has_authorities.sql", "classpath:sql/user/email_codes.sql"})
class EmailCodeRepositoryTest {

    @Autowired
    private EmailCodeRepository emailCodeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveEmailCode() {
        // given
        User userForCode = new User(
                null, "first", "last", "email@gmail.com", 100, "Password+123",
                true, true, true, true,
                Set.of(), null, null
        );

        Instant expiresAt = Instant.now().plus(1, ChronoUnit.DAYS);

        EmailCode emailCode = new EmailCode(null, "code", null, expiresAt, userForCode);

        // when
        EmailCode savedEmailCode = emailCodeRepository.save(emailCode);

        // then
        assertNotNull(savedEmailCode);
        assertNotNull(savedEmailCode.getId());
        assertEquals(emailCode.getCode(), savedEmailCode.getCode());
        assertEquals(emailCode.getExpiresAt(), savedEmailCode.getExpiresAt());
        assertEquals(emailCode.getUser(), savedEmailCode.getUser());
        assertEquals(emailCode.getCreatedAt(), savedEmailCode.getCreatedAt());
    }

    @Test
    void shouldThrowExceptionWhenSavingEmailCodeWithNullCode() {
        // given
        User userForCode = new User(
                null, "first", "last", "email@gmail.com", 100, "Password+123",
                true, true, true, true,
                Set.of(), null, null
        );

        Instant expiresAt = Instant.now().plus(1, ChronoUnit.DAYS);

        EmailCode emailCode = new EmailCode(null, null, null, expiresAt, userForCode);

        assertThrows(DataIntegrityViolationException.class, () -> emailCodeRepository.save(emailCode));
    }

    @Test
    void shouldReturnEmailCodeWhenFindById(){
        EmailCode emailCode = emailCodeRepository.findById(1L).orElseThrow();

        assertNotNull(emailCode);
        assertEquals(1L, emailCode.getId());
        assertEquals("123456", emailCode.getCode());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), emailCode.getCreatedAt());
        assertEquals(Instant.parse("2025-11-25T17:28:19.266615Z"), emailCode.getExpiresAt());
        assertEquals(1L, emailCode.getUser().getId());
    }

    @Test
    void shouldReturnEmailCodeWhenFindByCode(){
        EmailCode emailCode = emailCodeRepository.findByCode("123456").orElseThrow();

        assertNotNull(emailCode);
        assertEquals(1L, emailCode.getId());
        assertEquals("123456", emailCode.getCode());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), emailCode.getCreatedAt());
        assertEquals(Instant.parse("2025-11-25T17:28:19.266615Z"), emailCode.getExpiresAt());
        assertEquals(1L, emailCode.getUser().getId());
    }

    @Test
    void shouldUpdateEmailCodeWhenChangingCode() {
        // given
        EmailCode emailCode = emailCodeRepository.findById(1L).orElseThrow();

        emailCode.setCode("newCode");

        // when
        EmailCode updatedEmailCode = emailCodeRepository.save(emailCode);

        // then
        assertNotNull(updatedEmailCode);
        assertEquals(emailCode.getId(), updatedEmailCode.getId());
        assertEquals("newCode", updatedEmailCode.getCode());
        assertEquals(emailCode.getCreatedAt(), updatedEmailCode.getCreatedAt());
        assertEquals(emailCode.getExpiresAt(), updatedEmailCode.getExpiresAt());
        assertEquals(emailCode.getUser(), updatedEmailCode.getUser());
    }

    @Test
    void shouldDeleteEmailCodeWhenDeletingByExistingEmailCode() {
        // given
        EmailCode emailCode = emailCodeRepository.findById(1L).orElseThrow();

        // when
        emailCodeRepository.delete(emailCode);

        // then
        assertFalse(emailCodeRepository.existsById(1L));
    }

    @Test
    void shouldDeleteEmailCodeWhenDeletingByExistingEmailCodeId(){
        // when
        emailCodeRepository.deleteById(1L);

        // then
        assertFalse(emailCodeRepository.existsById(1L));
    }

    @Test
    void shouldReturnTrueWhenExistsByExistingEmailCodeId(){
        // when
        boolean exists = emailCodeRepository.existsById(1L);

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenExistsByNonExistingEmailCodeId(){
        // when
        boolean exists = emailCodeRepository.existsById(100L);

        // then
        assertFalse(exists);
    }

    @Test
    void shouldReturnNotEmptyListWhenFindAll() {
        // when
        List<EmailCode> emailCodes = emailCodeRepository.findAll();

        // then
        assertNotNull(emailCodes);
        assertFalse(emailCodes.isEmpty());
    }
}
