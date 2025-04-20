package com.stepaniuk.testhorizon.notebook;

import com.stepaniuk.testhorizon.event.notebook.NotebookCreatedEvent;
import com.stepaniuk.testhorizon.event.notebook.NotebookDeletedEvent;
import com.stepaniuk.testhorizon.event.notebook.NotebookEvent;
import com.stepaniuk.testhorizon.event.notebook.NotebookUpdatedEvent;
import com.stepaniuk.testhorizon.notebook.exceptions.NoSuchNotebookByIdException;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.payload.notebook.NotebookCreateRequest;
import com.stepaniuk.testhorizon.payload.notebook.NotebookUpdateRequest;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.shared.PageMapperImpl;
import com.stepaniuk.testhorizon.shared.UserInfoService;
import com.stepaniuk.testhorizon.shared.exceptions.AccessToManageEntityDeniedException;
import com.stepaniuk.testhorizon.testspecific.ServiceLevelUnitTest;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ServiceLevelUnitTest
@ContextConfiguration(classes = {NotebookService.class, NotebookMapperImpl.class, PageMapperImpl.class})
class NotebookServiceTest {

    @Autowired
    private NotebookService notebookService;

    @MockitoBean
    private NotebookProducer notebookProducer;

    @MockitoBean
    private NotebookRepository notebookRepository;

    @MockitoBean
    private UserInfoService userInfoService;

    @Test
    void shouldReturnNotebookResponseWhenCreatingNotebook() {
        // given
        NotebookCreateRequest notebookCreateRequest = new NotebookCreateRequest("title", "description");

        var userInfo = new UserInfo(1L, "firstName", "lastName");

        when(userInfoService.getUserInfo(1L)).thenReturn(userInfo);
        when(notebookRepository.save(any())).thenAnswer(answer(getFakeSave(1L)));
        final var receivedEventWrapper = new NotebookCreatedEvent[1];
        when(
                notebookProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (NotebookCreatedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        // when
        var notebookResponse = notebookService.createNotebook(notebookCreateRequest, 1L, UUID.randomUUID().toString());

        // then
        assertNotNull(notebookResponse);
        assertEquals(1L, notebookResponse.getOwner().getId());
        assertNotNull(notebookResponse.getOwner());
        assertEquals(userInfo.getFirstName(), notebookResponse.getOwner().getFirstName());
        assertEquals(userInfo.getLastName(), notebookResponse.getOwner().getLastName());
        assertEquals(notebookCreateRequest.getTitle(), notebookResponse.getTitle());
        assertEquals(notebookCreateRequest.getDescription(), notebookResponse.getDescription());
        assertTrue(notebookResponse.hasLinks());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(1L, receivedEvent.getNotebookId());
        assertEquals(notebookResponse.getOwner().getId(), receivedEvent.getOwnerId());

        verify(notebookRepository, times(1)).save(any());
    }

    @Test
    void shouldReturnNotebookResponseWhenGettingNotebookById() {
        // given
        Notebook notebook = getNewNotebookWithAllFields();
        var userInfo = new UserInfo(1L, "firstName", "lastName");

        when(userInfoService.getUserInfo(1L)).thenReturn(userInfo);
        when(notebookRepository.findById(1L)).thenReturn(Optional.of(notebook));

        // when
        var notebookResponse = notebookService.getNotebookById(1L);

        // then
        assertNotNull(notebookResponse);
        assertEquals(notebook.getId(), notebookResponse.getId());
        assertNotNull(notebookResponse.getOwner());
        assertEquals(notebook.getOwnerId(), notebookResponse.getOwner().getId());
        assertEquals(userInfo.getFirstName(), notebookResponse.getOwner().getFirstName());
        assertEquals(userInfo.getLastName(), notebookResponse.getOwner().getLastName());
        assertEquals(notebook.getTitle(), notebookResponse.getTitle());
        assertEquals(notebook.getDescription(), notebookResponse.getDescription());
        assertEquals(notebook.getCreatedAt(), notebookResponse.getCreatedAt());
        assertEquals(notebook.getUpdatedAt(), notebookResponse.getUpdatedAt());
        assertTrue(notebookResponse.hasLinks());
    }

    @Test
    void shouldThrowNoSuchNotebookByIdExceptionWhenGettingNotebookById() {
        // given
        when(notebookRepository.findById(10L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchNotebookByIdException.class, () -> notebookService.getNotebookById(1L));
    }

    @Test
    void shouldUpdateAndReturnNotebookResponseWhenChangingNotebookTitle() {
        // given
        Notebook notebookToUpdate = getNewNotebookWithAllFields();
        var userInfo = new UserInfo(1L, "firstName", "lastName");

        when(userInfoService.getUserInfo(1L)).thenReturn(userInfo);
        var notebookUpdateRequest = new NotebookUpdateRequest("newTitle", null);
        var authInfo = new AuthInfo(1L, List.of());
        when(notebookRepository.findById(1L)).thenReturn(Optional.of(notebookToUpdate));
        when(notebookRepository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
        var receivedEventWrapper = new NotebookUpdatedEvent[1];
        when(
                notebookProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (NotebookUpdatedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        // when
        var updatedNotebookResponse = notebookService.updateNotebook(1L, notebookUpdateRequest, UUID.randomUUID().toString(), authInfo);

        // then
        assertNotNull(updatedNotebookResponse);
        assertEquals(notebookToUpdate.getId(), updatedNotebookResponse.getId());
        assertNotNull(updatedNotebookResponse.getOwner());
        assertEquals(notebookToUpdate.getOwnerId(), updatedNotebookResponse.getOwner().getId());
        assertEquals(userInfo.getFirstName(), updatedNotebookResponse.getOwner().getFirstName());
        assertEquals(userInfo.getLastName(), updatedNotebookResponse.getOwner().getLastName());
        assertEquals(notebookUpdateRequest.getTitle(), updatedNotebookResponse.getTitle());
        assertEquals(notebookToUpdate.getDescription(), updatedNotebookResponse.getDescription());
        assertEquals(notebookToUpdate.getCreatedAt(), updatedNotebookResponse.getCreatedAt());
        assertEquals(notebookToUpdate.getUpdatedAt(), updatedNotebookResponse.getUpdatedAt());
        assertTrue(updatedNotebookResponse.hasLinks());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(updatedNotebookResponse.getId(), receivedEvent.getNotebookId());
        assertEquals(updatedNotebookResponse.getTitle(), receivedEvent.getData().getTitle());
        assertNull(receivedEvent.getData().getDescription());

        verify(notebookRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowNoSuchNotebookByIdExceptionWhenUpdatingNotebook() {
        // given
        var authInfo = new AuthInfo(1L, List.of());
        var correlationId = UUID.randomUUID().toString();
        NotebookUpdateRequest notebookUpdateRequest = new NotebookUpdateRequest("newTitle", null);

        when(notebookRepository.findById(10L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchNotebookByIdException.class, () -> notebookService.updateNotebook(10L, notebookUpdateRequest, correlationId, authInfo));
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenUpdatingNotebook() {
        // given
        var authInfo = new AuthInfo(2L, List.of());
        var correlationId = UUID.randomUUID().toString();
        Notebook notebookToUpdate = getNewNotebookWithAllFields();
        var notebookUpdateRequest = new NotebookUpdateRequest("newTitle", null);

        when(notebookRepository.findById(1L)).thenReturn(Optional.of(notebookToUpdate));

        // when && then
        assertThrows(AccessToManageEntityDeniedException.class, () -> notebookService.updateNotebook(1L, notebookUpdateRequest, correlationId, authInfo));
    }

    @Test
    void shouldDeleteAndReturnVoidWhenDeletingExistingNotebook() {
        // given
        Notebook notebookToDelete = getNewNotebookWithAllFields();
        var authInfo = new AuthInfo(1L, List.of());
        when(notebookRepository.findById(1L)).thenReturn(Optional.of(notebookToDelete));
        final var receivedEventWrapper = new NotebookDeletedEvent[1];
        when(
                notebookProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (NotebookDeletedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        // when
        notebookService.deleteNotebookById(1L, UUID.randomUUID().toString(), authInfo);

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(notebookToDelete.getId(), receivedEvent.getNotebookId());
        // then
        verify(notebookRepository, times(1)).delete(notebookToDelete);
    }

    @Test
    void shouldThrowNoSuchNotebookByIdExceptionWhenDeletingNotebook() {
        // given
        var authInfo = new AuthInfo(1L, List.of());
        var correlationId = UUID.randomUUID().toString();
        when(notebookRepository.findById(10L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchNotebookByIdException.class, () -> notebookService.deleteNotebookById(10L, correlationId, authInfo));
    }

    @Test
    void shouldThrowAccessToManageEntityDeniedExceptionWhenDeletingNotebook() {
        // given
        var authInfo = new AuthInfo(2L, List.of());
        var correlationId = UUID.randomUUID().toString();
        Notebook notebookToDelete = getNewNotebookWithAllFields();
        when(notebookRepository.findById(1L)).thenReturn(Optional.of(notebookToDelete));

        // when && then
        assertThrows(AccessToManageEntityDeniedException.class, () -> notebookService.deleteNotebookById(1L, correlationId, authInfo));
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllNotebooks() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        var userInfo = new UserInfo(1L, "firstName", "lastName");

        var notebookToFind = new Notebook(1L, 1L, "title", "description", timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);
        Specification<Notebook> specification = Specification.where(null);

        when(userInfoService.getUserInfo(1L)).thenReturn(userInfo);
        when(notebookRepository.findAll(specification, pageable)).thenReturn(new PageImpl<>(List.of(notebookToFind), pageable, 1));

        var notebookPageResponse = notebookService.getAllNotebooks(pageable, null, null);
        var notebookResponse = notebookPageResponse.getContent().iterator().next();

        // then
        assertNotNull(notebookPageResponse);
        assertNotNull(notebookPageResponse.getMetadata());
        assertEquals(1, notebookPageResponse.getMetadata().getTotalElements());
        assertEquals(1, notebookPageResponse.getContent().size());

        assertNotNull(notebookResponse);
        assertEquals(notebookToFind.getId(), notebookResponse.getId());
        assertNotNull(notebookResponse.getOwner());
        assertEquals(notebookToFind.getOwnerId(), notebookResponse.getOwner().getId());
        assertEquals(userInfo.getFirstName(), notebookResponse.getOwner().getFirstName());
        assertEquals(userInfo.getLastName(), notebookResponse.getOwner().getLastName());
        assertEquals(notebookToFind.getTitle(), notebookResponse.getTitle());
        assertEquals(notebookToFind.getDescription(), notebookResponse.getDescription());
        assertEquals(notebookToFind.getCreatedAt(), notebookResponse.getCreatedAt());
        assertEquals(notebookToFind.getUpdatedAt(), notebookResponse.getUpdatedAt());
        assertTrue(notebookResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllNotebooksByOwnerId() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        Long ownerId = 1L;

        var userInfo = new UserInfo(1L, "firstName", "lastName");
        var notebookToFind = new Notebook(1L, ownerId, "title", "description", timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);

        when(userInfoService.getUserInfo(ownerId)).thenReturn(userInfo);
        when(notebookRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(notebookToFind), pageable, 1));

        var notebookPageResponse = notebookService.getAllNotebooks(pageable, ownerId, null);
        var notebookResponse = notebookPageResponse.getContent().iterator().next();

        // then
        assertNotNull(notebookPageResponse);
        assertNotNull(notebookPageResponse.getMetadata());
        assertEquals(1, notebookPageResponse.getMetadata().getTotalElements());
        assertEquals(1, notebookPageResponse.getContent().size());

        assertNotNull(notebookResponse);
        assertEquals(notebookToFind.getId(), notebookResponse.getId());
        assertNotNull(notebookResponse.getOwner());
        assertEquals(ownerId, notebookResponse.getOwner().getId());
        assertEquals(userInfo.getFirstName(), notebookResponse.getOwner().getFirstName());
        assertEquals(userInfo.getLastName(), notebookResponse.getOwner().getLastName());
        assertEquals(notebookToFind.getTitle(), notebookResponse.getTitle());
        assertEquals(notebookToFind.getDescription(), notebookResponse.getDescription());
        assertEquals(notebookToFind.getCreatedAt(), notebookResponse.getCreatedAt());
        assertEquals(notebookToFind.getUpdatedAt(), notebookResponse.getUpdatedAt());
        assertTrue(notebookResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllNotebooksByTitle() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        String title = "title";
        var userInfo = new UserInfo(1L, "firstName", "lastName");
        var notebookToFind = new Notebook(1L, 1L, title, "description", timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);

        when(userInfoService.getUserInfo(1L)).thenReturn(userInfo);
        when(notebookRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(notebookToFind), pageable, 1));

        var notebookPageResponse = notebookService.getAllNotebooks(pageable, null, title);
        var notebookResponse = notebookPageResponse.getContent().iterator().next();

        // then
        assertNotNull(notebookPageResponse);
        assertNotNull(notebookPageResponse.getMetadata());
        assertEquals(1, notebookPageResponse.getMetadata().getTotalElements());
        assertEquals(1, notebookPageResponse.getContent().size());

        assertNotNull(notebookResponse);
        assertEquals(notebookToFind.getId(), notebookResponse.getId());
        assertNotNull(notebookResponse.getOwner());
        assertEquals(notebookToFind.getOwnerId(), notebookResponse.getOwner().getId());
        assertEquals(userInfo.getFirstName(), notebookResponse.getOwner().getFirstName());
        assertEquals(userInfo.getLastName(), notebookResponse.getOwner().getLastName());
        assertEquals(title, notebookResponse.getTitle());
        assertEquals(notebookToFind.getDescription(), notebookResponse.getDescription());
        assertEquals(notebookToFind.getCreatedAt(), notebookResponse.getCreatedAt());
        assertEquals(notebookToFind.getUpdatedAt(), notebookResponse.getUpdatedAt());
        assertTrue(notebookResponse.hasLinks());
    }

    private Answer1<Notebook, Notebook> getFakeSave(long id) {
        return notebook -> {
            notebook.setId(id);
            return notebook;
        };
    }

    private Answer1<CompletableFuture<SendResult<String, NotebookEvent>>, NotebookEvent> getFakeSendResult() {
        return event -> CompletableFuture.completedFuture(
                new SendResult<>(new ProducerRecord<>("notebooks", event),
                        new RecordMetadata(new TopicPartition("notebooks", 0), 0L, 0, 0L, 0, 0)));
    }

    private Notebook getNewNotebookWithAllFields() {
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        return new Notebook(1L, 1L, "title", "description", timeOfCreation, timeOfModification);
    }

    private static <T> T assertArg(java.util.function.Consumer<T> consumer) {
        return argThat(arg -> {
            consumer.accept(arg);
            return true;
        });
    }
}
