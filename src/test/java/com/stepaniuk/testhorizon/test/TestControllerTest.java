package com.stepaniuk.testhorizon.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stepaniuk.testhorizon.payload.test.TestCreateRequest;
import com.stepaniuk.testhorizon.payload.test.TestResponse;
import com.stepaniuk.testhorizon.payload.test.TestUpdateRequest;
import com.stepaniuk.testhorizon.security.config.JwtAuthFilter;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.test.exceptions.NoSuchTestByIdException;
import com.stepaniuk.testhorizon.test.exceptions.NoSuchTestTypeByNameException;
import com.stepaniuk.testhorizon.test.type.TestTypeName;
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

@ControllerLevelUnitTest(controllers = TestController.class)
@AutoConfigureMockMvc(addFilters = false)
class TestControllerTest {

    @MockitoBean
    private TestService testService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;
    @Autowired
    private PageMapper pageMapper;

    @Test
    void shouldReturnTestResponseWhenCreatingTest() throws Exception {
        // given
        Long userId = 1L;

        mockSecurityContext(userId);

        TestCreateRequest testCreateRequest = new TestCreateRequest(
                1L,
                1L,
                "title",
                "description",
                "instructions",
                "https://github.com",
                TestTypeName.ACCEPTANCE
        );

        TestResponse testResponse = createTestResponse();

        when(testService.createTest(eq(testCreateRequest), eq(userId), any())).thenReturn(testResponse);

        mockMvc.perform(post("/tests")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(testResponse.getId()), Long.class))
                .andExpect(jsonPath("$.projectId", is(testResponse.getProjectId()), Long.class))
                .andExpect(jsonPath("$.testCaseId", is(testResponse.getTestCaseId()), Long.class))
                .andExpect(jsonPath("$.authorId", is(testResponse.getAuthorId()), Long.class))
                .andExpect(jsonPath("$.title", is(testResponse.getTitle())))
                .andExpect(jsonPath("$.description", is(testResponse.getDescription())))
                .andExpect(jsonPath("$.instructions", is(testResponse.getInstructions())))
                .andExpect(jsonPath("$.githubUrl", is(testResponse.getGithubUrl())))
                .andExpect(jsonPath("$.type", is(testResponse.getType().name())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(testResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(testResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/tests/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/tests/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/tests/1")));

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnErrorResponseWhenCreatingTest() throws Exception {
        // given
        Long userId = 1L;

        mockSecurityContext(userId);

        TestCreateRequest testCreateRequest = new TestCreateRequest(
                1L,
                1L,
                "title",
                "description",
                "instructions",
                "https://github.com",
                TestTypeName.ACCEPTANCE
        );

        when(testService.createTest(eq(testCreateRequest), eq(userId), any())).thenThrow(new NoSuchTestTypeByNameException(testCreateRequest.getType()));

        // then
        mockMvc.perform(post("/tests")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testCreateRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such test type")))
                .andExpect(jsonPath("$.detail", is("No test type with name " + testCreateRequest.getType().name())))
                .andExpect(jsonPath("$.instance", is("/tests")));

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnTestResponseWhenGettingTestById() throws Exception {
        // given
        Long testId = 1L;

        TestResponse testResponse = createTestResponse();

        when(testService.getTestById(testId)).thenReturn(testResponse);

        // then
        mockMvc.perform(get("/tests/" + testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testResponse.getId()), Long.class))
                .andExpect(jsonPath("$.projectId", is(testResponse.getProjectId()), Long.class))
                .andExpect(jsonPath("$.testCaseId", is(testResponse.getTestCaseId()), Long.class))
                .andExpect(jsonPath("$.authorId", is(testResponse.getAuthorId()), Long.class))
                .andExpect(jsonPath("$.title", is(testResponse.getTitle())))
                .andExpect(jsonPath("$.description", is(testResponse.getDescription())))
                .andExpect(jsonPath("$.instructions", is(testResponse.getInstructions())))
                .andExpect(jsonPath("$.githubUrl", is(testResponse.getGithubUrl())))
                .andExpect(jsonPath("$.type", is(testResponse.getType().name())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(testResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(testResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/tests/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/tests/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/tests/1")));
    }

    @Test
    void shouldReturnErrorResponseWhenGettingTestById() throws Exception {
        // given
        Long testId = 1L;

        when(testService.getTestById(testId)).thenThrow(new NoSuchTestByIdException(testId));

        // then
        mockMvc.perform(get("/tests/" + testId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such test")))
                .andExpect(jsonPath("$.detail", is("No test with id " + testId)))
                .andExpect(jsonPath("$.instance", is("/tests")));
    }

    @Test
    void shouldReturnTestResponseWhenUpdatingTest() throws Exception {
        // given
        var testId = 1L;

        var testUpdateRequest = new TestUpdateRequest(
                1L,
                "title",
                "description",
                "instructions",
                "https://github.com",
                TestTypeName.ACCEPTANCE
        );

        var testResponse = createTestResponse();

        when(testService.updateTest(eq(testId), eq(testUpdateRequest), any())).thenReturn(testResponse);

        // then
        mockMvc.perform(patch("/tests/" + testId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testUpdateRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testResponse.getId()), Long.class))
                .andExpect(jsonPath("$.projectId", is(testResponse.getProjectId()), Long.class))
                .andExpect(jsonPath("$.testCaseId", is(testResponse.getTestCaseId()), Long.class))
                .andExpect(jsonPath("$.authorId", is(testResponse.getAuthorId()), Long.class))
                .andExpect(jsonPath("$.title", is(testResponse.getTitle())))
                .andExpect(jsonPath("$.description", is(testResponse.getDescription())))
                .andExpect(jsonPath("$.instructions", is(testResponse.getInstructions())))
                .andExpect(jsonPath("$.githubUrl", is(testResponse.getGithubUrl())))
                .andExpect(jsonPath("$.type", is(testResponse.getType().name())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(testResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(testResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/tests/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/tests/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/tests/1")));

    }

    @Test
    void shouldReturnErrorResponseNoSuchTestByIdExceptionWhenUpdatingTest() throws Exception {
        // given
        var testId = 1L;

        var testUpdateRequest = new TestUpdateRequest(
                1L,
                "title",
                "description",
                "instructions",
                "https://github.com",
                TestTypeName.ACCEPTANCE
        );

        when(testService.updateTest(eq(testId), eq(testUpdateRequest), any())).thenThrow(new NoSuchTestByIdException(testId));

        // then
        mockMvc.perform(patch("/tests/" + testId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testUpdateRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such test")))
                .andExpect(jsonPath("$.detail", is("No test with id " + testId)))
                .andExpect(jsonPath("$.instance", is("/tests")));
    }

    @Test
    void shouldReturnErrorResponseNoSuchTestTypeByNameExceptionWhenUpdatingTest() throws Exception {
        // given
        var testId = 1L;

        var testUpdateRequest = new TestUpdateRequest(
                1L,
                "title",
                "description",
                "instructions",
                "https://github.com",
                TestTypeName.ACCEPTANCE
        );

        when(testService.updateTest(eq(testId), eq(testUpdateRequest), any())).thenThrow(new NoSuchTestTypeByNameException(testUpdateRequest.getType()));

        // then
        mockMvc.perform(patch("/tests/" + testId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(testUpdateRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such test type")))
                .andExpect(jsonPath("$.detail", is("No test type with name " + testUpdateRequest.getType())))
                .andExpect(jsonPath("$.instance", is("/tests")));
    }

    @Test
    void shouldReturnNoContentWhenDeletingTest() throws Exception {
        // given
        var testId = 1L;

        // when && then
        mockMvc.perform(delete("/tests/" + testId)
                        .contentType("application/json")
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnErrorResponseWhenDeletingTest() throws Exception {
        // given
        var testId = 1L;

        doThrow(new NoSuchTestByIdException(testId))
                .when(testService)
                .deleteTestById(eq(testId), any());


        // then
        mockMvc.perform(delete("/tests/" + testId)
                        .contentType("application/json")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such test")))
                .andExpect(jsonPath("$.detail", is("No test with id " + testId)))
                .andExpect(jsonPath("$.instance", is("/tests")));
    }

    @Test
    void shouldReturnPageOfTestResponsesWhenGettingAllTests() throws Exception {
        // given
        var response = createTestResponse();
        var pageable = PageRequest.of(0, 2);

        when(testService.getAllTests(pageable, null, null, null, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/tests"))
                );

        // then
        mockMvc.perform(get("/tests")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.tests[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.tests[0].projectId", is(response.getProjectId()), Long.class))
                .andExpect(jsonPath("$._embedded.tests[0].testCaseId", is(response.getTestCaseId()), Long.class))
                .andExpect(jsonPath("$._embedded.tests[0].authorId", is(response.getAuthorId()), Long.class))
                .andExpect(jsonPath("$._embedded.tests[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.tests[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.tests[0].instructions", is(response.getInstructions())))
                .andExpect(jsonPath("$._embedded.tests[0].githubUrl", is(response.getGithubUrl())))
                .andExpect(jsonPath("$._embedded.tests[0].type", is(response.getType().name())))
                .andExpect(jsonPath("$._embedded.tests[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.tests[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.tests[0]._links.self.href", is("http://localhost/tests/1")))
                .andExpect(jsonPath("$._embedded.tests[0]._links.update.href", is("http://localhost/tests/1")))
                .andExpect(jsonPath("$._embedded.tests[0]._links.delete.href", is("http://localhost/tests/1")));
    }

    @Test
    void shouldReturnPageOfTestResponsesWhenGettingAllTestsWhenProjectIdNotNull() throws Exception {
        // given
        var response = createTestResponse();
        var pageable = PageRequest.of(0, 2);
        var projectId = 1L;

        when(testService.getAllTests(pageable, projectId, null, null, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/tests"))
                );

        // then
        mockMvc.perform(get("/tests")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("projectId", response.getId().toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.tests[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.tests[0].projectId", is(response.getProjectId()), Long.class))
                .andExpect(jsonPath("$._embedded.tests[0].testCaseId", is(response.getTestCaseId()), Long.class))
                .andExpect(jsonPath("$._embedded.tests[0].authorId", is(response.getAuthorId()), Long.class))
                .andExpect(jsonPath("$._embedded.tests[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.tests[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.tests[0].instructions", is(response.getInstructions())))
                .andExpect(jsonPath("$._embedded.tests[0].githubUrl", is(response.getGithubUrl())))
                .andExpect(jsonPath("$._embedded.tests[0].type", is(response.getType().name())))
                .andExpect(jsonPath("$._embedded.tests[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.tests[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.tests[0]._links.self.href", is("http://localhost/tests/1")))
                .andExpect(jsonPath("$._embedded.tests[0]._links.update.href", is("http://localhost/tests/1")))
                .andExpect(jsonPath("$._embedded.tests[0]._links.delete.href", is("http://localhost/tests/1")));
    }

    @Test
    void shouldReturnPageOfTestResponsesWhenGettingAllTestsWhenAuthorIdNotNull() throws Exception {
        // given
        var response = createTestResponse();
        var pageable = PageRequest.of(0, 2);
        var authorId = 1L;

        when(testService.getAllTests(pageable, null, authorId, null, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/tests"))
                );

        // then
        mockMvc.perform(get("/tests")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("authorId", response.getId().toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.tests[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.tests[0].projectId", is(response.getProjectId()), Long.class))
                .andExpect(jsonPath("$._embedded.tests[0].testCaseId", is(response.getTestCaseId()), Long.class))
                .andExpect(jsonPath("$._embedded.tests[0].authorId", is(response.getAuthorId()), Long.class))
                .andExpect(jsonPath("$._embedded.tests[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.tests[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.tests[0].instructions", is(response.getInstructions())))
                .andExpect(jsonPath("$._embedded.tests[0].githubUrl", is(response.getGithubUrl())))
                .andExpect(jsonPath("$._embedded.tests[0].type", is(response.getType().name())))
                .andExpect(jsonPath("$._embedded.tests[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.tests[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.tests[0]._links.self.href", is("http://localhost/tests/1")))
                .andExpect(jsonPath("$._embedded.tests[0]._links.update.href", is("http://localhost/tests/1")))
                .andExpect(jsonPath("$._embedded.tests[0]._links.delete.href", is("http://localhost/tests/1")));
    }

    @Test
    void shouldReturnPageOfTestResponsesWhenGettingAllTestsWhenTestCaseIdNotNull() throws Exception {
        // given
        var response = createTestResponse();
        var pageable = PageRequest.of(0, 2);
        var testCaseId = 1L;

        when(testService.getAllTests(pageable, null, null, testCaseId, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/tests"))
                );

        // then
        mockMvc.perform(get("/tests")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("testCaseId", response.getId().toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.tests[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.tests[0].projectId", is(response.getProjectId()), Long.class))
                .andExpect(jsonPath("$._embedded.tests[0].testCaseId", is(response.getTestCaseId()), Long.class))
                .andExpect(jsonPath("$._embedded.tests[0].authorId", is(response.getAuthorId()), Long.class))
                .andExpect(jsonPath("$._embedded.tests[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.tests[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.tests[0].instructions", is(response.getInstructions())))
                .andExpect(jsonPath("$._embedded.tests[0].githubUrl", is(response.getGithubUrl())))
                .andExpect(jsonPath("$._embedded.tests[0].type", is(response.getType().name())))
                .andExpect(jsonPath("$._embedded.tests[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.tests[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.tests[0]._links.self.href", is("http://localhost/tests/1")))
                .andExpect(jsonPath("$._embedded.tests[0]._links.update.href", is("http://localhost/tests/1")))
                .andExpect(jsonPath("$._embedded.tests[0]._links.delete.href", is("http://localhost/tests/1")));
    }

    @Test
    void shouldReturnPageOfTestResponsesWhenGettingAllTestsWhenTypeNotNull() throws Exception {
        // given
        var response = createTestResponse();
        var pageable = PageRequest.of(0, 2);
        var type = TestTypeName.ACCEPTANCE;

        when(testService.getAllTests(pageable, null, null, null, type))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/tests"))
                );

        // then
        mockMvc.perform(get("/tests")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("type", type.name())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.tests[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.tests[0].projectId", is(response.getProjectId()), Long.class))
                .andExpect(jsonPath("$._embedded.tests[0].testCaseId", is(response.getTestCaseId()), Long.class))
                .andExpect(jsonPath("$._embedded.tests[0].authorId", is(response.getAuthorId()), Long.class))
                .andExpect(jsonPath("$._embedded.tests[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.tests[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.tests[0].instructions", is(response.getInstructions())))
                .andExpect(jsonPath("$._embedded.tests[0].githubUrl", is(response.getGithubUrl())))
                .andExpect(jsonPath("$._embedded.tests[0].type", is(response.getType().name())))
                .andExpect(jsonPath("$._embedded.tests[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.tests[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.tests[0]._links.self.href", is("http://localhost/tests/1")))
                .andExpect(jsonPath("$._embedded.tests[0]._links.update.href", is("http://localhost/tests/1")))
                .andExpect(jsonPath("$._embedded.tests[0]._links.delete.href", is("http://localhost/tests/1")));
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

    private TestResponse createTestResponse() {
        var timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        var timeOfModification = Instant.now().plus(Duration.ofHours(20));

        TestResponse testResponse = new TestResponse(
                1L,
                1L,
                1L,
                1L,
                "title",
                "description",
                "instructions",
                "https://github.com",
                TestTypeName.ACCEPTANCE,
                timeOfCreation,
                timeOfModification
        );

        testResponse.add(Link.of("http://localhost/tests/1", "self"));
        testResponse.add(Link.of("http://localhost/tests/1", "update"));
        testResponse.add(Link.of("http://localhost/tests/1", "delete"));

        return testResponse;
    }
}
