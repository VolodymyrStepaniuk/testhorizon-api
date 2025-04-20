package com.stepaniuk.testhorizon.notebook.note;

import com.stepaniuk.testhorizon.testspecific.JpaLevelTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JpaLevelTest
@Sql(scripts = {"classpath:sql/notebook/note/notes.sql"})
class NoteRepositoryTest {

    @Autowired
    private NoteRepository noteRepository;

    @Test
    void shouldSaveNote() {
        // given
        Note noteToSave = new Note(null, 1L, "New Note", "Note content",
                Instant.now(), Instant.now());

        // when
        Note savedNote = noteRepository.save(noteToSave);

        // then
        assertNotNull(savedNote);
        assertNotNull(savedNote.getId());
        assertEquals(noteToSave.getNotebookId(), savedNote.getNotebookId());
        assertEquals(noteToSave.getTitle(), savedNote.getTitle());
        assertEquals(noteToSave.getContent(), savedNote.getContent());
        assertEquals(noteToSave.getCreatedAt(), savedNote.getCreatedAt());
        assertEquals(noteToSave.getUpdatedAt(), savedNote.getUpdatedAt());
    }

    @Test
    void shouldThrowExceptionWhenSavingNoteWithoutTitle() {
        // given
        Note noteToSave = new Note(null, 1L, null, "Note content",
                Instant.now(), Instant.now());

        // when & then
        assertThrows(DataIntegrityViolationException.class, () -> noteRepository.save(noteToSave));
    }

    @Test
    void shouldReturnNoteWhenFindById() {
        // when
        Optional<Note> optionalNote = noteRepository.findById(1L);

        // then
        assertTrue(optionalNote.isPresent());
        Note note = optionalNote.get();

        assertEquals(1L, note.getId());
        assertEquals(1L, note.getNotebookId());
        assertEquals("Test Note", note.getTitle());
        assertEquals("This is a test note.", note.getContent());
        assertNotNull(note.getCreatedAt());
        assertNotNull(note.getUpdatedAt());
    }

    @Test
    void shouldReturnNotebookIdWhenFindById() {
        // when
        Optional<Note> optionalNote = noteRepository.findById(1L);

        // then
        assertTrue(optionalNote.isPresent());
        Note note = optionalNote.get();

        assertEquals(1L, note.getNotebookId());
    }

    @Test
    void shouldUpdateNoteWhenChangingTitle() {
        // given
        Note noteToUpdate = noteRepository.findById(1L).orElseThrow();
        noteToUpdate.setTitle("Updated Note Title");

        // when
        Note updatedNote = noteRepository.save(noteToUpdate);

        // then
        assertEquals(noteToUpdate.getId(), updatedNote.getId());
        assertEquals("Updated Note Title", updatedNote.getTitle());
    }

    @Test
    void shouldUpdateNoteWhenChangingContent() {
        // given
        Note noteToUpdate = noteRepository.findById(1L).orElseThrow();
        noteToUpdate.setContent("Updated note content text");

        // when
        Note updatedNote = noteRepository.save(noteToUpdate);

        // then
        assertEquals(noteToUpdate.getId(), updatedNote.getId());
        assertEquals("Updated note content text", updatedNote.getContent());
    }

    @Test
    void shouldDeleteNoteWhenDeletingByExistingNote() {
        // given
        Note noteToDelete = noteRepository.findById(1L).orElseThrow();

        // when
        noteRepository.delete(noteToDelete);

        // then
        assertTrue(noteRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldDeleteNoteByIdWhenDeletingByExistingId() {
        // when
        noteRepository.deleteById(1L);

        // then
        assertTrue(noteRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldReturnTrueWhenNoteExists() {
        // when
        boolean exists = noteRepository.existsById(1L);

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenNoteDoesNotExist() {
        // when
        boolean exists = noteRepository.existsById(100L);

        // then
        assertFalse(exists);
    }

    @Test
    void shouldReturnNonEmptyListWhenFindAll() {
        // when
        List<Note> notes = noteRepository.findAll();

        // then
        assertNotNull(notes);
        assertFalse(notes.isEmpty());
    }
}
