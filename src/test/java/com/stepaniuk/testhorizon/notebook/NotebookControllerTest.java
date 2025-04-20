package com.stepaniuk.testhorizon.notebook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.payload.notebook.NotebookCreateRequest;
import com.stepaniuk.testhorizon.payload.notebook.NotebookResponse;
import com.stepaniuk.testhorizon.payload.notebook.NotebookUpdateRequest;
import com.stepaniuk.testhorizon.notebook.exceptions.NoSuchNotebookByIdException;
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

@ControllerLevelUnitTest(controllers = NotebookController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotebookControllerTest {

    @MockitoBean
    private NotebookService notebookService;

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
    void shouldReturnNotebookResponseWhenCreatingNotebook() throws Exception {
        // given
        Long userId = 1L;

        NotebookCreateRequest notebookCreateRequest = new NotebookCreateRequest("title", "description");
        NotebookResponse notebookResponse = createNotebookResponse();

        // when
        when(notebookService.createNotebook(eq(notebookCreateRequest), eq(userId), any())).thenReturn(notebookResponse);

        // then
        mockMvc.perform(post("/notebooks")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(notebookCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notebookResponse.getId()), Long.class))
                .andExpect(jsonPath("$.owner.id", is(notebookResponse.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$.owner.firstName", is(notebookResponse.getOwner().getFirstName())))
                .andExpect(jsonPath("$.owner.lastName", is(notebookResponse.getOwner().getLastName())))
                .andExpect(jsonPath("$.title", is(notebookResponse.getTitle())))
                .andExpect(jsonPath("$.description", is(notebookResponse.getDescription())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(notebookResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(notebookResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/notebooks/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/notebooks/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/notebooks/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnNotebookResponseWhenGettingById() throws Exception {
        // given
        Long notebookId = 1L;

        NotebookResponse notebookResponse = createNotebookResponse();

        // when
        when(notebookService.getNotebookById(notebookId)).thenReturn(notebookResponse);

        // then
        mockMvc.perform(get("/notebooks/" + notebookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notebookResponse.getId()), Long.class))
                .andExpect(jsonPath("$.owner.id", is(notebookResponse.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$.owner.firstName", is(notebookResponse.getOwner().getFirstName())))
                .andExpect(jsonPath("$.owner.lastName", is(notebookResponse.getOwner().getLastName())))
                .andExpect(jsonPath("$.title", is(notebookResponse.getTitle())))
                .andExpect(jsonPath("$.description", is(notebookResponse.getDescription())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(notebookResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(notebookResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/notebooks/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/notebooks/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/notebooks/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseWhenGettingById() throws Exception {
        // given
        Long notebookId = 1L;

        when(notebookService.getNotebookById(notebookId)).thenThrow(
                new NoSuchNotebookByIdException(notebookId)
        );

        // when
        mockMvc.perform(get("/notebooks/" + notebookId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such notebook")))
                .andExpect(jsonPath("$.detail", is("No notebook with id " + notebookId)))
                .andExpect(jsonPath("$.instance", is("/notebooks")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnNotebookResponseWhenUpdatingNotebook() throws Exception {
        // given
        Long notebookId = 1L;

        NotebookUpdateRequest notebookUpdateRequest = new NotebookUpdateRequest("new title", null);
        NotebookResponse notebookResponse = createNotebookResponse();

        // when
        when(notebookService.updateNotebook(eq(notebookId), eq(notebookUpdateRequest), any(), any())).thenReturn(notebookResponse);

        // then
        mockMvc.perform(patch("/notebooks/" + notebookId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(notebookUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notebookResponse.getId()), Long.class))
                .andExpect(jsonPath("$.owner.id", is(notebookResponse.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$.owner.firstName", is(notebookResponse.getOwner().getFirstName())))
                .andExpect(jsonPath("$.owner.lastName", is(notebookResponse.getOwner().getLastName())))
                .andExpect(jsonPath("$.title", is(notebookResponse.getTitle())))
                .andExpect(jsonPath("$.description", is(notebookResponse.getDescription())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(notebookResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(notebookResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/notebooks/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/notebooks/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/notebooks/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseNoSuchNotebookByIdExceptionWhenUpdatingNotebook() throws Exception {
        // given
        Long notebookId = 1L;

        NotebookUpdateRequest notebookUpdateRequest = new NotebookUpdateRequest("new title", null);

        when(notebookService.updateNotebook(eq(notebookId), eq(notebookUpdateRequest), any(), any())).thenThrow(
                new NoSuchNotebookByIdException(notebookId)
        );

        // when
        mockMvc.perform(patch("/notebooks/" + notebookId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(notebookUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such notebook")))
                .andExpect(jsonPath("$.detail", is("No notebook with id " + notebookId)))
                .andExpect(jsonPath("$.instance", is("/notebooks")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseAccessToManageEntityDeniedExceptionWhenUpdatingNotebook() throws Exception {
        // given
        Long notebookId = 1L;

        NotebookUpdateRequest notebookUpdateRequest = new NotebookUpdateRequest("new title", null);

        doThrow(new AccessToManageEntityDeniedException("Notebook", "/notebooks"))
                .when(notebookService)
                .updateNotebook(eq(notebookId), eq(notebookUpdateRequest), any(), any());

        // when
        mockMvc.perform(patch("/notebooks/" + notebookId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(notebookUpdateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.title", is("Access denied")))
                .andExpect(jsonPath("$.detail", is("Access to manage Notebook denied")))
                .andExpect(jsonPath("$.instance", is("/notebooks")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnNoContentWhenDeletingNotebook() throws Exception {
        // given
        long notebookId = 1L;

        // when && then
        mockMvc.perform(delete("/notebooks/" + notebookId)
                        .contentType("application/json")
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseNoSuchNotebookByIdExceptionWhenDeletingNotebook() throws Exception {
        // given
        Long notebookId = 1L;

        doThrow(new NoSuchNotebookByIdException(notebookId)).
                when(notebookService).
                deleteNotebookById(eq(notebookId), any(), any());

        // when
        mockMvc.perform(delete("/notebooks/" + notebookId)
                        .contentType("application/json")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such notebook")))
                .andExpect(jsonPath("$.detail", is("No notebook with id " + notebookId)))
                .andExpect(jsonPath("$.instance", is("/notebooks")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseAccessToManageEntityDeniedExceptionWhenDeletingNotebook() throws Exception {
        // given
        Long notebookId = 1L;

        doThrow(new AccessToManageEntityDeniedException("Notebook", "/notebooks"))
                .when(notebookService)
                .deleteNotebookById(eq(notebookId), any(), any());

        // when
        mockMvc.perform(delete("/notebooks/" + notebookId)
                        .contentType("application/json")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.title", is("Access denied")))
                .andExpect(jsonPath("$.detail", is("Access to manage Notebook denied")))
                .andExpect(jsonPath("$.instance", is("/notebooks")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfNotebookResponsesWhenGettingAllNotebooks() throws Exception {
        // given
        var response = createNotebookResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(notebookService.getAllNotebooks(pageable, null, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/notebooks"))
                );

        // then
        mockMvc.perform(get("/notebooks")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.notebooks[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.notebooks[0].owner.id", is(response.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$._embedded.notebooks[0].owner.firstName", is(response.getOwner().getFirstName())))
                .andExpect(jsonPath("$._embedded.notebooks[0].owner.lastName", is(response.getOwner().getLastName())))
                .andExpect(jsonPath("$._embedded.notebooks[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.notebooks[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.notebooks[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.notebooks[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.notebooks[0]._links.self.href", is("http://localhost/notebooks/1")))
                .andExpect(jsonPath("$._embedded.notebooks[0]._links.update.href", is("http://localhost/notebooks/1")))
                .andExpect(jsonPath("$._embedded.notebooks[0]._links.delete.href", is("http://localhost/notebooks/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfNotebookResponsesWhenGettingAllNotebooksByOwnerId() throws Exception {
        // given
        var response = createNotebookResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(notebookService.getAllNotebooks(pageable, 1L, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/notebooks"))
                );

        // then
        mockMvc.perform(get("/notebooks")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("ownerId", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.notebooks[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.notebooks[0].owner.id", is(response.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$._embedded.notebooks[0].owner.firstName", is(response.getOwner().getFirstName())))
                .andExpect(jsonPath("$._embedded.notebooks[0].owner.lastName", is(response.getOwner().getLastName())))
                .andExpect(jsonPath("$._embedded.notebooks[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.notebooks[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.notebooks[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.notebooks[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.notebooks[0]._links.self.href", is("http://localhost/notebooks/1")))
                .andExpect(jsonPath("$._embedded.notebooks[0]._links.update.href", is("http://localhost/notebooks/1")))
                .andExpect(jsonPath("$._embedded.notebooks[0]._links.delete.href", is("http://localhost/notebooks/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfNotebookResponsesWhenGettingAllNotebooksByTitle() throws Exception {
        // given
        var response = createNotebookResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(notebookService.getAllNotebooks(pageable, null, "title"))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/notebooks"))
                );

        // then
        mockMvc.perform(get("/notebooks")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("title", "title")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.notebooks[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.notebooks[0].owner.id", is(response.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$._embedded.notebooks[0].owner.firstName", is(response.getOwner().getFirstName())))
                .andExpect(jsonPath("$._embedded.notebooks[0].owner.lastName", is(response.getOwner().getLastName())))
                .andExpect(jsonPath("$._embedded.notebooks[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.notebooks[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.notebooks[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.notebooks[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.notebooks[0]._links.self.href", is("http://localhost/notebooks/1")))
                .andExpect(jsonPath("$._embedded.notebooks[0]._links.update.href", is("http://localhost/notebooks/1")))
                .andExpect(jsonPath("$._embedded.notebooks[0]._links.delete.href", is("http://localhost/notebooks/1")));
    }

    private NotebookResponse createNotebookResponse() {
        var timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        var timeOfModification = Instant.now().plus(Duration.ofHours(20));

        var response = new NotebookResponse(
                1L,
                new UserInfo(1L, "firstName", "lastName"),
                "title",
                "description",
                timeOfCreation,
                timeOfModification
        );

        response.add(Link.of("http://localhost/notebooks/1", "self"));
        response.add(Link.of("http://localhost/notebooks/1", "update"));
        response.add(Link.of("http://localhost/notebooks/1", "delete"));

        return response;
    }
}
