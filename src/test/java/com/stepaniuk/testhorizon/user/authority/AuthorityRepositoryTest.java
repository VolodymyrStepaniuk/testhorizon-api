package com.stepaniuk.testhorizon.user.authority;

import com.stepaniuk.testhorizon.testspecific.JpaLevelTest;
import com.stepaniuk.testhorizon.types.user.AuthorityName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JpaLevelTest
@Sql(scripts = {"classpath:sql/user/authorities.sql"})
class AuthorityRepositoryTest {

    @Autowired
    private AuthorityRepository authorityRepository;

    @Test
    void shouldSaveAuthority() {
        // given
        Authority authority = new Authority(null, AuthorityName.ADMIN);

        // when
        Authority savedAuthority = authorityRepository.save(authority);

        // then
        assertNotNull(savedAuthority);
        assertNotNull(savedAuthority.getId());
        assertEquals(authority.getName(), savedAuthority.getName());
    }

    @Test
    void shouldThrowExceptionWhenSavingAuthorityWithNullName() {
        // given
        Authority authority = new Authority(null, null);

        // when & then
        assertThrows(DataIntegrityViolationException.class, () -> authorityRepository.save(authority));
    }

    @Test
    void shouldReturnAuthorityWhenFindById() {
        // when
        Authority authority = authorityRepository.findById(1L).orElseThrow();

        // then
        assertNotNull(authority);
        assertEquals(1L, authority.getId());
        assertEquals(AuthorityName.DEVELOPER, authority.getName());
    }

    @Test
    void shouldReturnAuthorityWhenFindByName() {
        // when
        Authority authority = authorityRepository.findByName(AuthorityName.DEVELOPER).orElseThrow();

        // then
        assertNotNull(authority);
        assertEquals(1L, authority.getId());
        assertEquals(AuthorityName.DEVELOPER, authority.getName());
    }

    @Test
    void shouldUpdateAuthorityWhenChangingName() {
        // given
        Authority authority = authorityRepository.findById(1L).orElseThrow();
        authority.setName(AuthorityName.ADMIN);

        // when
        Authority updatedAuthority = authorityRepository.save(authority);

        // then
        assertNotNull(updatedAuthority);
        assertEquals(authority.getId(), updatedAuthority.getId());
        assertEquals(AuthorityName.ADMIN, updatedAuthority.getName());
    }

    @Test
    void shouldDeleteAuthorityWhenDeletingByExistingAuthority() {
        // given
        Authority authority = authorityRepository.findById(1L).orElseThrow();

        // when
        authorityRepository.delete(authority);

        // then
        assertFalse(authorityRepository.findById(1L).isPresent());
    }

    @Test
    void shouldDeleteAuthorityWhenDeletingByExistingAuthorityId() {
        // when
        authorityRepository.deleteById(1L);

        // then
        assertFalse(authorityRepository.findById(1L).isPresent());
    }

    @Test
    void shouldReturnTrueWhenExistsByExistingAuthorityId() {
        // when
        boolean exists = authorityRepository.existsById(1L);

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenExistsByNonExistingAuthorityId() {
        // when
        boolean exists = authorityRepository.existsById(100L);

        // then
        assertFalse(exists);
    }

    @Test
    void shouldReturnNotEmptyListWhenFindAll() {
        // when
        List<Authority> authorities = authorityRepository.findAll();

        // then
        assertNotNull(authorities);
        assertFalse(authorities.isEmpty());
    }
}
