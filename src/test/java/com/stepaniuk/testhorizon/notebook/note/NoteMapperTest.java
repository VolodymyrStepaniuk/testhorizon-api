package com.stepaniuk.testhorizon.notebook.note;

import com.stepaniuk.testhorizon.payload.notebook.note.NoteResponse;
import com.stepaniuk.testhorizon.testspecific.MapperLevelUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@MapperLevelUnitTest
@ContextConfiguration(classes = {NoteMapperImpl.class})
class NoteMapperTest {

    @Autowired
    private NoteMapper noteMapper;

    @Test
    void shouldMapNoteToNoteResponse() {
        // given
        Instant timeOfCreation = Instant.now().minus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().minus(Duration.ofHours(5));
        
        Note note = new Note(1L, 2L, "Note title", "Note content",
                timeOfCreation, timeOfModification);
        
        // when
        NoteResponse noteResponse = noteMapper.toResponse(note);
        
        // then
        assertNotNull(noteResponse);
        assertEquals(note.getId(), noteResponse.getId());
        assertEquals(note.getNotebookId(), noteResponse.getNotebookId());
        assertEquals(note.getTitle(), noteResponse.getTitle());
        assertEquals(note.getContent(), noteResponse.getContent());
        assertEquals(note.getCreatedAt(), noteResponse.getCreatedAt());
        assertEquals(note.getUpdatedAt(), noteResponse.getUpdatedAt());
        
        // verify links
        assertTrue(noteResponse.hasLinks());
        assertTrue(noteResponse.getLinks().hasLink("self"));
        assertTrue(noteResponse.getLinks().hasLink("update"));
        assertTrue(noteResponse.getLinks().hasLink("delete"));
        
        // verify link values
        assertEquals("/notes/1", noteResponse.getLinks().getRequiredLink("self").getHref());
        assertEquals("/notes/1", noteResponse.getLinks().getRequiredLink("update").getHref());
        assertEquals("/notes/1", noteResponse.getLinks().getRequiredLink("delete").getHref());
    }
}
