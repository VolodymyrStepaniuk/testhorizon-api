package com.stepaniuk.testhorizon.notebook;

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
@Sql(scripts = {"classpath:sql/notebook/notebooks.sql"})
class NotebookRepositoryTest {

    @Autowired
    private NotebookRepository notebookRepository;

    @Test
    void shouldSaveNotebook() {
        // given
        Notebook notebookToSave = new Notebook(null, 1L, "New Notebook", "Notebook content", 
                Instant.now(), Instant.now());

        // when
        Notebook savedNotebook = notebookRepository.save(notebookToSave);

        // then
        assertNotNull(savedNotebook);
        assertNotNull(savedNotebook.getId());
        assertEquals(notebookToSave.getOwnerId(), savedNotebook.getOwnerId());
        assertEquals(notebookToSave.getTitle(), savedNotebook.getTitle());
        assertEquals(notebookToSave.getDescription(), savedNotebook.getDescription());
        assertEquals(notebookToSave.getCreatedAt(), savedNotebook.getCreatedAt());
        assertEquals(notebookToSave.getUpdatedAt(), savedNotebook.getUpdatedAt());
    }

    @Test
    void shouldThrowExceptionWhenSavingNotebookWithoutTitle() {
        // given
        Notebook notebookToSave = new Notebook(null, 1L, null, "Notebook content",
                Instant.now(), Instant.now());

        // when & then
        assertThrows(DataIntegrityViolationException.class, () -> notebookRepository.save(notebookToSave));
    }

    @Test
    void shouldReturnNotebookWhenFindById() {
        // when
        Optional<Notebook> optionalNotebook = notebookRepository.findById(1L);

        // then
        assertTrue(optionalNotebook.isPresent());
        Notebook notebook = optionalNotebook.get();

        assertEquals(1L, notebook.getId());
        assertEquals(1L, notebook.getOwnerId());
        assertEquals("Test Notebook", notebook.getTitle());
        assertEquals("This is a test notebook.", notebook.getDescription());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), notebook.getCreatedAt());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), notebook.getUpdatedAt());
    }

    @Test
    void shouldReturnOwnerIdWhenFindById() {
        // when
        Optional<Notebook> optionalNotebook = notebookRepository.findById(1L);

        // then
        assertTrue(optionalNotebook.isPresent());
        Notebook notebook = optionalNotebook.get();

        assertEquals(1L, notebook.getOwnerId());
    }

    @Test
    void shouldUpdateNotebookWhenChangingTitle() {
        // given
        Notebook notebookToUpdate = notebookRepository.findById(1L).orElseThrow();
        notebookToUpdate.setTitle("Updated Notebook Title");

        // when
        Notebook updatedNotebook = notebookRepository.save(notebookToUpdate);

        // then
        assertEquals(notebookToUpdate.getId(), updatedNotebook.getId());
        assertEquals("Updated Notebook Title", updatedNotebook.getTitle());
    }

    @Test
    void shouldDeleteNotebookWhenDeletingByExistingNotebook() {
        // given
        Notebook notebookToDelete = notebookRepository.findById(1L).orElseThrow();

        // when
        notebookRepository.delete(notebookToDelete);

        // then
        assertTrue(notebookRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldDeleteNotebookByIdWhenDeletingByExistingId() {
        // when
        notebookRepository.deleteById(1L);

        // then
        assertTrue(notebookRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldReturnTrueWhenNotebookExists() {
        // when
        boolean exists = notebookRepository.existsById(1L);

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenNotebookDoesNotExist() {
        // when
        boolean exists = notebookRepository.existsById(100L);

        // then
        assertFalse(exists);
    }

    @Test
    void shouldReturnNonEmptyListWhenFindAll() {
        // when
        List<Notebook> notebooks = notebookRepository.findAll();

        // then
        assertNotNull(notebooks);
        assertFalse(notebooks.isEmpty());
    }
}
