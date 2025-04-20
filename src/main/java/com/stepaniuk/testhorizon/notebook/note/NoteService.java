package com.stepaniuk.testhorizon.notebook.note;

import com.stepaniuk.testhorizon.event.notebook.note.NoteCreatedEvent;
import com.stepaniuk.testhorizon.event.notebook.note.NoteDeletedEvent;
import com.stepaniuk.testhorizon.event.notebook.note.NoteUpdatedEvent;
import com.stepaniuk.testhorizon.notebook.NotebookService;
import com.stepaniuk.testhorizon.notebook.note.exceptions.NoSuchNoteByIdException;
import com.stepaniuk.testhorizon.payload.notebook.note.NoteCreateRequest;
import com.stepaniuk.testhorizon.payload.notebook.note.NoteResponse;
import com.stepaniuk.testhorizon.payload.notebook.note.NoteUpdateRequest;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.shared.exceptions.AccessToManageEntityDeniedException;
import com.stepaniuk.testhorizon.types.user.AuthorityName;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import static com.stepaniuk.testhorizon.security.SecurityUtils.hasAuthority;
import static com.stepaniuk.testhorizon.security.SecurityUtils.isOwner;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;
    private final NoteProducer noteProducer;
    private final PageMapper pageMapper;
    private final NotebookService notebookService;

    public NoteResponse createNote(NoteCreateRequest noteCreateRequest, Long notebookId, String correlationId) {
        var notebook = notebookService.getNotebookById(notebookId);

        var note = new Note();
        note.setTitle(noteCreateRequest.getTitle());
        note.setContent(noteCreateRequest.getContent());
        note.setNotebookId(notebook.getId());

        var savedNote = noteRepository.save(note);

        noteProducer.send(
                new NoteCreatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        savedNote.getId(),notebook.getId()
                )
        );

        return noteMapper.toResponse(savedNote);
    }

    public NoteResponse getNoteById(Long id) {
        return noteRepository.findById(id)
                .map(noteMapper::toResponse)
                .orElseThrow(() -> new NoSuchNoteByIdException(id));
    }

    public void deleteNoteById(Long id, String correlationId, AuthInfo authInfo) {
        var note = noteRepository.findById(id)
                .orElseThrow(() -> new NoSuchNoteByIdException(id));

        var notebook = notebookService.getNotebookById(note.getNotebookId());
        var notebookOwnerId = notebook.getOwner().getId();

        if (hasNoAccessToManageNote(notebookOwnerId, authInfo)) {
            throw new AccessToManageEntityDeniedException("Note", "/notes");
        }

        noteRepository.delete(note);

        noteProducer.send(
                new NoteDeletedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        id
                )
        );
    }

    public NoteResponse updateNote(Long id, NoteUpdateRequest noteUpdateRequest, String correlationId, AuthInfo authInfo) {
        var note = noteRepository.findById(id)
                .orElseThrow(() -> new NoSuchNoteByIdException(id));

        var notebook = notebookService.getNotebookById(note.getNotebookId());
        var notebookOwnerId = notebook.getOwner().getId();

        if (hasNoAccessToManageNote(notebookOwnerId, authInfo)) {
            throw new AccessToManageEntityDeniedException("Note", "/notes");
        }

        var noteData = new NoteUpdatedEvent.Data();

        if (noteUpdateRequest.getTitle() != null) {
            note.setTitle(noteUpdateRequest.getTitle());
            noteData.setTitle(noteUpdateRequest.getTitle());
        }

        if (noteUpdateRequest.getContent() != null) {
            note.setContent(noteUpdateRequest.getContent());
            noteData.setContent(noteUpdateRequest.getContent());
        }

        var updatedNote = noteRepository.save(note);

        noteProducer.send(
                new NoteUpdatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        updatedNote.getId(), noteData
                )
        );

        return noteMapper.toResponse(updatedNote);
    }

    public PagedModel<NoteResponse> getAllNotes(Pageable pageable, @Nullable Long notebookId, @Nullable String title) {
        Specification<Note> specification = Specification.where(null);

        if (notebookId != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get("notebookId"), notebookId)
            );
        }

        if (title != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%")
            );
        }

        var notesPage = noteRepository.findAll(specification, pageable);

        return pageMapper.toResponse(
                notesPage.map(noteMapper::toResponse),
                URI.create("/notes")
        );
    }

    private boolean hasNoAccessToManageNote(Long ownerId, AuthInfo authInfo) {
        return !(isOwner(authInfo, ownerId) || hasAuthority(authInfo, AuthorityName.ADMIN.name()));
    }
}
