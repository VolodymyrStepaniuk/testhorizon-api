package com.stepaniuk.testhorizon.notebook.note;

import com.stepaniuk.testhorizon.payload.notebook.note.NoteCreateRequest;
import com.stepaniuk.testhorizon.payload.notebook.note.NoteResponse;
import com.stepaniuk.testhorizon.payload.notebook.note.NoteUpdateRequest;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/notes", produces = "application/json")
@Validated
public class NoteController {

    private final NoteService noteService;

    @PostMapping("/{notebookId}")
    public ResponseEntity<NoteResponse> createNote(
            @PathVariable Long notebookId,
            @Valid @RequestBody NoteCreateRequest noteCreateRequest
    ) {
        return new ResponseEntity<>(
                noteService.createNote(noteCreateRequest, notebookId,  UUID.randomUUID().toString()),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getNoteById(@PathVariable Long id) {
        return ResponseEntity.ok(noteService.getNoteById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody NoteUpdateRequest noteUpdateRequest,
            AuthInfo authInfo
    ) {
        return ResponseEntity.ok(noteService.updateNote(id, noteUpdateRequest, UUID.randomUUID().toString(), authInfo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNoteById(@PathVariable Long id, AuthInfo authInfo) {
        noteService.deleteNoteById(id, UUID.randomUUID().toString(), authInfo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedModel<NoteResponse>> getAllNotes(Pageable pageable,
                                                                @RequestParam(required = false) Long notebookId,
                                                                @RequestParam(required = false) String title
    ) {
        return ResponseEntity.ok(noteService.getAllNotes(pageable, notebookId, title));
    }
}
