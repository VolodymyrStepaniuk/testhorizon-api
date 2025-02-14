package com.stepaniuk.testhorizon.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.stepaniuk.testhorizon.testspecific.JpaLevelTest;
import com.stepaniuk.testhorizon.user.authority.Authority;
import com.stepaniuk.testhorizon.types.user.AuthorityName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@JpaLevelTest
@Sql(scripts = {"classpath:sql/user/users.sql", "classpath:sql/user/authorities.sql", "classpath:sql/user/users_has_authorities.sql"})
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveUser() {
        Authority authority = new Authority(1L, AuthorityName.DEVELOPER);
        // given
        User userToSave = new User(null, "John", "Doe", "johndoe@gmail.com", 100,"Password+123",
                true, true, true, true,
                Set.of(authority), null, null);

        // when
        User savedUser = userRepository.save(userToSave);

        // then
        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        assertEquals(userToSave.getFirstName(), savedUser.getFirstName());
        assertEquals(userToSave.getLastName(), savedUser.getLastName());
        assertEquals(userToSave.getEmail(), savedUser.getEmail());
        assertEquals(userToSave.isEnabled(), savedUser.isEnabled());
        assertEquals(userToSave.isAccountNonExpired(), savedUser.isAccountNonExpired());
        assertEquals(userToSave.isAccountNonLocked(), savedUser.isAccountNonLocked());
        assertEquals(userToSave.isCredentialsNonExpired(), savedUser.isCredentialsNonExpired());
        assertEquals(userToSave.getCreatedAt(), savedUser.getCreatedAt());
        assertEquals(userToSave.getUpdatedAt(), savedUser.getUpdatedAt());
    }

    @Test
    void shouldThrowExceptionWhenSavingUserWithoutEmail() {
        Authority authority = new Authority(1L, AuthorityName.DEVELOPER);
        // given
        User userToSave = new User(null, "John", "Doe", null, 10,null,
                true, true, true, true,
                Set.of(authority), null, null);

        // when && then
        assertThrows(DataIntegrityViolationException.class, () -> userRepository.save(userToSave));
    }

    @Test
    void shouldReturnUserWhenFindById() {
        // when
        Optional<User> optionalUser = userRepository.findById(1L);

        // then
        assertTrue(optionalUser.isPresent());
        User user = optionalUser.get();

        assertEquals(1L, user.getId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals(121, user.getTotalRating());
        assertEquals("random.email@example.com", user.getEmail());
        assertTrue(user.isEnabled());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), user.getCreatedAt());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), user.getUpdatedAt());
    }

    @Test
    void shouldReturnUserWhenFindByEmail() {
        // when
        Optional<User> optionalUser = userRepository.findByEmail("random.email@example.com");

        // then
        assertTrue(optionalUser.isPresent());
        User user = optionalUser.get();

        assertEquals(1L, user.getId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals(121, user.getTotalRating());
        assertEquals("random.email@example.com", user.getEmail());
        assertTrue(user.isEnabled());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), user.getCreatedAt());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), user.getUpdatedAt());
    }

    @Test
    void shouldReturnUserWithAuthoritiesWhenFindById() {
        // when
        Optional<User> optionalUser = userRepository.findById(1L);

        // then
        assertTrue(optionalUser.isPresent());
        User user = optionalUser.get();

        assertEquals(1L, user.getId());
        assertFalse(user.getAuthorities().isEmpty());
    }

    @Test
    void shouldReturnUserWithAuthoritiesWhenFindByEmail() {
        // when
        Optional<User> optionalUser = userRepository.findByEmail("random.email@example.com");

        // then
        assertTrue(optionalUser.isPresent());
        User user = optionalUser.get();

        assertEquals(1L, user.getId());
        assertFalse(user.getAuthorities().isEmpty());
    }

    @Test
    void shouldUpdateUserWhenChangingFirstName() {
        // given
        User userToUpdate = userRepository.findById(1L).orElseThrow();
        userToUpdate.setFirstName("Jane");

        // when
        User updatedUser = userRepository.save(userToUpdate);

        // then
        assertEquals(userToUpdate.getId(), updatedUser.getId());
        assertEquals("Jane", updatedUser.getFirstName());
    }

    @Test
    void shouldDeleteUserWhenDeletingByExistingUser() {
        // given
        User userToDelete = userRepository.findById(1L).orElseThrow();

        // when
        userRepository.delete(userToDelete);

        // then
        assertTrue(userRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldDeleteUserByIdWhenDeletingByExistingId() {
        // when
        userRepository.deleteById(1L);

        // then
        assertTrue(userRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldReturnTrueWhenUserExists() {
        // when
        boolean exists = userRepository.existsById(1L);

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotExist() {
        // when
        boolean exists = userRepository.existsById(100L);

        // then
        assertFalse(exists);
    }

    @Test
    void shouldReturnTrueWhenUserExistsByEmail() {
        // when
        boolean exists = userRepository.existsByEmail("random.email@example.com");

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnNonEmptyListWhenFindAll() {
        // when
        List<User> users = userRepository.findAll();

        // then
        assertNotNull(users);
        assertFalse(users.isEmpty());
    }
}
