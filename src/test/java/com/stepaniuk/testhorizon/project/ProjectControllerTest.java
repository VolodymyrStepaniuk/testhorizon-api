package com.stepaniuk.testhorizon.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.payload.project.ProjectCreateRequest;
import com.stepaniuk.testhorizon.payload.project.ProjectResponse;
import com.stepaniuk.testhorizon.payload.project.ProjectUpdateRequest;
import com.stepaniuk.testhorizon.project.exceptions.NoSuchProjectByIdException;
import com.stepaniuk.testhorizon.project.exceptions.NoSuchProjectStatusByNameException;
import com.stepaniuk.testhorizon.types.project.ProjectStatusName;
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

@ControllerLevelUnitTest(controllers = ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @MockitoBean
    private ProjectService projectService;

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
    void shouldReturnProjectResponseWhenCreatingProject() throws Exception {
        // given
        Long userId = 1L;

        ProjectCreateRequest projectCreateRequest = new ProjectCreateRequest("title", "description", "instructions",
                "https://github.com");

        ProjectResponse projectResponse = createProjectResponse();

        // when
        when(projectService.createProject(eq(projectCreateRequest), eq(userId), any())).thenReturn(projectResponse);

        // then
        mockMvc.perform(post("/projects")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(projectCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(projectResponse.getId()), Long.class))
                .andExpect(jsonPath("$.owner.id", is(projectResponse.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$.owner.firstName", is(projectResponse.getOwner().getFirstName())))
                .andExpect(jsonPath("$.owner.lastName", is(projectResponse.getOwner().getLastName())))
                .andExpect(jsonPath("$.title", is(projectResponse.getTitle())))
                .andExpect(jsonPath("$.description", is(projectResponse.getDescription())))
                .andExpect(jsonPath("$.instructions", is(projectResponse.getInstructions())))
                .andExpect(jsonPath("$.githubUrl", is(projectResponse.getGithubUrl())))
                .andExpect(jsonPath("$.status", is(projectResponse.getStatus().name())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(projectResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(projectResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/projects/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseWhenCreatingProject() throws Exception {
        // given
        Long userId = 1L;

        ProjectCreateRequest projectCreateRequest = new ProjectCreateRequest("title", "description", "instructions",
                "https://github.com");

        when(projectService.createProject(eq(projectCreateRequest), eq(userId), any())).thenThrow(
                new NoSuchProjectStatusByNameException(ProjectStatusName.ACTIVE)
        );

        // when
        mockMvc.perform(post("/projects")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(projectCreateRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such project status")))
                .andExpect(jsonPath("$.detail", is("No project status with name " + ProjectStatusName.ACTIVE)))
                .andExpect(jsonPath("$.instance", is("/projects")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnProjectResponseWhenGettingById() throws Exception {
        // given
        Long projectId = 1L;

        ProjectResponse projectResponse = createProjectResponse();

        // when
        when(projectService.getProjectById(projectId)).thenReturn(projectResponse);

        // then
        mockMvc.perform(get("/projects/" + projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(projectResponse.getId()), Long.class))
                .andExpect(jsonPath("$.owner.id", is(projectResponse.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$.owner.firstName", is(projectResponse.getOwner().getFirstName())))
                .andExpect(jsonPath("$.owner.lastName", is(projectResponse.getOwner().getLastName())))
                .andExpect(jsonPath("$.title", is(projectResponse.getTitle())))
                .andExpect(jsonPath("$.description", is(projectResponse.getDescription())))
                .andExpect(jsonPath("$.instructions", is(projectResponse.getInstructions())))
                .andExpect(jsonPath("$.githubUrl", is(projectResponse.getGithubUrl())))
                .andExpect(jsonPath("$.status", is(projectResponse.getStatus().name())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(projectResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(projectResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/projects/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseWhenGettingById() throws Exception {
        // given
        Long projectId = 1L;

        when(projectService.getProjectById(projectId)).thenThrow(
                new NoSuchProjectByIdException(projectId)
        );

        // when
        mockMvc.perform(get("/projects/" + projectId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such project")))
                .andExpect(jsonPath("$.detail", is("No project with id " + projectId)))
                .andExpect(jsonPath("$.instance", is("/projects")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnProjectResponseWhenUpdatingProject() throws Exception {
        // given
        Long projectId = 1L;

        ProjectUpdateRequest projectUpdateRequest = new ProjectUpdateRequest("new title", null,
                null, null);

        ProjectResponse projectResponse = createProjectResponse();

        // when
        when(projectService.updateProject(eq(projectId), eq(projectUpdateRequest), any(), any())).thenReturn(projectResponse);

        // then
        mockMvc.perform(patch("/projects/" + projectId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(projectUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(projectResponse.getId()), Long.class))
                .andExpect(jsonPath("$.owner.id", is(projectResponse.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$.owner.firstName", is(projectResponse.getOwner().getFirstName())))
                .andExpect(jsonPath("$.owner.lastName", is(projectResponse.getOwner().getLastName())))
                .andExpect(jsonPath("$.title", is(projectResponse.getTitle())))
                .andExpect(jsonPath("$.description", is(projectResponse.getDescription())))
                .andExpect(jsonPath("$.instructions", is(projectResponse.getInstructions())))
                .andExpect(jsonPath("$.githubUrl", is(projectResponse.getGithubUrl())))
                .andExpect(jsonPath("$.status", is(projectResponse.getStatus().name())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(projectResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(projectResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/projects/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseNoSuchProjectByIdExceptionWhenUpdatingProject() throws Exception {
        // given
        Long projectId = 1L;

        ProjectUpdateRequest projectUpdateRequest = new ProjectUpdateRequest("new title", null,
                null, null);

        when(projectService.updateProject(eq(projectId), eq(projectUpdateRequest), any(), any())).thenThrow(
                new NoSuchProjectByIdException(projectId)
        );

        // when
        mockMvc.perform(patch("/projects/" + projectId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(projectUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such project")))
                .andExpect(jsonPath("$.detail", is("No project with id " + projectId)))
                .andExpect(jsonPath("$.instance", is("/projects")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseNoSuchProjectStatusByNameExceptionWhenUpdatingProject() throws Exception {
        // given
        Long projectId = 1L;

        ProjectUpdateRequest projectUpdateRequest = new ProjectUpdateRequest(null, null,
                ProjectStatusName.ACTIVE, null);

        when(projectService.updateProject(eq(projectId), eq(projectUpdateRequest), any(), any())).thenThrow(
                new NoSuchProjectStatusByNameException(projectUpdateRequest.getStatus())
        );

        // when
        mockMvc.perform(patch("/projects/" + projectId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(projectUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such project status")))
                .andExpect(jsonPath("$.detail", is("No project status with name " + projectUpdateRequest.getStatus())))
                .andExpect(jsonPath("$.instance", is("/projects")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseAccessToManageEntityDeniedExceptionWhenUpdatingProject() throws Exception{
        // given
        Long projectId = 1L;

        ProjectUpdateRequest projectUpdateRequest = new ProjectUpdateRequest(null, null,
                ProjectStatusName.ACTIVE, null);

        doThrow(new AccessToManageEntityDeniedException("Project", "/projects"))
                .when(projectService)
                .updateProject(eq(projectId), eq(projectUpdateRequest), any(), any());

        // when
        mockMvc.perform(patch("/projects/" + projectId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(projectUpdateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.title", is("Access denied")))
                .andExpect(jsonPath("$.detail", is("Access to manage Project denied")))
                .andExpect(jsonPath("$.instance", is("/projects")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnNoContentWhenDeletingProject() throws Exception {
        // given
        long projectId = 1L;

        // when && then
        mockMvc.perform(delete("/projects/" + projectId)
                        .contentType("application/json")
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseNoSuchProjectByIdExceptionWhenDeletingProject() throws Exception {
        // given
        Long projectId = 1L;

        doThrow(new NoSuchProjectByIdException(projectId)).
                when(projectService).
                deleteProjectById(eq(projectId), any(), any());

        // when
        mockMvc.perform(delete("/projects/" + projectId)
                        .contentType("application/json")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such project")))
                .andExpect(jsonPath("$.detail", is("No project with id " + projectId)))
                .andExpect(jsonPath("$.instance", is("/projects")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseAccessToManageEntityDeniedExceptionWhenDeletingProject() throws Exception {
        // given
        Long projectId = 1L;

        doThrow(new AccessToManageEntityDeniedException("Project", "/projects"))
                .when(projectService)
                .deleteProjectById(eq(projectId), any(), any());

        // when
        mockMvc.perform(delete("/projects/" + projectId)
                        .contentType("application/json")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.title", is("Access denied")))
                .andExpect(jsonPath("$.detail", is("Access to manage Project denied")))
                .andExpect(jsonPath("$.instance", is("/projects")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfProjectResponsesWhenGettingAllProjects() throws Exception {
        // given
        var response = createProjectResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(projectService.getAllProjects(pageable, null, null, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/projects"))
                );

        // then
        mockMvc.perform(get("/projects")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.projects[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.projects[0].owner.id", is(response.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$._embedded.projects[0].owner.firstName", is(response.getOwner().getFirstName())))
                .andExpect(jsonPath("$._embedded.projects[0].owner.lastName", is(response.getOwner().getLastName())))
                .andExpect(jsonPath("$._embedded.projects[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.projects[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.projects[0].instructions", is(response.getInstructions())))
                .andExpect(jsonPath("$._embedded.projects[0].githubUrl", is(response.getGithubUrl())))
                .andExpect(jsonPath("$._embedded.projects[0].status", is(response.getStatus().name())))
                .andExpect(jsonPath("$._embedded.projects[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.projects[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.projects[0]._links.self.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._embedded.projects[0]._links.update.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._embedded.projects[0]._links.delete.href", is("http://localhost/projects/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfProjectResponsesWhenGettingAllProjectsByOwnerId() throws Exception {
        // given
        var response = createProjectResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(projectService.getAllProjects(pageable, 1L, null, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/projects"))
                );

        // then
        mockMvc.perform(get("/projects")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("ownerId", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.projects[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.projects[0].owner.id", is(response.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$._embedded.projects[0].owner.firstName", is(response.getOwner().getFirstName())))
                .andExpect(jsonPath("$._embedded.projects[0].owner.lastName", is(response.getOwner().getLastName())))
                .andExpect(jsonPath("$._embedded.projects[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.projects[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.projects[0].instructions", is(response.getInstructions())))
                .andExpect(jsonPath("$._embedded.projects[0].githubUrl", is(response.getGithubUrl())))
                .andExpect(jsonPath("$._embedded.projects[0].status", is(response.getStatus().name())))
                .andExpect(jsonPath("$._embedded.projects[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.projects[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.projects[0]._links.self.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._embedded.projects[0]._links.update.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._embedded.projects[0]._links.delete.href", is("http://localhost/projects/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfProjectResponsesWhenGettingAllProjectsByTitle() throws Exception {
        // given
        var response = createProjectResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(projectService.getAllProjects(pageable, null, "title", null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/projects"))
                );

        // then
        mockMvc.perform(get("/projects")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("title", "title")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.projects[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.projects[0].owner.id", is(response.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$._embedded.projects[0].owner.firstName", is(response.getOwner().getFirstName())))
                .andExpect(jsonPath("$._embedded.projects[0].owner.lastName", is(response.getOwner().getLastName())))
                .andExpect(jsonPath("$._embedded.projects[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.projects[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.projects[0].instructions", is(response.getInstructions())))
                .andExpect(jsonPath("$._embedded.projects[0].githubUrl", is(response.getGithubUrl())))
                .andExpect(jsonPath("$._embedded.projects[0].status", is(response.getStatus().name())))
                .andExpect(jsonPath("$._embedded.projects[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.projects[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.projects[0]._links.self.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._embedded.projects[0]._links.update.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._embedded.projects[0]._links.delete.href", is("http://localhost/projects/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfProjectResponsesWhenGettingAllProjectsByStatus() throws Exception {
        // given
        var response = createProjectResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(projectService.getAllProjects(pageable, null, null, ProjectStatusName.ACTIVE))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/projects"))
                );

        // then
        mockMvc.perform(get("/projects")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("status", ProjectStatusName.ACTIVE.name())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.projects[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.projects[0].owner.id", is(response.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$._embedded.projects[0].owner.firstName", is(response.getOwner().getFirstName())))
                .andExpect(jsonPath("$._embedded.projects[0].owner.lastName", is(response.getOwner().getLastName())))
                .andExpect(jsonPath("$._embedded.projects[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.projects[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.projects[0].instructions", is(response.getInstructions())))
                .andExpect(jsonPath("$._embedded.projects[0].githubUrl", is(response.getGithubUrl())))
                .andExpect(jsonPath("$._embedded.projects[0].status", is(response.getStatus().name())))
                .andExpect(jsonPath("$._embedded.projects[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.projects[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.projects[0]._links.self.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._embedded.projects[0]._links.update.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._embedded.projects[0]._links.delete.href", is("http://localhost/projects/1")));
    }

    private ProjectResponse createProjectResponse() {
        var timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        var timeOfModification = Instant.now().plus(Duration.ofHours(20));

        var response = new ProjectResponse(
                1L,
                new UserInfo(1L, "firstName", "lastName"),
                "title",
                "description",
                "instructions",
                "https://github.com",
                ProjectStatusName.ACTIVE,
                timeOfCreation,
                timeOfModification
        );

        response.add(Link.of("http://localhost/projects/1", "self"));
        response.add(Link.of("http://localhost/projects/1", "update"));
        response.add(Link.of("http://localhost/projects/1", "delete"));

        return response;
    }
}
