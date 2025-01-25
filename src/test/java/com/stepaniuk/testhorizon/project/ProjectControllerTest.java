package com.stepaniuk.testhorizon.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stepaniuk.testhorizon.payload.project.ProjectCreateRequest;
import com.stepaniuk.testhorizon.payload.project.ProjectResponse;
import com.stepaniuk.testhorizon.payload.project.ProjectUpdateRequest;
import com.stepaniuk.testhorizon.project.exception.NoSuchProjectByIdException;
import com.stepaniuk.testhorizon.project.exception.NoSuchProjectStatusByNameException;
import com.stepaniuk.testhorizon.project.status.ProjectStatusName;
import com.stepaniuk.testhorizon.security.config.JwtAuthFilter;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.testspecific.ControllerLevelUnitTest;
import com.stepaniuk.testhorizon.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.Link;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

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
    void shouldReturnProjectResponseWhenCreatingProject() throws Exception {
        // given
        Long userId = 1L;

        mockSecurityContext(userId);

        ProjectCreateRequest projectCreateRequest = new ProjectCreateRequest("title", "description", "instructions",
                "https://github.com", List.of("https://image.com"));

        ProjectResponse projectResponse = createProjectResponse();

        // when
        when(projectService.createProject(eq(projectCreateRequest), eq(userId), any())).thenReturn(projectResponse);

        // then
        mockMvc.perform(post("/projects")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(projectCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(projectResponse.getId()), Long.class))
                .andExpect(jsonPath("$.ownerId", is(projectResponse.getOwnerId()), Long.class))
                .andExpect(jsonPath("$.title", is(projectResponse.getTitle())))
                .andExpect(jsonPath("$.description", is(projectResponse.getDescription())))
                .andExpect(jsonPath("$.instructions", is(projectResponse.getInstructions())))
                .andExpect(jsonPath("$.githubUrl", is(projectResponse.getGithubUrl())))
                .andExpect(jsonPath("$.imageUrls", is(projectResponse.getImageUrls())))
                .andExpect(jsonPath("$.status", is(projectResponse.getStatus().name())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(projectResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(projectResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/projects/1")));

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnErrorResponseWhenCreatingProject() throws Exception {
        // given
        Long userId = 1L;

        mockSecurityContext(userId);

        ProjectCreateRequest projectCreateRequest = new ProjectCreateRequest("title", "description", "instructions",
                "https://github.com", List.of("https://image.com"));

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

        SecurityContextHolder.clearContext();
    }

    @Test
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
                .andExpect(jsonPath("$.ownerId", is(projectResponse.getOwnerId()), Long.class))
                .andExpect(jsonPath("$.title", is(projectResponse.getTitle())))
                .andExpect(jsonPath("$.description", is(projectResponse.getDescription())))
                .andExpect(jsonPath("$.instructions", is(projectResponse.getInstructions())))
                .andExpect(jsonPath("$.githubUrl", is(projectResponse.getGithubUrl())))
                .andExpect(jsonPath("$.imageUrls", is(projectResponse.getImageUrls())))
                .andExpect(jsonPath("$.status", is(projectResponse.getStatus().name())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(projectResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(projectResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/projects/1")));
    }

    @Test
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
    void shouldReturnProjectResponseWhenUpdatingProject() throws Exception {
        // given
        Long projectId = 1L;

        ProjectUpdateRequest projectUpdateRequest = new ProjectUpdateRequest("new title", null,
                null, null, null);

        ProjectResponse projectResponse = createProjectResponse();

        // when
        when(projectService.updateProject(eq(projectId), eq(projectUpdateRequest), any())).thenReturn(projectResponse);

        // then
        mockMvc.perform(patch("/projects/" + projectId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(projectUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(projectResponse.getId()), Long.class))
                .andExpect(jsonPath("$.ownerId", is(projectResponse.getOwnerId()), Long.class))
                .andExpect(jsonPath("$.title", is(projectResponse.getTitle())))
                .andExpect(jsonPath("$.description", is(projectResponse.getDescription())))
                .andExpect(jsonPath("$.instructions", is(projectResponse.getInstructions())))
                .andExpect(jsonPath("$.githubUrl", is(projectResponse.getGithubUrl())))
                .andExpect(jsonPath("$.imageUrls", is(projectResponse.getImageUrls())))
                .andExpect(jsonPath("$.status", is(projectResponse.getStatus().name())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(projectResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(projectResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/projects/1")));
    }

    @Test
    void shouldReturnErrorResponseNoSuchProjectByIdExceptionWhenUpdatingProject() throws Exception {
        // given
        Long projectId = 1L;

        ProjectUpdateRequest projectUpdateRequest = new ProjectUpdateRequest("new title", null,
                null, null, null);

        when(projectService.updateProject(eq(projectId), eq(projectUpdateRequest), any())).thenThrow(
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
    void shouldReturnErrorResponseNoSuchProjectStatusByNameExceptionWhenUpdatingProject() throws Exception {
        // given
        Long projectId = 1L;

        ProjectUpdateRequest projectUpdateRequest = new ProjectUpdateRequest(null, null,
                ProjectStatusName.ACTIVE, null, List.of());

        when(projectService.updateProject(eq(projectId), eq(projectUpdateRequest), any())).thenThrow(
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
    void shouldReturnErrorResponseNoSuchProjectByIdExceptionWhenDeletingProject() throws Exception {
        // given
        Long projectId = 1L;

        doThrow(new NoSuchProjectByIdException(projectId)).
                when(projectService).
                deleteProjectById(eq(projectId), any());

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
                .andExpect(jsonPath("$._embedded.projects[0].ownerId", is(response.getOwnerId()), Long.class))
                .andExpect(jsonPath("$._embedded.projects[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.projects[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.projects[0].instructions", is(response.getInstructions())))
                .andExpect(jsonPath("$._embedded.projects[0].githubUrl", is(response.getGithubUrl())))
                .andExpect(jsonPath("$._embedded.projects[0].imageUrls", is(response.getImageUrls())))
                .andExpect(jsonPath("$._embedded.projects[0].status", is(response.getStatus().name())))
                .andExpect(jsonPath("$._embedded.projects[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.projects[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.projects[0]._links.self.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._embedded.projects[0]._links.update.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._embedded.projects[0]._links.delete.href", is("http://localhost/projects/1")));
    }

    @Test
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
                .andExpect(jsonPath("$._embedded.projects[0].ownerId", is(response.getOwnerId()), Long.class))
                .andExpect(jsonPath("$._embedded.projects[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.projects[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.projects[0].instructions", is(response.getInstructions())))
                .andExpect(jsonPath("$._embedded.projects[0].githubUrl", is(response.getGithubUrl())))
                .andExpect(jsonPath("$._embedded.projects[0].imageUrls", is(response.getImageUrls())))
                .andExpect(jsonPath("$._embedded.projects[0].status", is(response.getStatus().name())))
                .andExpect(jsonPath("$._embedded.projects[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.projects[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.projects[0]._links.self.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._embedded.projects[0]._links.update.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._embedded.projects[0]._links.delete.href", is("http://localhost/projects/1")));
    }

    @Test
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
                .andExpect(jsonPath("$._embedded.projects[0].ownerId", is(response.getOwnerId()), Long.class))
                .andExpect(jsonPath("$._embedded.projects[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.projects[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.projects[0].instructions", is(response.getInstructions())))
                .andExpect(jsonPath("$._embedded.projects[0].githubUrl", is(response.getGithubUrl())))
                .andExpect(jsonPath("$._embedded.projects[0].imageUrls", is(response.getImageUrls())))
                .andExpect(jsonPath("$._embedded.projects[0].status", is(response.getStatus().name())))
                .andExpect(jsonPath("$._embedded.projects[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.projects[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.projects[0]._links.self.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._embedded.projects[0]._links.update.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._embedded.projects[0]._links.delete.href", is("http://localhost/projects/1")));
    }

    @Test
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
                .andExpect(jsonPath("$._embedded.projects[0].ownerId", is(response.getOwnerId()), Long.class))
                .andExpect(jsonPath("$._embedded.projects[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.projects[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.projects[0].instructions", is(response.getInstructions())))
                .andExpect(jsonPath("$._embedded.projects[0].githubUrl", is(response.getGithubUrl())))
                .andExpect(jsonPath("$._embedded.projects[0].imageUrls", is(response.getImageUrls())))
                .andExpect(jsonPath("$._embedded.projects[0].status", is(response.getStatus().name())))
                .andExpect(jsonPath("$._embedded.projects[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.projects[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.projects[0]._links.self.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._embedded.projects[0]._links.update.href", is("http://localhost/projects/1")))
                .andExpect(jsonPath("$._embedded.projects[0]._links.delete.href", is("http://localhost/projects/1")));
    }

    private void mockSecurityContext(Long userId) {
        User mockUser = new User(
                userId,
                "firstName",
                "lastName",
                "email",
                0,
                "password",
                true,
                true,
                true,
                true,
                null,
                Set.of(),
                Instant.now(),
                Instant.now()
        );

        // Мокання SecurityContext для передачі користувача
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(mockUser);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private ProjectResponse createProjectResponse() {
        var timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        var timeOfModification = Instant.now().plus(Duration.ofHours(20));

        var response = new ProjectResponse(
                1L,
                1L,
                "title",
                "description",
                "instructions",
                "https://github.com",
                List.of("https://image.com"),
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
