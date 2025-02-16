package com.stepaniuk.testhorizon.bugreport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stepaniuk.testhorizon.bugreport.exceptions.NoSuchBugReportByIdException;
import com.stepaniuk.testhorizon.bugreport.exceptions.NoSuchBugReportSeverityByNameException;
import com.stepaniuk.testhorizon.bugreport.exceptions.NoSuchBugReportStatusByNameException;
import com.stepaniuk.testhorizon.types.bugreport.BugReportSeverityName;
import com.stepaniuk.testhorizon.types.bugreport.BugReportStatusName;
import com.stepaniuk.testhorizon.payload.bugreport.BugReportCreateRequest;
import com.stepaniuk.testhorizon.payload.bugreport.BugReportResponse;
import com.stepaniuk.testhorizon.payload.bugreport.BugReportUpdateRequest;
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
import org.springframework.security.core.context.SecurityContextHolder;
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

@ControllerLevelUnitTest(controllers = BugReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class BugReportControllerTest {

    @MockitoBean
    private BugReportService bugReportService;

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
    void shouldReturnBugResponseWhenCreatingBugReport() throws Exception {
        // given
        Long reporterId = 1L;

        BugReportCreateRequest bugReportCreateRequest = new BugReportCreateRequest(1L, "title", "description",
                "environment", BugReportSeverityName.CRITICAL);

        var response = getResponse();

        // when
        when(bugReportService.createBugReport(eq(bugReportCreateRequest), eq(reporterId), any())).thenReturn(response);

        // then
        mockMvc.perform(post("/bug-reports")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bugReportCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.projectId", is(response.getProjectId()), Long.class))
                .andExpect(jsonPath("$.reporterId", is(response.getReporterId()), Long.class))
                .andExpect(jsonPath("$.title", is(response.getTitle())))
                .andExpect(jsonPath("$.description", is(response.getDescription())))
                .andExpect(jsonPath("$.environment", is(response.getEnvironment())))
                .andExpect(jsonPath("$.severity", is(response.getSeverity().name())))
                .andExpect(jsonPath("$.status", is(response.getStatus().name())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/bug-reports/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/bug-reports/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/bug-reports/1")));

        SecurityContextHolder.clearContext();
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldThrowNoSuchBugReportSeverityByNameExceptionWhenCreatingBugReportWithNonExistingSeverity() throws Exception {
        // given
        Long reporterId = 1L;

        BugReportCreateRequest bugReportCreateRequest = new BugReportCreateRequest(1L, "title", "description",
                "environment", BugReportSeverityName.CRITICAL);

        // when
        when(bugReportService.createBugReport(eq(bugReportCreateRequest), eq(reporterId), any()))
                .thenThrow(new NoSuchBugReportSeverityByNameException(bugReportCreateRequest.getSeverity()));

        // then
        mockMvc.perform(post("/bug-reports")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bugReportCreateRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such bug report severity")))
                .andExpect(jsonPath("$.detail", is("No bug report severity with name " + bugReportCreateRequest.getSeverity().name())))
                .andExpect(jsonPath("$.instance", is("/bug-reports")));

        SecurityContextHolder.clearContext();
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldThrowNoSuchBugReportStatusByNameExceptionWhenCreatingBugReportWithNonExistingStatus() throws Exception {
        // given
        Long reporterId = 1L;

        BugReportCreateRequest bugReportCreateRequest = new BugReportCreateRequest(1L, "title", "description",
                "environment", BugReportSeverityName.CRITICAL);

        // when
        when(bugReportService.createBugReport(eq(bugReportCreateRequest), eq(reporterId), any()))
                .thenThrow(new NoSuchBugReportStatusByNameException(BugReportStatusName.OPENED));

        // then
        mockMvc.perform(post("/bug-reports")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bugReportCreateRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such bug report status")))
                .andExpect(jsonPath("$.detail", is("No bug report status with name " + BugReportStatusName.OPENED)))
                .andExpect(jsonPath("$.instance", is("/bug-reports")));

        SecurityContextHolder.clearContext();
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnBugResponseWhenGettingById() throws Exception {
        Long bugReportId = 1L;
        // given
        var response = getResponse();

        // when
        when(bugReportService.getBugReportById(bugReportId)).thenReturn(response);

        // then
        mockMvc.perform(get("/bug-reports/" + bugReportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.projectId", is(response.getProjectId()), Long.class))
                .andExpect(jsonPath("$.reporterId", is(response.getReporterId()), Long.class))
                .andExpect(jsonPath("$.title", is(response.getTitle())))
                .andExpect(jsonPath("$.description", is(response.getDescription())))
                .andExpect(jsonPath("$.environment", is(response.getEnvironment())))
                .andExpect(jsonPath("$.severity", is(response.getSeverity().name())))
                .andExpect(jsonPath("$.status", is(response.getStatus().name())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/bug-reports/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/bug-reports/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/bug-reports/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldThrowNoSuchBugReportByIdExceptionWhenGettingNonExistingBugReport() throws Exception {
        Long bugReportId = 1L;
        // when
        when(bugReportService.getBugReportById(bugReportId))
                .thenThrow(new NoSuchBugReportByIdException(bugReportId));

        // then
        mockMvc.perform(get("/bug-reports/" + bugReportId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such bug report")))
                .andExpect(jsonPath("$.detail", is("No bug report with id " + bugReportId)))
                .andExpect(jsonPath("$.instance", is("/bug-reports")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnBugReportResponseWhenUpdatingBugReport() throws Exception {
        // given
        Long bugReportId = 1L;

        var bugReportResponse = getResponse();

        var bugReportUpdateRequest = new BugReportUpdateRequest("new title", null, null,
                null, null);

        when(bugReportService.updateBugReport(eq(bugReportId), eq(bugReportUpdateRequest), any(), any())).thenReturn(bugReportResponse);

        // then
        mockMvc.perform(patch("/bug-reports/" + bugReportId).contentType("application/json")
                        .content(objectMapper.writeValueAsString(bugReportUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bugReportResponse.getId()), Long.class))
                .andExpect(jsonPath("$.projectId", is(bugReportResponse.getProjectId()), Long.class))
                .andExpect(jsonPath("$.reporterId", is(bugReportResponse.getReporterId()), Long.class))
                .andExpect(jsonPath("$.title", is(bugReportResponse.getTitle())))
                .andExpect(jsonPath("$.description", is(bugReportResponse.getDescription())))
                .andExpect(jsonPath("$.environment", is(bugReportResponse.getEnvironment())))
                .andExpect(jsonPath("$.severity", is(bugReportResponse.getSeverity().name())))
                .andExpect(jsonPath("$.status", is(bugReportResponse.getStatus().name())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(bugReportResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(bugReportResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/bug-reports/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/bug-reports/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/bug-reports/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseBugNoSuchBugReportByIdExceptionResponseWhenUpdatingBugReport() throws Exception {
        // given
        Long bugReportId = 1L;
        BugReportUpdateRequest bugReportUpdateRequest = new BugReportUpdateRequest("new title", null, null, null,
                null);

        // when
        when(bugReportService.updateBugReport(eq(bugReportId), eq(bugReportUpdateRequest), any(), any()))
                .thenThrow(new NoSuchBugReportByIdException(bugReportId));

        // then
        mockMvc.perform(patch("/bug-reports/" + bugReportId).contentType("application/json")
                        .content(objectMapper.writeValueAsString(bugReportUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such bug report")))
                .andExpect(jsonPath("$.detail", is("No bug report with id 1")))
                .andExpect(jsonPath("$.instance", is("/bug-reports")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseBugNoSuchBugReportSeverityByNameExceptionResponseWhenUpdatingBugReport() throws Exception {
        // given
        Long bugReportId = 1L;
        BugReportUpdateRequest bugReportUpdateRequest = new BugReportUpdateRequest("new title", null, null, BugReportSeverityName.LOW, null);

        // when
        when(bugReportService.updateBugReport(eq(bugReportId), eq(bugReportUpdateRequest), any(), any()))
                .thenThrow(new NoSuchBugReportSeverityByNameException(bugReportUpdateRequest.getSeverity()));

        // then
        mockMvc.perform(patch("/bug-reports/" + bugReportId).contentType("application/json")
                        .content(objectMapper.writeValueAsString(bugReportUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such bug report severity")))
                .andExpect(jsonPath("$.detail", is("No bug report severity with name " + bugReportUpdateRequest.getSeverity())))
                .andExpect(jsonPath("$.instance", is("/bug-reports")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseBugNoSuchBugReportStatusByNameExceptionResponseWhenUpdatingBugReport() throws Exception {
        // given
        Long bugReportId = 1L;
        BugReportUpdateRequest bugReportUpdateRequest = new BugReportUpdateRequest("new title", null,
                null, null, BugReportStatusName.CLOSED);

        // when
        when(bugReportService.updateBugReport(eq(bugReportId), eq(bugReportUpdateRequest), any(), any()))
                .thenThrow(new NoSuchBugReportStatusByNameException(bugReportUpdateRequest.getStatus()));

        // then
        mockMvc.perform(patch("/bug-reports/" + bugReportId).contentType("application/json")
                        .content(objectMapper.writeValueAsString(bugReportUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such bug report status")))
                .andExpect(jsonPath("$.detail", is("No bug report status with name " + bugReportUpdateRequest.getStatus())))
                .andExpect(jsonPath("$.instance", is("/bug-reports")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseAccessToManageEntityDeniedExceptionResponseWhenUpdatingBugReport() throws Exception {
        // given
        Long bugReportId = 1L;
        BugReportUpdateRequest bugReportUpdateRequest = new BugReportUpdateRequest("new title", null, null, null,
                null);

        // when
        when(bugReportService.updateBugReport(eq(bugReportId), eq(bugReportUpdateRequest), any(), any()))
                .thenThrow(new AccessToManageEntityDeniedException("BugReport", "/bug-reports"));

        // then
        mockMvc.perform(patch("/bug-reports/" + bugReportId).contentType("application/json")
                        .content(objectMapper.writeValueAsString(bugReportUpdateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.title", is("Access denied")))
                .andExpect(jsonPath("$.detail", is("Access to manage BugReport denied")))
                .andExpect(jsonPath("$.instance", is("/bug-reports")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnNoContentWhenDeletingBugReport() throws Exception {
        long bugReportId = 1L;
        // when && then
        mockMvc.perform(delete("/bug-reports/" + bugReportId)
                        .contentType("application/json")
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseBugNoSuchBugReportByIdExceptionResponseWhenDeletingBugReport() throws Exception {
        // when
        Long bugReportId = 1L;

        doThrow(new NoSuchBugReportByIdException(bugReportId))
                .when(bugReportService)
                .deleteBugReportById(eq(bugReportId), any(), any());

        // then
        mockMvc.perform(delete("/bug-reports/" + bugReportId)
                        .contentType("application/json")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such bug report")))
                .andExpect(jsonPath("$.detail", is("No bug report with id 1")))
                .andExpect(jsonPath("$.instance", is("/bug-reports")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseAccessToManageEntityDeniedExceptionResponseWhenDeletingBugReport() throws Exception {
        // when
        Long bugReportId = 1L;

        doThrow(new AccessToManageEntityDeniedException("BugReport", "/bug-reports"))
                .when(bugReportService)
                .deleteBugReportById(eq(bugReportId), any(), any());

        // then
        mockMvc.perform(delete("/bug-reports/" + bugReportId)
                        .contentType("application/json")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.title", is("Access denied")))
                .andExpect(jsonPath("$.detail", is("Access to manage BugReport denied")))
                .andExpect(jsonPath("$.instance", is("/bug-reports")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfBugReportResponsesWhenGettingAllBugReports() throws Exception {
        // given
        var response = getResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(bugReportService.getAllBugReports(pageable, null, null, null, null, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/bug-reports"))
                );

        // then
        mockMvc.perform(get("/bug-reports")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.bugReports[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.bugReports[0].projectId", is(response.getProjectId()), Long.class))
                .andExpect(jsonPath("$._embedded.bugReports[0].reporterId", is(response.getReporterId()), Long.class))
                .andExpect(jsonPath("$._embedded.bugReports[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.bugReports[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.bugReports[0].environment", is(response.getEnvironment())))
                .andExpect(jsonPath("$._embedded.bugReports[0].severity", is(response.getSeverity().name())))
                .andExpect(jsonPath("$._embedded.bugReports[0].status", is(response.getStatus().name())))
                .andExpect(jsonPath("$._embedded.bugReports[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.bugReports[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.bugReports[0]._links.self.href", is("http://localhost/bug-reports/1")))
                .andExpect(jsonPath("$._embedded.bugReports[0]._links.update.href", is("http://localhost/bug-reports/1")))
                .andExpect(jsonPath("$._embedded.bugReports[0]._links.delete.href", is("http://localhost/bug-reports/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfBugReportResponsesWhenGettingAllBugReportsWhenProjectIdIsNotNull() throws Exception {
        // given
        var response = getResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(bugReportService.getAllBugReports(pageable, response.getId(), null, null, null, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/bug-reports"))
                );

        // then
        mockMvc.perform(get("/bug-reports")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("projectId", response.getId().toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.bugReports[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.bugReports[0].projectId", is(response.getProjectId()), Long.class))
                .andExpect(jsonPath("$._embedded.bugReports[0].reporterId", is(response.getReporterId()), Long.class))
                .andExpect(jsonPath("$._embedded.bugReports[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.bugReports[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.bugReports[0].environment", is(response.getEnvironment())))
                .andExpect(jsonPath("$._embedded.bugReports[0].severity", is(response.getSeverity().name())))
                .andExpect(jsonPath("$._embedded.bugReports[0].status", is(response.getStatus().name())))
                .andExpect(jsonPath("$._embedded.bugReports[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.bugReports[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.bugReports[0]._links.self.href", is("http://localhost/bug-reports/1")))
                .andExpect(jsonPath("$._embedded.bugReports[0]._links.update.href", is("http://localhost/bug-reports/1")))
                .andExpect(jsonPath("$._embedded.bugReports[0]._links.delete.href", is("http://localhost/bug-reports/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfBugReportResponsesWhenGettingAllBugReportsWhenTitleIsNotNull() throws Exception {
        // given
        var response = getResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(bugReportService.getAllBugReports(pageable, null, response.getTitle(), null, null, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/bug-reports"))
                );

        // then
        mockMvc.perform(get("/bug-reports")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("title", response.getTitle())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.bugReports[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.bugReports[0].projectId", is(response.getProjectId()), Long.class))
                .andExpect(jsonPath("$._embedded.bugReports[0].reporterId", is(response.getReporterId()), Long.class))
                .andExpect(jsonPath("$._embedded.bugReports[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.bugReports[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.bugReports[0].environment", is(response.getEnvironment())))
                .andExpect(jsonPath("$._embedded.bugReports[0].severity", is(response.getSeverity().name())))
                .andExpect(jsonPath("$._embedded.bugReports[0].status", is(response.getStatus().name())))
                .andExpect(jsonPath("$._embedded.bugReports[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.bugReports[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.bugReports[0]._links.self.href", is("http://localhost/bug-reports/1")))
                .andExpect(jsonPath("$._embedded.bugReports[0]._links.update.href", is("http://localhost/bug-reports/1")))
                .andExpect(jsonPath("$._embedded.bugReports[0]._links.delete.href", is("http://localhost/bug-reports/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfBugReportResponsesWhenGettingAllBugReportsWhenReporterIdIsNotNull() throws Exception {
        // given
        var response = getResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(bugReportService.getAllBugReports(pageable, null, null, response.getReporterId(), null, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/bug-reports"))
                );

        // then
        mockMvc.perform(get("/bug-reports")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("reporterId", response.getReporterId().toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.bugReports[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.bugReports[0].projectId", is(response.getProjectId()), Long.class))
                .andExpect(jsonPath("$._embedded.bugReports[0].reporterId", is(response.getReporterId()), Long.class))
                .andExpect(jsonPath("$._embedded.bugReports[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.bugReports[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.bugReports[0].environment", is(response.getEnvironment())))
                .andExpect(jsonPath("$._embedded.bugReports[0].severity", is(response.getSeverity().name())))
                .andExpect(jsonPath("$._embedded.bugReports[0].status", is(response.getStatus().name())))
                .andExpect(jsonPath("$._embedded.bugReports[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.bugReports[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.bugReports[0]._links.self.href", is("http://localhost/bug-reports/1")))
                .andExpect(jsonPath("$._embedded.bugReports[0]._links.update.href", is("http://localhost/bug-reports/1")))
                .andExpect(jsonPath("$._embedded.bugReports[0]._links.delete.href", is("http://localhost/bug-reports/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfBugReportResponsesWhenGettingAllBugReportsWhenSeverityNameIsNotNull() throws Exception {
        // given
        var response = getResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(bugReportService.getAllBugReports(pageable, null, null, null, response.getSeverity(), null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/bug-reports"))
                );

        // then
        mockMvc.perform(get("/bug-reports")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("severityName", response.getSeverity().name())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.bugReports[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.bugReports[0].projectId", is(response.getProjectId()), Long.class))
                .andExpect(jsonPath("$._embedded.bugReports[0].reporterId", is(response.getReporterId()), Long.class))
                .andExpect(jsonPath("$._embedded.bugReports[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.bugReports[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.bugReports[0].environment", is(response.getEnvironment())))
                .andExpect(jsonPath("$._embedded.bugReports[0].severity", is(response.getSeverity().name())))
                .andExpect(jsonPath("$._embedded.bugReports[0].status", is(response.getStatus().name())))
                .andExpect(jsonPath("$._embedded.bugReports[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.bugReports[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.bugReports[0]._links.self.href", is("http://localhost/bug-reports/1")))
                .andExpect(jsonPath("$._embedded.bugReports[0]._links.update.href", is("http://localhost/bug-reports/1")))
                .andExpect(jsonPath("$._embedded.bugReports[0]._links.delete.href", is("http://localhost/bug-reports/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfBugReportResponsesWhenGettingAllBugReportsWhenStatusIsNotNull() throws Exception {
        // given
        var response = getResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(bugReportService.getAllBugReports(pageable, null, null, null, null, response.getStatus()))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/bug-reports"))
                );

        // then
        mockMvc.perform(get("/bug-reports")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("status", response.getStatus().name())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.bugReports[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.bugReports[0].projectId", is(response.getProjectId()), Long.class))
                .andExpect(jsonPath("$._embedded.bugReports[0].reporterId", is(response.getReporterId()), Long.class))
                .andExpect(jsonPath("$._embedded.bugReports[0].title", is(response.getTitle())))
                .andExpect(jsonPath("$._embedded.bugReports[0].description", is(response.getDescription())))
                .andExpect(jsonPath("$._embedded.bugReports[0].environment", is(response.getEnvironment())))
                .andExpect(jsonPath("$._embedded.bugReports[0].severity", is(response.getSeverity().name())))
                .andExpect(jsonPath("$._embedded.bugReports[0].status", is(response.getStatus().name())))
                .andExpect(jsonPath("$._embedded.bugReports[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.bugReports[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.bugReports[0]._links.self.href", is("http://localhost/bug-reports/1")))
                .andExpect(jsonPath("$._embedded.bugReports[0]._links.update.href", is("http://localhost/bug-reports/1")))
                .andExpect(jsonPath("$._embedded.bugReports[0]._links.delete.href", is("http://localhost/bug-reports/1")));
    }

    private BugReportResponse getResponse() {
        var timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        var timeOfModification = Instant.now().plus(Duration.ofHours(20));

        var response = new BugReportResponse(1L, 1L, 1L, "title", "description", "environment",
                BugReportSeverityName.CRITICAL, BugReportStatusName.OPENED,
                timeOfCreation, timeOfModification);

        response.add(Link.of("http://localhost/bug-reports/1", "self"));
        response.add(Link.of("http://localhost/bug-reports/1", "update"));
        response.add(Link.of("http://localhost/bug-reports/1", "delete"));

        return response;
    }
}
