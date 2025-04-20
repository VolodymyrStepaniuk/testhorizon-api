package com.stepaniuk.testhorizon.notebook.note;



import com.stepaniuk.testhorizon.event.notebook.note.NoteCreatedEvent;
import com.stepaniuk.testhorizon.event.notebook.note.NoteDeletedEvent;
import com.stepaniuk.testhorizon.event.notebook.note.NoteEvent;
import com.stepaniuk.testhorizon.event.notebook.note.NoteUpdatedEvent;
import com.stepaniuk.testhorizon.notebook.NotebookService;
import com.stepaniuk.testhorizon.notebook.note.exceptions.NoSuchNoteByIdException;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.payload.notebook.NotebookResponse;
import com.stepaniuk.testhorizon.payload.notebook.note.NoteCreateRequest;
import com.stepaniuk.testhorizon.payload.notebook.note.NoteResponse;
import com.stepaniuk.testhorizon.payload.notebook.note.NoteUpdateRequest;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.shared.PageMapperImpl;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ServiceLevelUnitTest
@ContextConfiguration(classes = {NoteService.class, NoteMapperImpl.class, PageMapperImpl.class})
class NoteServiceTest {

    @Autowired
    private NoteService noteService;

    @MockitoBean
    private NoteRepository noteRepository;

    @MockitoBean
    private NoteMapper noteMapper;

    @MockitoBean
    private NoteProducer noteProducer;

    @MockitoBean
    private NotebookService notebookService;

    @MockitoBean
    private PageMapperImpl pageMapper;

    @Test
    void shouldReturnNoteResponseWhenCreatingNote() {
        // given
        NoteCreateRequest noteCreateRequest = new NoteCreateRequest("title", "content");
        Long notebookId = 1L;
        
        NotebookResponse notebookResponse = mock(NotebookResponse.class);
        when(notebookResponse.getId()).thenReturn(notebookId);
        
        NoteResponse noteResponse = mock(NoteResponse.class);
        
        when(notebookService.getNotebookById(notebookId)).thenReturn(notebookResponse);
        when(noteRepository.save(any())).thenAnswer(answer(getFakeSave(1L)));
        when(noteMapper.toResponse(any())).thenReturn(noteResponse);
        
        final var receivedEventWrapper = new NoteCreatedEvent[1];
        when(
                noteProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (NoteCreatedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        // when
        var response = noteService.createNote(noteCreateRequest, notebookId, UUID.randomUUID().toString());

        // then
        assertNotNull(response);
        verify(noteRepository, times(1)).save(any());
        
        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(1L, receivedEvent.getNoteId());
        assertEquals(notebookId, receivedEvent.getNotebookId());
    }

    @Test
    void shouldReturnNoteResponseWhenGettingNoteById() {
        // given
        Note note = getNewNoteWithAllFields();
        NoteResponse noteResponse = mock(NoteResponse.class);
        
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));
        when(noteMapper.toResponse(note)).thenReturn(noteResponse);

        // when
        var response = noteService.getNoteById(1L);

        // then
        assertNotNull(response);
        assertEquals(noteResponse, response);
    }

    @Test
    void shouldThrowNoSuchNoteByIdExceptionWhenGettingNoteById() {
        // given
        when(noteRepository.findById(10L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchNoteByIdException.class, () -> noteService.getNoteById(10L));
    }

    @Test
    void shouldUpdateAndReturnNoteResponseWhenChangingNoteTitle() {
        // given
        Note noteToUpdate = getNewNoteWithAllFields();
        var noteUpdateRequest = new NoteUpdateRequest("newTitle", null);
        var authInfo = new AuthInfo(1L, List.of());
        
        NotebookResponse notebookResponse = mock(NotebookResponse.class);
        UserInfo userInfo = mock(UserInfo.class);
        when(userInfo.getId()).thenReturn(1L);
        when(notebookResponse.getOwner()).thenReturn(userInfo);
        
        NoteResponse noteResponse = mock(NoteResponse.class);
        
        when(noteRepository.findById(1L)).thenReturn(Optional.of(noteToUpdate));
        when(notebookService.getNotebookById(noteToUpdate.getNotebookId())).thenReturn(notebookResponse);
        when(noteRepository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
        when(noteMapper.toResponse(noteToUpdate)).thenReturn(noteResponse);
        
        var receivedEventWrapper = new NoteUpdatedEvent[1];
        when(
                noteProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (NoteUpdatedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        // when
        var updatedNoteResponse = noteService.updateNote(1L, noteUpdateRequest, UUID.randomUUID().toString(), authInfo);

        // then
        assertNotNull(updatedNoteResponse);
        assertEquals(noteResponse, updatedNoteResponse);
        
        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(noteToUpdate.getId(), receivedEvent.getNoteId());
        assertEquals(noteUpdateRequest.getTitle(), receivedEvent.getData().getTitle());
        assertNull(receivedEvent.getData().getContent());

        verify(noteRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowNoSuchNoteByIdExceptionWhenUpdatingNote() {
        // given
        var authInfo = new AuthInfo(1L, List.of());
        var correlationId = UUID.randomUUID().toString();
        NoteUpdateRequest noteUpdateRequest = new NoteUpdateRequest("newTitle", null);

        when(noteRepository.findById(10L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchNoteByIdException.class, () -> noteService.updateNote(10L, noteUpdateRequest, correlationId, authInfo));
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenUpdatingNote() {
        // given
        var authInfo = new AuthInfo(2L, List.of());
        var correlationId = UUID.randomUUID().toString();
        Note noteToUpdate = getNewNoteWithAllFields();
        var noteUpdateRequest = new NoteUpdateRequest("newTitle", null);
        
        NotebookResponse notebookResponse = mock(NotebookResponse.class);
        UserInfo userInfo = mock(UserInfo.class);
        when(userInfo.getId()).thenReturn(1L);
        when(notebookResponse.getOwner()).thenReturn(userInfo);

        when(noteRepository.findById(1L)).thenReturn(Optional.of(noteToUpdate));
        when(notebookService.getNotebookById(noteToUpdate.getNotebookId())).thenReturn(notebookResponse);

        // when && then
        assertThrows(AccessToManageEntityDeniedException.class, () -> noteService.updateNote(1L, noteUpdateRequest, correlationId, authInfo));
    }

    @Test
    void shouldDeleteAndReturnVoidWhenDeletingExistingNote() {
        // given
        Note noteToDelete = getNewNoteWithAllFields();
        var authInfo = new AuthInfo(1L, List.of());
        
        NotebookResponse notebookResponse = mock(NotebookResponse.class);
        UserInfo userInfo = mock(UserInfo.class);
        when(userInfo.getId()).thenReturn(1L);
        when(notebookResponse.getOwner()).thenReturn(userInfo);
        
        when(noteRepository.findById(1L)).thenReturn(Optional.of(noteToDelete));
        when(notebookService.getNotebookById(noteToDelete.getNotebookId())).thenReturn(notebookResponse);
        
        final var receivedEventWrapper = new NoteDeletedEvent[1];
        when(
                noteProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (NoteDeletedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        // when
        noteService.deleteNoteById(1L, UUID.randomUUID().toString(), authInfo);

        // then
        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(noteToDelete.getId(), receivedEvent.getNoteId());
        
        verify(noteRepository, times(1)).delete(noteToDelete);
    }

    @Test
    void shouldThrowNoSuchNoteByIdExceptionWhenDeletingNote() {
        // given
        var authInfo = new AuthInfo(1L, List.of());
        var correlationId = UUID.randomUUID().toString();
        when(noteRepository.findById(10L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchNoteByIdException.class, () -> noteService.deleteNoteById(10L, correlationId, authInfo));
    }

    @Test
    void shouldThrowAccessToManageEntityDeniedExceptionWhenDeletingNote() {
        // given
        var authInfo = new AuthInfo(2L, List.of());
        var correlationId = UUID.randomUUID().toString();
        Note noteToDelete = getNewNoteWithAllFields();
        
        NotebookResponse notebookResponse = mock(NotebookResponse.class);
        UserInfo userInfo = mock(UserInfo.class);
        when(userInfo.getId()).thenReturn(1L);
        when(notebookResponse.getOwner()).thenReturn(userInfo);
        
        when(noteRepository.findById(1L)).thenReturn(Optional.of(noteToDelete));
        when(notebookService.getNotebookById(noteToDelete.getNotebookId())).thenReturn(notebookResponse);

        // when && then
        assertThrows(AccessToManageEntityDeniedException.class, () -> noteService.deleteNoteById(1L, correlationId, authInfo));
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllNotes() {
        // given

        var noteToFind = getNewNoteWithAllFields();
        var pageable = PageRequest.of(0, 2);
        Specification<Note> specification = Specification.where(null);
        
        var notePage = new PageImpl<>(List.of(noteToFind), pageable, 1);
        var noteResponse = mock(NoteResponse.class);
        var pagedModel = mock(org.springframework.hateoas.PagedModel.class);

        when(noteRepository.findAll(specification, pageable)).thenReturn(notePage);
        when(noteMapper.toResponse(noteToFind)).thenReturn(noteResponse);
        when(pageMapper.toResponse(any(), any())).thenReturn(pagedModel);

        // when
        var notePageResponse = noteService.getAllNotes(pageable, null, null);

        // then
        assertNotNull(notePageResponse);
        assertEquals(pagedModel, notePageResponse);
        verify(pageMapper, times(1)).toResponse(any(), any());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllNotesByNotebookId() {
        // given
        Long notebookId = 1L;
        var noteToFind = getNewNoteWithAllFields();
        var pageable = PageRequest.of(0, 2);
        var notePage = new PageImpl<>(List.of(noteToFind), pageable, 1);
        var noteResponse = mock(NoteResponse.class);
        var pagedModel = mock(org.springframework.hateoas.PagedModel.class);

        when(noteRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(notePage);
        when(noteMapper.toResponse(noteToFind)).thenReturn(noteResponse);
        when(pageMapper.toResponse(any(), any())).thenReturn(pagedModel);

        // when
        var notePageResponse = noteService.getAllNotes(pageable, notebookId, null);

        // then
        assertNotNull(notePageResponse);
        assertEquals(pagedModel, notePageResponse);
        verify(pageMapper, times(1)).toResponse(any(), any());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllNotesByTitle() {
        // given
        String title = "title";
        var noteToFind = getNewNoteWithAllFields();
        var pageable = PageRequest.of(0, 2);
        var notePage = new PageImpl<>(List.of(noteToFind), pageable, 1);
        var noteResponse = mock(NoteResponse.class);
        var pagedModel = mock(org.springframework.hateoas.PagedModel.class);

        when(noteRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(notePage);
        when(noteMapper.toResponse(noteToFind)).thenReturn(noteResponse);
        when(pageMapper.toResponse(any(), any())).thenReturn(pagedModel);

        // when
        var notePageResponse = noteService.getAllNotes(pageable, null, title);

        // then
        assertNotNull(notePageResponse);
        assertEquals(pagedModel, notePageResponse);
        verify(pageMapper, times(1)).toResponse(any(), any());
    }

    private Answer1<Note, Note> getFakeSave(long id) {
        return note -> {
            note.setId(id);
            return note;
        };
    }

    private Answer1<CompletableFuture<SendResult<String, NoteEvent>>, NoteEvent> getFakeSendResult() {
        return event -> CompletableFuture.completedFuture(
                new SendResult<>(new ProducerRecord<>("notes", event),
                        new RecordMetadata(new TopicPartition("notes", 0), 0L, 0, 0L, 0, 0)));
    }

    private Note getNewNoteWithAllFields() {
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        Note note = new Note();
        note.setId(1L);
        note.setNotebookId(1L);
        note.setTitle("title");
        note.setContent("content");
        note.setCreatedAt(timeOfCreation);
        note.setUpdatedAt(timeOfModification);
        
        return note;
    }

    private static <T> T assertArg(java.util.function.Consumer<T> consumer) {
        return argThat(arg -> {
            consumer.accept(arg);
            return true;
        });
    }
}
