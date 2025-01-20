package com.stepaniuk.testhorizon.user;

import com.stepaniuk.testhorizon.payload.user.UserUpdateRequest;
import com.stepaniuk.testhorizon.shared.PageMapperImpl;
import com.stepaniuk.testhorizon.testspecific.ServiceLevelUnitTest;
import com.stepaniuk.testhorizon.user.authority.AuthorityRepository;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchUserByEmailException;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchUserByIdException;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ServiceLevelUnitTest
@ContextConfiguration(classes = {UserService.class, UserMapperImpl.class, PageMapperImpl.class})
class UserServiceTest {
    @Autowired
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthorityRepository authorityRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldReturnUserResponseWhenGetByExistingId() {
        // given
        User userToFind = getNewUserWithAllFields();

        when(userRepository.findById(1L)).thenReturn(Optional.of(userToFind));

        // when
        var userResponse = userService.getUserById(1L);

        // then
        assertNotNull(userResponse);
        assertEquals(userToFind.getId(), userResponse.getId());
        assertEquals(userToFind.getFirstName(), userResponse.getFirstName());
        assertEquals(userToFind.getLastName(), userResponse.getLastName());
        assertEquals(userToFind.getEmail(), userResponse.getEmail());
        assertEquals(userToFind.getCreatedAt(), userResponse.getCreatedAt());
        assertEquals(userToFind.getUpdatedAt(), userResponse.getUpdatedAt());
        assertTrue(userResponse.hasLinks());
    }

    @Test
    void shouldThrowNoSuchUserByIdExceptionWhenGetByNonExistingId() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchUserByIdException.class, () -> userService.getUserById(1L));
    }

    @Test
    void shouldReturnUserResponseWhenGetByExistingEmail(){
        // given
        User userToFind = getNewUserWithAllFields();

        when(userRepository.findByEmail("johndoe@gmail.com")).thenReturn(Optional.of(userToFind));

        // when
        var userResponse = userService.getUserByEmail("johndoe@gmail.com");

        // then
        assertNotNull(userResponse);
        assertEquals(userToFind.getId(), userResponse.getId());
        assertEquals(userToFind.getFirstName(), userResponse.getFirstName());
        assertEquals(userToFind.getLastName(), userResponse.getLastName());
        assertEquals(userToFind.getEmail(), userResponse.getEmail());
        assertEquals(userToFind.getCreatedAt(), userResponse.getCreatedAt());
        assertEquals(userToFind.getUpdatedAt(), userResponse.getUpdatedAt());
        assertTrue(userResponse.hasLinks());
    }

    @Test
    void shouldThrowNoSuchUserByEmailExceptionWhenGetByNonExistingEmail() {
        // given
        when(userRepository.findByEmail("johndoe@gmail.com")).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchUserByEmailException.class, () -> userService.getUserByEmail("johndoe@gmail.com"));
    }

    @Test
    void shouldUpdateAndReturnUserResponseWhenChangingFirstName() {
        // given
        User userToUpdate = getNewUserWithAllFields();
        var userUpdateRequest = new UserUpdateRequest(null, null, "Jane", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userToUpdate));
        when(userRepository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());

        // when
        var userResponse = userService.updateUser(1L, userUpdateRequest);

        // then
        assertNotNull(userResponse);
        assertEquals(userToUpdate.getId(), userResponse.getId());
        assertEquals(userUpdateRequest.getFirstName(), userResponse.getFirstName());
        assertEquals(userToUpdate.getLastName(), userResponse.getLastName());
        assertEquals(userToUpdate.getEmail(), userResponse.getEmail());
        assertEquals(userToUpdate.getCreatedAt(), userResponse.getCreatedAt());
        assertEquals(userToUpdate.getUpdatedAt(), userResponse.getUpdatedAt());
        assertTrue(userResponse.hasLinks());

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowNoSuchUserByIdExceptionWhenChangingFirstNameOfNonExistingUser() {
        // given
        var userUpdateRequest = new UserUpdateRequest(null, null, "Jane", null);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchUserByIdException.class, () -> userService.updateUser(1L, userUpdateRequest));
    }

    @Test
    void shouldDeleteAndReturnVoidWhenDeletingExistingUser() {
        // given
        User userToDelete = getNewUserWithAllFields();

        when(userRepository.findById(1L)).thenReturn(Optional.of(userToDelete));

        // when
        userService.deleteUserById(1L);

        // then
        verify(userRepository, times(1)).delete(userToDelete);
    }

    @Test
    void shouldThrowNoSuchUserByIdExceptionWhenDeletingNonExistingUser() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchUserByIdException.class, () -> userService.deleteUserById(1L));
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllUsers() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        var userToFind = new User(1L, "John", "Doe", "johndoe@gmail.com", 120, "Password+123",
                true, true, true, true, null,
                Set.of(), timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);
        Specification<User> specification = Specification.where(null);

        when(userRepository.findAll(specification, pageable)).thenReturn(
                new PageImpl<>(List.of(userToFind), pageable, 1));
        // when
        var userResponses = userService.getAllUsers(pageable, null);
        var userResponse = userResponses.getContent().iterator().next();

        //then
        assertNotNull(userResponses);
        assertNotNull(userResponses.getMetadata());
        assertEquals(1, userResponses.getMetadata().getTotalElements());
        assertEquals(1, userResponses.getContent().size());

        assertEquals(userToFind.getId(), userResponse.getId());
        assertEquals(userToFind.getFirstName(), userResponse.getFirstName());
        assertEquals(userToFind.getLastName(), userResponse.getLastName());
        assertEquals(userToFind.getTotalRating(), userResponse.getTotalRating());
        assertEquals(userToFind.getEmail(), userResponse.getEmail());
        assertEquals(userToFind.getCreatedAt(), userResponse.getCreatedAt());
        assertEquals(userToFind.getUpdatedAt(), userResponse.getUpdatedAt());
        assertTrue(userResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllUsersAndListOfIdsIsNotEmpty() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        var userToFind = new User(1L, "John", "Doe", "johndoe@gmail.com", 120, "Password+123",
                true, true, true, true, null,
                Set.of(), timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(
                new PageImpl<>(List.of(userToFind), pageable, 1));
        // when
        var userResponses = userService.getAllUsers(pageable, List.of(1L));
        var userResponse = userResponses.getContent().iterator().next();

        //then
        assertNotNull(userResponses);
        assertNotNull(userResponses.getMetadata());
        assertEquals(1, userResponses.getMetadata().getTotalElements());
        assertEquals(1, userResponses.getContent().size());

        assertEquals(userToFind.getId(), userResponse.getId());
        assertEquals(userToFind.getFirstName(), userResponse.getFirstName());
        assertEquals(userToFind.getLastName(), userResponse.getLastName());
        assertEquals(userToFind.getTotalRating(), userResponse.getTotalRating());
        assertEquals(userToFind.getEmail(), userResponse.getEmail());
        assertEquals(userToFind.getCreatedAt(), userResponse.getCreatedAt());
        assertEquals(userToFind.getUpdatedAt(), userResponse.getUpdatedAt());
        assertTrue(userResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingTopUsersByRating(){
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        var userToFind = new User(1L, "John", "Doe", "johndoe@gmail.com", 120, "Password+123",
                true, true, true, true, null,
                Set.of(), timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(
                new PageImpl<>(List.of(userToFind), pageable, 1));
        // when
        var userResponses = userService.getTopUsersByRating(pageable);
        var userResponse = userResponses.getContent().iterator().next();

        //then
        assertNotNull(userResponses);
        assertNotNull(userResponses.getMetadata());
        assertEquals(1, userResponses.getMetadata().getTotalElements());
        assertEquals(1, userResponses.getContent().size());

        assertEquals(userToFind.getId(), userResponse.getId());
        assertEquals(userToFind.getFirstName(), userResponse.getFirstName());
        assertEquals(userToFind.getLastName(), userResponse.getLastName());
        assertEquals(userToFind.getTotalRating(), userResponse.getTotalRating());
        assertEquals(userToFind.getEmail(), userResponse.getEmail());
        assertEquals(userToFind.getCreatedAt(), userResponse.getCreatedAt());
        assertEquals(userToFind.getUpdatedAt(), userResponse.getUpdatedAt());
        assertTrue(userResponse.hasLinks());
    }

    private static User getNewUserWithAllFields(){
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        return new User(null, "John", "Doe", "johndoe@gmail.com", 120, "Password+123",
                true, true, true, true, null,
                Set.of(), timeOfCreation, timeOfModification);
    }
}
