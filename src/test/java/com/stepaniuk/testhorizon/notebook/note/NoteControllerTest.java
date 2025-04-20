package com.stepaniuk.testhorizon.notebook.note;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stepaniuk.testhorizon.notebook.note.exceptions.NoSuchNoteByIdException;
import com.stepaniuk.testhorizon.payload.notebook.note.NoteCreateRequest;
import com.stepaniuk.testhorizon.payload.notebook.note.NoteResponse;
import com.stepaniuk.testhorizon.payload.notebook.note.NoteUpdateRequest;
import com.stepaniuk.testhorizon.security.config.JwtAuthFilter;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.shared.exceptions.AccessToManageEntityDeniedException;
import com.stepaniuk.testhorizon.testspecific.ControllerLevelUnitTest;
import com.stepaniuk.testhorizon.testspecific.jwt.WithJwtToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.Link;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static com.stepaniuk.testhorizon.testspecific.hamcrest.TemporalStringMatchers.instantComparesEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerLevelUnitTest(controllers = NoteController.class)
@AutoConfigureMockMvc(addFilters = false)
class NoteControllerTest {

    @MockitoBean
    private NoteService noteService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private PageMapper pageMapper;

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnNoteResponseWhenCreatingNote() throws Exception {
        // given
        Long notebookId = 1L;
        NoteCreateRequest noteCreateRequest = new NoteCreateRequest("title", "content");
        NoteResponse noteResponse = createNoteResponse();

        // when
        when(noteService.createNote(eq(noteCreateRequest), eq(notebookId), any())).thenReturn(noteResponse);

        // then
        mockMvc.perform(post("/notes/" + notebookId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(noteCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(noteResponse.getId()), Long.class))
                .andExpect(jsonPath("$.notebookId", is(noteResponse.getNotebookId()), Long.class))
                .andExpect(jsonPath("$.title", is(noteResponse.getTitle())))
                .andExpect(jsonPath("$.content", is(noteResponse.getContent())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(noteResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(noteResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/notes/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/notes/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/notes/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnNoteResponseWhenGettingById() throws Exception {
        // given
        Long noteId = 1L;
        NoteResponse noteResponse = createNoteResponse();

        // when
        when(noteService.getNoteById(noteId)).thenReturn(noteResponse);

        // then
        mockMvc.perform(get("/notes/" + noteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(noteResponse.getId()), Long.class))
                .andExpect(jsonPath("$.notebookId", is(noteResponse.getNotebookId()), Long.class))
                .andExpect(jsonPath("$.title", is(noteResponse.getTitle())))
                .andExpect(jsonPath("$.content", is(noteResponse.getContent())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(noteResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(noteResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/notes/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/notes/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/notes/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseWhenGettingById() throws Exception {
        // given
        Long noteId = 1L;

        when(noteService.getNoteById(noteId)).thenThrow(
                new NoSuchNoteByIdException(noteId)
        );

        // when
        mockMvc.perform(get("/notes/" + noteId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such note")))
                .andExpect(jsonPath("$.detail", is("No note with id " + noteId)))
                .andExpect(jsonPath("$.instance", is("/notes")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnNoteResponseWhenUpdatingNote() throws Exception {
        // given
        Long noteId = 1L;
        NoteUpdateRequest noteUpdateRequest = new NoteUpdateRequest("new title", null);
        NoteResponse noteResponse = createNoteResponse();

        // when
        when(noteService.updateNote(eq(noteId), eq(noteUpdateRequest), any(), any())).thenReturn(noteResponse);

        // then
        mockMvc.perform(patch("/notes/" + noteId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(noteUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(noteResponse.getId()), Long.class))
                .andExpect(jsonPath("$.notebookId", is(noteResponse.getNotebookId()), Long.class))
                .andExpect(jsonPath("$.title", is(noteResponse.getTitle())))
                .andExpect(jsonPath("$.content", is(noteResponse.getContent())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(noteResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(noteResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/notes/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/notes/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/notes/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseNoSuchNoteByIdExceptionWhenUpdatingNote() throws Exception {
        // given
        Long noteId = 1L;
        NoteUpdateRequest noteUpdateRequest = new NoteUpdateRequest("new title", null);

        when(noteService.updateNote(eq(noteId), eq(noteUpdateRequest), any(), any())).thenThrow(
                new NoSuchNoteByIdException(noteId)
        );

        // when
        mockMvc.perform(patch("/notes/" + noteId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(noteUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such note")))
                .andExpect(jsonPath("$.detail", is("No note with id " + noteId)))
                .andExpect(jsonPath("$.instance", is("/notes")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseAccessToManageEntityDeniedExceptionWhenUpdatingNote() throws Exception {
        // given
        Long noteId = 1L;
        NoteUpdateRequest noteUpdateRequest = new NoteUpdateRequest("new title", null);

        doThrow(new AccessToManageEntityDeniedException("Note", "/notes"))
                .when(noteService)
                .updateNote(eq(noteId), eq(noteUpdateRequest), any(), any());

        // when
        mockMvc.perform(patch("/notes/" + noteId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(noteUpdateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.title", is("Access denied")))
                .andExpect(jsonPath("$.detail", is("Access to manage Note denied")))
                .andExpect(jsonPath("$.instance", is("/notes")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnNoContentWhenDeletingNote() throws Exception {
        // given
        long noteId = 1L;

        // when && then
        mockMvc.perform(delete("/notes/" + noteId)
                        .contentType("application/json")
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseNoSuchNoteByIdExceptionWhenDeletingNote() throws Exception {
        // given
        Long noteId = 1L;

        doThrow(new NoSuchNoteByIdException(noteId)).
                when(noteService).
                deleteNoteById(eq(noteId), any(), any());

        // when
        mockMvc.perform(delete("/notes/" + noteId)
                        .contentType("application/json")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such note")))
                .andExpect(jsonPath("$.detail", is("No note with id " + noteId)))
                .andExpect(jsonPath("$.instance", is("/notes")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseAccessToManageEntityDeniedExceptionWhenDeletingNote() throws Exception {
        // given
        Long noteId = 1L;

        doThrow(new AccessToManageEntityDeniedException("Note", "/notes"))
                .when(noteService)
                .deleteNoteById(eq(noteId), any(), any());

        // when
        mockMvc.perform(delete("/notes/" + noteId)
                        .contentType("application/json")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.title", is("Access denied")))
                .andExpect(jsonPath("$.detail", is("Access to manage Note denied")))
                .andExpect(jsonPath("$.instance", is("/notes")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfNoteResponsesWhenGettingAllNotes() throws Exception {
        // given
        var response = createNoteResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(noteService.getAllNotes(pageable, null, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/notes"))
                );

        // then
        mockMvc.perform(get("/notes")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.notes[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.notes[0].notebookId", is(response.getNotebookId()), Long.class))
                .andExpect(jsonPath("$._embedded.notes[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.notes[0].content", is(response.getContent())))
                .andExpect(jsonPath("$._embedded.notes[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.notes[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.notes[0]._links.self.href", is("http://localhost/notes/1")))
                .andExpect(jsonPath("$._embedded.notes[0]._links.update.href", is("http://localhost/notes/1")))
                .andExpect(jsonPath("$._embedded.notes[0]._links.delete.href", is("http://localhost/notes/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfNoteResponsesWhenGettingAllNotesByNotebookId() throws Exception {
        // given
        var response = createNoteResponse();
        var pageable = PageRequest.of(0, 2);
        Long notebookId = 1L;

        // when
        when(noteService.getAllNotes(pageable, notebookId, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/notes"))
                );

        // then
        mockMvc.perform(get("/notes")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("notebookId", notebookId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.notes[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.notes[0].notebookId", is(response.getNotebookId()), Long.class))
                .andExpect(jsonPath("$._embedded.notes[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.notes[0].content", is(response.getContent())))
                .andExpect(jsonPath("$._embedded.notes[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.notes[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.notes[0]._links.self.href", is("http://localhost/notes/1")))
                .andExpect(jsonPath("$._embedded.notes[0]._links.update.href", is("http://localhost/notes/1")))
                .andExpect(jsonPath("$._embedded.notes[0]._links.delete.href", is("http://localhost/notes/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfNoteResponsesWhenGettingAllNotesByTitle() throws Exception {
        // given
        var response = createNoteResponse();
        var pageable = PageRequest.of(0, 2);
        String title = "title";

        // when
        when(noteService.getAllNotes(pageable, null, title))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/notes"))
                );

        // then
        mockMvc.perform(get("/notes")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("title", title)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.notes[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.notes[0].notebookId", is(response.getNotebookId()), Long.class))
                .andExpect(jsonPath("$._embedded.notes[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.notes[0].content", is(response.getContent())))
                .andExpect(jsonPath("$._embedded.notes[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.notes[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.notes[0]._links.self.href", is("http://localhost/notes/1")))
                .andExpect(jsonPath("$._embedded.notes[0]._links.update.href", is("http://localhost/notes/1")))
                .andExpect(jsonPath("$._embedded.notes[0]._links.delete.href", is("http://localhost/notes/1")));
    }

    private NoteResponse createNoteResponse() {
        var timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        var timeOfModification = Instant.now().plus(Duration.ofHours(20));

        var response = new NoteResponse(
                1L,
                1L,
                "title",
                "content",
                timeOfCreation,
                timeOfModification
        );

        response.add(Link.of("http://localhost/notes/1", "self"));
        response.add(Link.of("http://localhost/notes/1", "update"));
        response.add(Link.of("http://localhost/notes/1", "delete"));

        return response;
    }
}
