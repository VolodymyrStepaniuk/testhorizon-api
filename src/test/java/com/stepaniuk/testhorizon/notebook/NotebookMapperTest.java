package com.stepaniuk.testhorizon.notebook;

import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.payload.notebook.NotebookResponse;
import com.stepaniuk.testhorizon.testspecific.MapperLevelUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@MapperLevelUnitTest
@ContextConfiguration(classes = {NotebookMapperImpl.class})
class NotebookMapperTest {

    @Autowired
    private NotebookMapper notebookMapper;
    
    @Test
    void shouldMapNotebookToNotebookResponse() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        
        Notebook notebook = new Notebook(1L, 1L, "Notebook title", "Notebook description",
                timeOfCreation, timeOfModification);
        UserInfo owner = new UserInfo(1L, "firstName", "lastName");
        
        // when
        NotebookResponse notebookResponse = notebookMapper.toResponse(notebook, owner);
        
        // then
        assertNotNull(notebookResponse);
        assertNotNull(notebookResponse.getOwner());
        assertEquals(notebook.getOwnerId(), notebookResponse.getOwner().getId());
        assertEquals(owner.getFirstName(), notebookResponse.getOwner().getFirstName());
        assertEquals(owner.getLastName(), notebookResponse.getOwner().getLastName());
        assertEquals(notebook.getTitle(), notebookResponse.getTitle());
        assertEquals(notebook.getDescription(), notebookResponse.getDescription());
        assertEquals(notebook.getCreatedAt(), notebookResponse.getCreatedAt());
        assertEquals(notebook.getUpdatedAt(), notebookResponse.getUpdatedAt());
        assertTrue(notebookResponse.hasLinks());
        assertTrue(notebookResponse.getLinks().hasLink("self"));
        assertTrue(notebookResponse.getLinks().hasLink("update"));
        assertTrue(notebookResponse.getLinks().hasLink("delete"));
    }
}
