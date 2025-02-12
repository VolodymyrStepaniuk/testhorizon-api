package com.stepaniuk.testhorizon.aws.s3;

import com.stepaniuk.testhorizon.testspecific.JpaLevelTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@JpaLevelTest
@Sql(scripts = {"classpath:sql/file/files.sql"})
class FileRepositoryTest {

    @Autowired
    private FileRepository fileRepository;

    @Test
    void shouldSaveFile() {
        // given
        File fileToSave = new File(null, "originalName", "entityType", 1L, Instant.now());

        // when
        File savedFile = fileRepository.save(fileToSave);

        // then
        assertNotNull(savedFile);
        assertNotNull(savedFile.getId());
        assertEquals(fileToSave.getOriginalName(), savedFile.getOriginalName());
        assertEquals(fileToSave.getEntityType(), savedFile.getEntityType());
        assertEquals(fileToSave.getEntityId(), savedFile.getEntityId());
        assertEquals(fileToSave.getCreatedAt(), savedFile.getCreatedAt());
    }

    @Test
    void shouldThrowExceptionWhenSavingFileWithoutOriginalName() {
        // given
        File fileToSave = new File(null, null, "entityType", 1L, Instant.now());

        // when & then
        assertThrows(DataIntegrityViolationException.class, () -> fileRepository.save(fileToSave));
    }

    @Test
    void shouldReturnFileWhenFindById() {
        // when
        Optional<File> optionalFile = fileRepository.findById(
              UUID.fromString("00000000-0000-0000-0000-000000000001")
        );

        // then
        assertTrue(optionalFile.isPresent());
        File file = optionalFile.get();

        assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), file.getId());
        assertEquals("file.txt", file.getOriginalName());
        assertEquals("TEST", file.getEntityType());
        assertEquals(1L, file.getEntityId());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), file.getCreatedAt());
    }

    @Test
    void shouldUpdateFileWhenChangingOriginalName() {
        // given
        String originalName = "newName.txt";
        File fileToUpdate = fileRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000001")).orElseThrow();
        fileToUpdate.setOriginalName(originalName);

        // when
        File updatedFile = fileRepository.save(fileToUpdate);

        // then
        assertEquals(fileToUpdate.getId(), updatedFile.getId());
        assertEquals(originalName, updatedFile.getOriginalName());
    }

    @Test
    void shouldDeleteFileWhenDeletingByExistingFile() {
        // given
        File fileToDelete = fileRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000001")).orElseThrow();

        // when
        fileRepository.delete(fileToDelete);

        // then
        assertTrue(fileRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000001")).isEmpty());
    }

    @Test
    void shouldDeleteFileByIdWhenDeletingByExistingId() {
        // when
        fileRepository.deleteById(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // then
        assertTrue(fileRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000001")).isEmpty());
    }

    @Test
    void shouldReturnTrueWhenFileExists() {
        // when
        boolean exists = fileRepository.existsById(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenFileDoesNotExist() {
        // when
        boolean exists = fileRepository.existsById(UUID.fromString("00000000-0000-0000-0000-000000000100"));

        // then
        assertFalse(exists);
    }

    @Test
    void shouldReturnTrueWhenFileExistsByOriginalNameAndEntityTypeAndEntityId() {
        // when
        boolean exists = fileRepository.existsByOriginalNameAndEntityTypeAndEntityId("file.txt", "TEST", 1L);

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenFileDoesNotExistByOriginalNameAndEntityTypeAndEntityId() {
        // when
        boolean exists = fileRepository.existsByOriginalNameAndEntityTypeAndEntityId("file.txt", "TEST", 100L);

        // then
        assertFalse(exists);
    }

    @Test
    void shouldReturnNonEmptyListWhenFindAll() {
        // when
        List<File> files = fileRepository.findAll();

        // then
        assertNotNull(files);
        assertFalse(files.isEmpty());
    }
}
