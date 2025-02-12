package com.stepaniuk.testhorizon.user;

import com.stepaniuk.testhorizon.event.user.UserDeletedEvent;
import com.stepaniuk.testhorizon.event.user.UserEvent;
import com.stepaniuk.testhorizon.event.user.UserUpdatedEvent;
import com.stepaniuk.testhorizon.payload.user.UserUpdateRequest;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.shared.exceptions.AccessToManageEntityDeniedException;
import com.stepaniuk.testhorizon.shared.PageMapperImpl;
import com.stepaniuk.testhorizon.testspecific.ServiceLevelUnitTest;
import com.stepaniuk.testhorizon.user.authority.AuthorityRepository;
import com.stepaniuk.testhorizon.user.email.EmailCodeRepository;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchUserByEmailException;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchUserByIdException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.stubbing.Answer1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.Mockito.*;

@ServiceLevelUnitTest
@ContextConfiguration(classes = {UserService.class, UserMapperImpl.class, PageMapperImpl.class})
class UserServiceTest {
    @Autowired
    private UserService userService;

    @MockitoBean
    private UserProducer userProducer;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthorityRepository authorityRepository;

    @MockitoBean
    private EmailCodeRepository emailCodeRepository;

    @Test
    void shouldReturnUserResponseWhenGetByExistingId() {
        // given
        User userToFind = getNewUserWithAllFields();
        var authInfo = new AuthInfo(1L, List.of());

        when(userRepository.findById(1L)).thenReturn(Optional.of(userToFind));

        // when
        var userResponse = userService.getUserById(1L, authInfo);

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
        var authInfo = new AuthInfo(1L, List.of());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchUserByIdException.class, () -> userService.getUserById(1L, authInfo));
    }

    @Test
    void shouldReturnUserResponseWhenGetByExistingEmail() {
        // given
        User userToFind = getNewUserWithAllFields();
        var authInfo = new AuthInfo(1L, List.of());

        when(userRepository.findByEmail("johndoe@gmail.com")).thenReturn(Optional.of(userToFind));

        // when
        var userResponse = userService.getUserByEmail("johndoe@gmail.com", authInfo);

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
        var authInfo = new AuthInfo(1L, List.of());
        when(userRepository.findByEmail("johndoe@gmail.com")).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchUserByEmailException.class, () -> userService.getUserByEmail("johndoe@gmail.com", authInfo));
    }

    @Test
    void shouldUpdateAndReturnUserResponseWhenChangingFirstName() {
        // given
        User userToUpdate = getNewUserWithAllFields();
        var userUpdateRequest = new UserUpdateRequest(null, "Jane", null );
        var authInfo = new AuthInfo(1L, List.of());

        when(userRepository.findById(1L)).thenReturn(Optional.of(userToUpdate));
        when(userRepository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
        final var receivedEventWrapper = new UserUpdatedEvent[1];
        when(userProducer.send(
                assertArg(
                        event -> receivedEventWrapper[0] = (UserUpdatedEvent) event))).thenAnswer(
                answer(getFakeSendResult())
        );

        // when
        var userResponse = userService.updateUser(1L, userUpdateRequest, UUID.randomUUID().toString(), authInfo);

        // then
        assertNotNull(userResponse);
        assertEquals(userToUpdate.getId(), userResponse.getId());
        assertEquals(userUpdateRequest.getFirstName(), userResponse.getFirstName());
        assertEquals(userToUpdate.getLastName(), userResponse.getLastName());
        assertEquals(userToUpdate.getEmail(), userResponse.getEmail());
        assertEquals(userToUpdate.getCreatedAt(), userResponse.getCreatedAt());
        assertEquals(userToUpdate.getUpdatedAt(), userResponse.getUpdatedAt());
        assertTrue(userResponse.hasLinks());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(userResponse.getId(), receivedEvent.getUserId());
        assertEquals(userResponse.getFirstName(), receivedEvent.getData().getFirstName());
        assertNull(receivedEvent.getData().getLastName());

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowNoSuchUserByIdExceptionWhenChangingFirstNameOfNonExistingUser() {
        // given
        var userUpdateRequest = new UserUpdateRequest(null, null, "Jane");
        var correlationId = UUID.randomUUID().toString();
        var authInfo = new AuthInfo(1L, List.of());

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchUserByIdException.class, () -> userService.updateUser(1L, userUpdateRequest, correlationId, authInfo));
    }

    @Test
    void shouldThrowAccessToManageEntityDeniedExceptionWhenChangingFirstNameOfUser() {
        // given
        User userToUpdate = getNewUserWithAllFields();
        var userUpdateRequest = new UserUpdateRequest(null, "Jane", null);
        var correlationId = UUID.randomUUID().toString();
        var authInfo = new AuthInfo(2L, List.of());

        when(userRepository.findById(1L)).thenReturn(Optional.of(userToUpdate));

        // when && then
        assertThrows(AccessToManageEntityDeniedException.class, () -> userService.updateUser(1L, userUpdateRequest, correlationId, authInfo));
    }

    @Test
    void shouldDeleteAndReturnVoidWhenDeletingExistingUser() {
        // given
        User userToDelete = getNewUserWithAllFields();
        var authInfo = new AuthInfo(1L, List.of());

        final var receivedEventWrapper = new UserDeletedEvent[1];
        when(userProducer.send(
                assertArg(event -> receivedEventWrapper[0] = (UserDeletedEvent) event))).thenAnswer(
                answer(getFakeSendResult())
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(userToDelete));

        // when
        userService.deleteUserById(1L, UUID.randomUUID().toString(), authInfo);

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(userToDelete.getId(), receivedEvent.getUserId());

        // then
        verify(userRepository, times(1)).delete(userToDelete);
    }

    @Test
    void shouldThrowNoSuchUserByIdExceptionWhenDeletingNonExistingUser() {
        // given
        var correlationId = UUID.randomUUID().toString();
        var authInfo = new AuthInfo(1L, List.of());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchUserByIdException.class, () -> userService.deleteUserById(1L, correlationId, authInfo));
    }

    @Test
    void shouldThrowAccessToManageEntityDeniedExceptionWhenDeletingUser() {
        // given
        User userToDelete = getNewUserWithAllFields();
        var correlationId = UUID.randomUUID().toString();
        var authInfo = new AuthInfo(2L, List.of());
        when(userRepository.findById(1L)).thenReturn(Optional.of(userToDelete));

        // when && then
        assertThrows(AccessToManageEntityDeniedException.class, () -> userService.deleteUserById(1L, correlationId, authInfo));
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllUsers() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        var authInfo = new AuthInfo(1L, List.of());

        var userToFind = new User(1L, "John", "Doe", "johndoe@gmail.com", 120, "Password+123",
                true, true, true, true,
                Set.of(), timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);
        Specification<User> specification = Specification.where(null);

        when(userRepository.findAll(specification, pageable)).thenReturn(
                new PageImpl<>(List.of(userToFind), pageable, 1));
        // when
        var userResponses = userService.getAllUsers(pageable, null, null, null, authInfo);
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
        var authInfo = new AuthInfo(1L, List.of());

        var userToFind = new User(1L, "John", "Doe", "johndoe@gmail.com", 120, "Password+123",
                true, true, true, true,
                Set.of(), timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(
                new PageImpl<>(List.of(userToFind), pageable, 1));
        // when
        var userResponses = userService.getAllUsers(pageable, List.of(1L), null, null, authInfo);
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
    void shouldReturnPagedModelWhenGettingAllUsersAndEmailNotNull() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        var email = "email@mail.com";
        var authInfo = new AuthInfo(1L, List.of());
        var userToFind = new User(1L, "John", "Doe", email, 120, "Password+123",
                true, true, true, true,
                Set.of(), timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(
                new PageImpl<>(List.of(userToFind), pageable, 1));
        // when
        var userResponses = userService.getAllUsers(pageable, null, email, null, authInfo);
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
    void shouldReturnPagedModelWhenGettingAllUsersAndFullNameNotNull() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        var authInfo = new AuthInfo(1L, List.of());
        var fullName = "John Doe";
        var userToFind = new User(1L, "John", "Doe", "", 120, "Password+123",
                true, true, true, true,
                Set.of(), timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(
                new PageImpl<>(List.of(userToFind), pageable, 1));
        // when
        var userResponses = userService.getAllUsers(pageable, null, null, fullName, authInfo);
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
    void shouldReturnPagedModelWhenGettingTopUsersByRating() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        var authInfo = new AuthInfo(1L, List.of());
        var userToFind = new User(1L, "John", "Doe", "johndoe@gmail.com", 120, "Password+123",
                true, true, true, true,
                Set.of(), timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(
                new PageImpl<>(List.of(userToFind), pageable, 1));
        // when
        var userResponses = userService.getTopUsersByRating(pageable,authInfo);
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

    private Answer1<CompletableFuture<SendResult<String, UserEvent>>, UserEvent> getFakeSendResult() {
        return event -> CompletableFuture.completedFuture(
                new SendResult<>(new ProducerRecord<>("users", event),
                        new RecordMetadata(new TopicPartition("users", 0), 0L, 0, 0L, 0, 0)));
    }

    private static User getNewUserWithAllFields() {
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        return new User(1L, "John", "Doe", "johndoe@gmail.com", 120, "Password+123",
                true, true, true, true,
                Set.of(), timeOfCreation, timeOfModification);
    }
}
