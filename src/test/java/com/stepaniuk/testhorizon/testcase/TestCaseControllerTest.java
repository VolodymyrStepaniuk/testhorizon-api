package com.stepaniuk.testhorizon.testcase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stepaniuk.testhorizon.payload.testcase.TestCaseCreateRequest;
import com.stepaniuk.testhorizon.payload.testcase.TestCaseResponse;
import com.stepaniuk.testhorizon.payload.testcase.TestCaseUpdateRequest;
import com.stepaniuk.testhorizon.security.config.JwtAuthFilter;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.testcase.exceptions.NoSuchTestCaseByIdException;
import com.stepaniuk.testhorizon.testcase.exceptions.NoSuchTestCasePriorityByNameException;
import com.stepaniuk.testhorizon.testcase.priority.TestCasePriorityName;
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

@ControllerLevelUnitTest(controllers = TestCaseController.class)
@AutoConfigureMockMvc(addFilters = false)
class TestCaseControllerTest {

    @MockitoBean
    private TestCaseService testCaseService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;
    @Autowired
    private PageMapper pageMapper;

    @Test
    void shouldReturnTestCaseWhenCreatingTestCase() throws Exception {
        // given
        Long userId = 1L;

        mockSecurityContext(userId);

        TestCaseCreateRequest request = new TestCaseCreateRequest(
                1L,
                "title",
                "description",
                "preconditions",
                "inputData",
                List.of("step1", "step2"),
                TestCasePriorityName.HIGH
        );

        TestCaseResponse response = createResponse();

        // when
        when(testCaseService.createTestCase(request, userId)).thenReturn(response);

        // then
        mockMvc.perform(post("/test-cases")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.projectId", is(response.getProjectId()), Long.class))
                .andExpect(jsonPath("$.authorId", is(response.getAuthorId()), Long.class))
                .andExpect(jsonPath("$.title", is(response.getTitle())))
                .andExpect(jsonPath("$.description", is(response.getDescription())))
                .andExpect(jsonPath("$.preconditions", is(response.getPreconditions())))
                .andExpect(jsonPath("$.inputData", is(response.getInputData())))
                .andExpect(jsonPath("$.steps", is(response.getSteps())))
                .andExpect(jsonPath("$.priority", is(response.getPriority().name())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/test-cases/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/test-cases/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/test-cases/1")));

        SecurityContextHolder.clearContext();

    }

    @Test
    void shouldReturnErrorResponseWhenCreatingTestCase() throws Exception {
        // given
        Long userId = 1L;

        mockSecurityContext(userId);

        TestCaseCreateRequest request = new TestCaseCreateRequest(
                1L,
                "title",
                "description",
                "preconditions",
                "inputData",
                List.of("step1", "step2"),
                TestCasePriorityName.HIGH
        );

        // when
        when(testCaseService.createTestCase(request, userId)).thenThrow(new NoSuchTestCasePriorityByNameException(request.getPriority()));

        // then
        mockMvc.perform(post("/test-cases")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such test case priority")))
                .andExpect(jsonPath("$.detail", is("No test case priority with name " + request.getPriority())))
                .andExpect(jsonPath("$.instance", is("/test-cases")));

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnTestCaseResponseWhenGettingById() throws Exception {
        Long testCaseId = 1L;

        TestCaseResponse response = createResponse();

        when(testCaseService.getTestCaseById(testCaseId)).thenReturn(response);

        mockMvc.perform(get("/test-cases/" + testCaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.projectId", is(response.getProjectId()), Long.class))
                .andExpect(jsonPath("$.authorId", is(response.getAuthorId()), Long.class))
                .andExpect(jsonPath("$.title", is(response.getTitle())))
                .andExpect(jsonPath("$.description", is(response.getDescription())))
                .andExpect(jsonPath("$.preconditions", is(response.getPreconditions())))
                .andExpect(jsonPath("$.inputData", is(response.getInputData())))
                .andExpect(jsonPath("$.steps", is(response.getSteps())))
                .andExpect(jsonPath("$.priority", is(response.getPriority().name())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/test-cases/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/test-cases/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/test-cases/1")));
    }

    @Test
    void shouldReturnErrorResponseWhenGettingById() throws Exception {
        Long testCaseId = 1L;

        when(testCaseService.getTestCaseById(testCaseId)).thenThrow(new NoSuchTestCaseByIdException(testCaseId));

        mockMvc.perform(get("/test-cases/" + testCaseId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such test case")))
                .andExpect(jsonPath("$.detail", is("No test case with id " + testCaseId)))
                .andExpect(jsonPath("$.instance", is("/test-cases")));
    }

    @Test
    void shouldReturnTestCaseResponseWhenUpdatingTestCase() throws Exception {
        Long testCaseId = 1L;

        TestCaseUpdateRequest request = new TestCaseUpdateRequest(
                "new title",
                "new description",
                "new preconditions",
                "new inputData",
                List.of("new step1", "new step2"),
                TestCasePriorityName.LOW
        );

        TestCaseResponse response = createResponse();

        when(testCaseService.updateTestCase(testCaseId, request)).thenReturn(response);

        mockMvc.perform(patch("/test-cases/" + testCaseId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.projectId", is(response.getProjectId()), Long.class))
                .andExpect(jsonPath("$.authorId", is(response.getAuthorId()), Long.class))
                .andExpect(jsonPath("$.title", is(response.getTitle())))
                .andExpect(jsonPath("$.description", is(response.getDescription())))
                .andExpect(jsonPath("$.preconditions", is(response.getPreconditions())))
                .andExpect(jsonPath("$.inputData", is(response.getInputData())))
                .andExpect(jsonPath("$.steps", is(response.getSteps())))
                .andExpect(jsonPath("$.priority", is(response.getPriority().name())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/test-cases/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/test-cases/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/test-cases/1")));
    }

    @Test
    void shouldReturnErrorResponseNoSuchTestCaseByIdExceptionWhenUpdatingTestCase() throws Exception {
        Long testCaseId = 1L;

        TestCaseUpdateRequest request = new TestCaseUpdateRequest(
                "new title",
                "new description",
                "new preconditions",
                "new inputData",
                List.of("new step1", "new step2"),
                TestCasePriorityName.LOW
        );

        when(testCaseService.updateTestCase(testCaseId, request)).thenThrow(new NoSuchTestCaseByIdException(testCaseId));

        mockMvc.perform(patch("/test-cases/" + testCaseId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such test case")))
                .andExpect(jsonPath("$.detail", is("No test case with id " + testCaseId)))
                .andExpect(jsonPath("$.instance", is("/test-cases")));
    }

    @Test
    void shouldReturnErrorResponseNoSuchTestCasePriorityByNameExceptionWhenUpdatingTestCase() throws Exception {
        Long testCaseId = 1L;

        TestCaseUpdateRequest request = new TestCaseUpdateRequest(
                "new title",
                "new description",
                "new preconditions",
                "new inputData",
                List.of("new step1", "new step2"),
                TestCasePriorityName.LOW
        );

        when(testCaseService.updateTestCase(testCaseId, request)).thenThrow(new NoSuchTestCasePriorityByNameException(request.getPriority()));

        mockMvc.perform(patch("/test-cases/" + testCaseId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such test case priority")))
                .andExpect(jsonPath("$.detail", is("No test case priority with name " + request.getPriority())))
                .andExpect(jsonPath("$.instance", is("/test-cases")));
    }

    @Test
    void shouldReturnNoContentWhenDeletingTestCase() throws Exception {
        long testCaseId = 1L;

        // when && then
        mockMvc.perform(delete("/test-cases/" + testCaseId)
                        .contentType("application/json")
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnErrorResponseWhenDeletingTestCase() throws Exception {
        Long testCaseId = 1L;

        // when
        doThrow(new NoSuchTestCaseByIdException(testCaseId))
                .when(testCaseService)
                .deleteTestCaseById(testCaseId);

        // then
        mockMvc.perform(delete("/test-cases/" + testCaseId)
                        .contentType("application/json")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such test case")))
                .andExpect(jsonPath("$.detail", is("No test case with id " + testCaseId)))
                .andExpect(jsonPath("$.instance", is("/test-cases")));
    }

    @Test
    void shouldReturnPageOfTestCaseResponsesWhenGettingAllTestCases() throws Exception {
        // given
        var response = createResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(testCaseService.getAllTestCases(pageable, null, null, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/test-cases"))
                );

        // then
        mockMvc.perform(get("/test-cases")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.testCases[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.testCases[0].projectId", is(response.getProjectId()), Long.class))
                .andExpect(jsonPath("$._embedded.testCases[0].authorId", is(response.getAuthorId()), Long.class))
                .andExpect(jsonPath("$._embedded.testCases[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.testCases[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.testCases[0].preconditions", is(response.getPreconditions())))
                .andExpect(jsonPath("$._embedded.testCases[0].inputData", is(response.getInputData())))
                .andExpect(jsonPath("$._embedded.testCases[0].steps", is(response.getSteps())))
                .andExpect(jsonPath("$._embedded.testCases[0].priority", is(response.getPriority().name())))
                .andExpect(jsonPath("$._embedded.testCases[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.testCases[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.testCases[0]._links.self.href", is("http://localhost/test-cases/1")))
                .andExpect(jsonPath("$._embedded.testCases[0]._links.update.href", is("http://localhost/test-cases/1")))
                .andExpect(jsonPath("$._embedded.testCases[0]._links.delete.href", is("http://localhost/test-cases/1")));
    }

    @Test
    void shouldReturnPageOfTestCaseResponsesWhenGettingAllTestCasesWhenProjectIdNotNull() throws Exception {
        // given
        var response = createResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(testCaseService.getAllTestCases(pageable, response.getProjectId(), null, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/test-cases"))
                );

        // then
        mockMvc.perform(get("/test-cases")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("projectId", response.getId().toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.testCases[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.testCases[0].projectId", is(response.getProjectId()), Long.class))
                .andExpect(jsonPath("$._embedded.testCases[0].authorId", is(response.getAuthorId()), Long.class))
                .andExpect(jsonPath("$._embedded.testCases[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.testCases[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.testCases[0].preconditions", is(response.getPreconditions())))
                .andExpect(jsonPath("$._embedded.testCases[0].inputData", is(response.getInputData())))
                .andExpect(jsonPath("$._embedded.testCases[0].steps", is(response.getSteps())))
                .andExpect(jsonPath("$._embedded.testCases[0].priority", is(response.getPriority().name())))
                .andExpect(jsonPath("$._embedded.testCases[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.testCases[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.testCases[0]._links.self.href", is("http://localhost/test-cases/1")))
                .andExpect(jsonPath("$._embedded.testCases[0]._links.update.href", is("http://localhost/test-cases/1")))
                .andExpect(jsonPath("$._embedded.testCases[0]._links.delete.href", is("http://localhost/test-cases/1")));
    }

    @Test
    void shouldReturnPageOfTestCaseResponsesWhenGettingAllTestCasesWhenAuthorIdNotNull() throws Exception {
        // given
        var response = createResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(testCaseService.getAllTestCases(pageable, null, response.getAuthorId(), null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/test-cases"))
                );

        // then
        mockMvc.perform(get("/test-cases")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("authorId", response.getAuthorId().toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.testCases[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.testCases[0].projectId", is(response.getProjectId()), Long.class))
                .andExpect(jsonPath("$._embedded.testCases[0].authorId", is(response.getAuthorId()), Long.class))
                .andExpect(jsonPath("$._embedded.testCases[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.testCases[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.testCases[0].preconditions", is(response.getPreconditions())))
                .andExpect(jsonPath("$._embedded.testCases[0].inputData", is(response.getInputData())))
                .andExpect(jsonPath("$._embedded.testCases[0].steps", is(response.getSteps())))
                .andExpect(jsonPath("$._embedded.testCases[0].priority", is(response.getPriority().name())))
                .andExpect(jsonPath("$._embedded.testCases[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.testCases[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.testCases[0]._links.self.href", is("http://localhost/test-cases/1")))
                .andExpect(jsonPath("$._embedded.testCases[0]._links.update.href", is("http://localhost/test-cases/1")))
                .andExpect(jsonPath("$._embedded.testCases[0]._links.delete.href", is("http://localhost/test-cases/1")));
    }

    @Test
    void shouldReturnPageOfTestCaseResponsesWhenGettingAllTestCasesWhenPriorityNotNull() throws Exception {
        // given
        var response = createResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(testCaseService.getAllTestCases(pageable, null, null, response.getPriority()))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/test-cases"))
                );

        // then
        mockMvc.perform(get("/test-cases")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("priority", response.getPriority().name())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.testCases[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.testCases[0].projectId", is(response.getProjectId()), Long.class))
                .andExpect(jsonPath("$._embedded.testCases[0].authorId", is(response.getAuthorId()), Long.class))
                .andExpect(jsonPath("$._embedded.testCases[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.testCases[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.testCases[0].preconditions", is(response.getPreconditions())))
                .andExpect(jsonPath("$._embedded.testCases[0].inputData", is(response.getInputData())))
                .andExpect(jsonPath("$._embedded.testCases[0].steps", is(response.getSteps())))
                .andExpect(jsonPath("$._embedded.testCases[0].priority", is(response.getPriority().name())))
                .andExpect(jsonPath("$._embedded.testCases[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.testCases[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.testCases[0]._links.self.href", is("http://localhost/test-cases/1")))
                .andExpect(jsonPath("$._embedded.testCases[0]._links.update.href", is("http://localhost/test-cases/1")))
                .andExpect(jsonPath("$._embedded.testCases[0]._links.delete.href", is("http://localhost/test-cases/1")));
    }

    private void mockSecurityContext(Long userId) {
        User mockUser = new User(
                userId,
                "firstName",
                "lastName",
                "email",
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

    private TestCaseResponse createResponse() {
        var timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        var timeOfModification = Instant.now().plus(Duration.ofHours(20));

        var response = new TestCaseResponse(
                1L,
                1L,
                1L,
                "title",
                "description",
                "preconditions",
                "inputData",
                List.of("step1", "step2"),
                TestCasePriorityName.HIGH,
                timeOfCreation,
                timeOfModification
        );

        response.add(Link.of("http://localhost/test-cases/1", "self"));
        response.add(Link.of("http://localhost/test-cases/1", "update"));
        response.add(Link.of("http://localhost/test-cases/1", "delete"));

        return response;
    }
}
