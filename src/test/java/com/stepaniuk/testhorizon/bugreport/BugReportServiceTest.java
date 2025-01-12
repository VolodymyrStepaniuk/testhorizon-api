package com.stepaniuk.testhorizon.bugreport;

import com.stepaniuk.testhorizon.bugreport.exceptions.NoSuchBugReportByIdException;
import com.stepaniuk.testhorizon.bugreport.exceptions.NoSuchBugReportSeverityByNameException;
import com.stepaniuk.testhorizon.bugreport.exceptions.NoSuchBugReportStatusByNameException;
import com.stepaniuk.testhorizon.bugreport.severity.BugReportSeverity;
import com.stepaniuk.testhorizon.bugreport.severity.BugReportSeverityName;
import com.stepaniuk.testhorizon.bugreport.severity.BugReportSeverityRepository;
import com.stepaniuk.testhorizon.bugreport.status.BugReportStatus;
import com.stepaniuk.testhorizon.bugreport.status.BugReportStatusName;
import com.stepaniuk.testhorizon.bugreport.status.BugReportStatusRepository;
import com.stepaniuk.testhorizon.payload.bugreport.BugReportCreateRequest;
import com.stepaniuk.testhorizon.payload.bugreport.BugReportUpdateRequest;
import com.stepaniuk.testhorizon.shared.PageMapperImpl;
import com.stepaniuk.testhorizon.testspecific.ServiceLevelUnitTest;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ServiceLevelUnitTest
@ContextConfiguration(classes = {BugReportService.class, BugReportMapperImpl.class, PageMapperImpl.class})
class BugReportServiceTest {

    @Autowired
    private BugReportService bugReportService;

    @MockitoBean
    private BugReportRepository bugReportRepository;

    @MockitoBean
    private BugReportSeverityRepository bugReportSeverityRepository;

    @MockitoBean
    private BugReportStatusRepository bugReportStatusRepository;

    @Test
    void shouldReturnBugReportResponseWhenCreatingBugReport() {
        // given
        BugReportSeverity bugReportSeverity = new BugReportSeverity(1L, BugReportSeverityName.HIGH);
        BugReportCreateRequest bugReportCreateRequest = new BugReportCreateRequest(
                1L, "title", "description", "environment", List.of("https://image.com", "https://image2.com"),
                List.of("https://video.com"), bugReportSeverity.getName()
        );

        when(bugReportRepository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
        when(bugReportStatusRepository.findByName(BugReportStatusName.OPENED)).thenReturn(Optional.of(new BugReportStatus(1L, BugReportStatusName.OPENED)));
        when(bugReportSeverityRepository.findByName(bugReportSeverity.getName())).thenReturn(Optional.of(bugReportSeverity));

        // when
        var bugReportResponse = bugReportService.createBugReport(bugReportCreateRequest, 1L);

        // then
        assertNotNull(bugReportResponse);
        assertEquals(1L, bugReportResponse.getReporterId());
        assertEquals(bugReportCreateRequest.getProjectId(), bugReportResponse.getProjectId());
        assertEquals(bugReportCreateRequest.getTitle(), bugReportResponse.getTitle());
        assertEquals(bugReportCreateRequest.getDescription(), bugReportResponse.getDescription());
        assertEquals(bugReportCreateRequest.getEnvironment(), bugReportResponse.getEnvironment());
        assertEquals(bugReportCreateRequest.getImageUrls(), bugReportResponse.getImageUrls());
        assertEquals(bugReportCreateRequest.getVideoUrls(), bugReportResponse.getVideoUrls());
        assertEquals(bugReportSeverity.getName(), bugReportResponse.getSeverity());
        assertEquals(BugReportStatusName.OPENED, bugReportResponse.getStatus());
        assertTrue(bugReportResponse.hasLinks());

        verify(bugReportRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowNoSuchBugReportSeverityByNameExceptionWhenCreatingBugReport() {
        // given
        BugReportCreateRequest bugReportCreateRequest = new BugReportCreateRequest(
                1L, "title", "description", "environment", List.of("https://image.com", "https://image2.com"),
                List.of("https://video.com"), BugReportSeverityName.HIGH
        );

        when(bugReportSeverityRepository.findByName(bugReportCreateRequest.getSeverity())).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchBugReportSeverityByNameException.class, () -> bugReportService.createBugReport(bugReportCreateRequest, 1L));
    }

    @Test
    void shouldThrowNoSuchBugReportStatusByNameExceptionWhenCreatingBugReport() {
        // given
        BugReportCreateRequest bugReportCreateRequest = new BugReportCreateRequest(
                1L, "title", "description", "environment", List.of("https://image.com", "https://image2.com"),
                List.of("https://video.com"), BugReportSeverityName.HIGH
        );

        when(bugReportSeverityRepository.findByName(bugReportCreateRequest.getSeverity())).thenReturn(Optional.of(new BugReportSeverity(1L, BugReportSeverityName.HIGH)));
        when(bugReportStatusRepository.findByName(BugReportStatusName.OPENED)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchBugReportStatusByNameException.class, () -> bugReportService.createBugReport(bugReportCreateRequest, 1L));
    }

    @Test
    void shouldReturnBugReportResponseWhenGettingBugReportById() {
        // given
        BugReport bugReport = getNewBugReportWithAllFields();

        when(bugReportRepository.findById(1L)).thenReturn(Optional.of(bugReport));

        // when
        var bugReportResponse = bugReportService.getBugReportById(1L);

        // then
        assertNotNull(bugReportResponse);
        assertEquals(bugReport.getId(), bugReportResponse.getId());
        assertEquals(bugReport.getReporterId(), bugReportResponse.getReporterId());
        assertEquals(bugReport.getProjectId(), bugReportResponse.getProjectId());
        assertEquals(bugReport.getTitle(), bugReportResponse.getTitle());
        assertEquals(bugReport.getDescription(), bugReportResponse.getDescription());
        assertEquals(bugReport.getEnvironment(), bugReportResponse.getEnvironment());
        assertEquals(bugReport.getImageUrls(), bugReportResponse.getImageUrls());
        assertEquals(bugReport.getVideoUrls(), bugReportResponse.getVideoUrls());
        assertEquals(bugReport.getSeverity().getName(), bugReportResponse.getSeverity());
        assertEquals(bugReport.getStatus().getName(), bugReportResponse.getStatus());
        assertEquals(bugReport.getCreatedAt(), bugReportResponse.getCreatedAt());
        assertEquals(bugReport.getUpdatedAt(), bugReportResponse.getUpdatedAt());
        assertTrue(bugReportResponse.hasLinks());
    }

    @Test
    void shouldThrowNoSuchBugReportByIdExceptionWhenGettingBugReportById() {
        // given
        when(bugReportRepository.findById(1L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchBugReportByIdException.class, () -> bugReportService.getBugReportById(1L));
    }

    @Test
    void shouldUpdateAndReturnBugReportResponseWhenChangingBugReportTitle() {
        // given
        BugReport bugReport = getNewBugReportWithAllFields();
        BugReportUpdateRequest bugReportUpdateRequest = new BugReportUpdateRequest("new title", null, null, null, null, null, null);

        when(bugReportRepository.findById(1L)).thenReturn(Optional.of(bugReport));
        when(bugReportRepository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());

        // when
        var bugReportResponse = bugReportService.updateBugReport(1L, bugReportUpdateRequest);

        // then
        assertNotNull(bugReportResponse);
        assertEquals(bugReport.getId(), bugReportResponse.getId());
        assertEquals(bugReport.getReporterId(), bugReportResponse.getReporterId());
        assertEquals(bugReport.getProjectId(), bugReportResponse.getProjectId());
        assertEquals(bugReportUpdateRequest.getTitle(), bugReportResponse.getTitle());
        assertEquals(bugReport.getDescription(), bugReportResponse.getDescription());
        assertEquals(bugReport.getEnvironment(), bugReportResponse.getEnvironment());
        assertEquals(bugReport.getImageUrls(), bugReportResponse.getImageUrls());
        assertEquals(bugReport.getVideoUrls(), bugReportResponse.getVideoUrls());
        assertEquals(bugReport.getSeverity().getName(), bugReportResponse.getSeverity());
        assertEquals(bugReport.getStatus().getName(), bugReportResponse.getStatus());
        assertEquals(bugReport.getCreatedAt(), bugReportResponse.getCreatedAt());
        assertEquals(bugReport.getUpdatedAt(), bugReportResponse.getUpdatedAt());
        assertTrue(bugReportResponse.hasLinks());

        verify(bugReportRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowNoSuchBugReportByIdExceptionWhenUpdatingBugReport() {
        // given
        BugReportUpdateRequest bugReportUpdateRequest = new BugReportUpdateRequest("new title", null, null, null, null, null, null);

        when(bugReportRepository.findById(1L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchBugReportByIdException.class, () -> bugReportService.updateBugReport(1L, bugReportUpdateRequest));
    }

    @Test
    void shouldThrowNoSuchBugReportSeverityByNameExceptionWhenUpdatingBugReport() {
        // given
        BugReport bugReport = getNewBugReportWithAllFields();
        BugReportUpdateRequest bugReportUpdateRequest = new BugReportUpdateRequest(null, null, null, null, null, BugReportSeverityName.HIGH, null);

        when(bugReportRepository.findById(1L)).thenReturn(Optional.of(bugReport));
        when(bugReportSeverityRepository.findByName(bugReportUpdateRequest.getSeverity())).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchBugReportSeverityByNameException.class, () -> bugReportService.updateBugReport(1L, bugReportUpdateRequest));
    }

    @Test
    void shouldThrowNoSuchBugReportStatusByNameExceptionWhenUpdatingBugReport() {
        // given
        BugReport bugReport = getNewBugReportWithAllFields();
        BugReportUpdateRequest bugReportUpdateRequest = new BugReportUpdateRequest(null, null, null, null, null, null, BugReportStatusName.OPENED);

        when(bugReportRepository.findById(1L)).thenReturn(Optional.of(bugReport));
        when(bugReportStatusRepository.findByName(BugReportStatusName.OPENED)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchBugReportStatusByNameException.class, () -> bugReportService.updateBugReport(1L, bugReportUpdateRequest));
    }

    @Test
    void shouldDeleteAndReturnVoidWhenDeletingExistingBugReport() {
        // given
        BugReport bugReport = getNewBugReportWithAllFields();

        when(bugReportRepository.findById(1L)).thenReturn(Optional.of(bugReport));

        // when
        bugReportService.deleteBugReportById(1L);

        // then
        verify(bugReportRepository, times(1)).delete(bugReport);
    }

    @Test
    void shouldThrowNoSuchBugReportByIdExceptionWhenDeletingBugReport() {
        // given
        when(bugReportRepository.findById(1L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchBugReportByIdException.class, () -> bugReportService.deleteBugReportById(1L));
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllBugReports() {
        // given
        var bugReportToFind = getNewBugReportWithAllFields();

        var pageable = PageRequest.of(0, 2);
        Specification<BugReport> specification = Specification.where(null);

        when(bugReportRepository.findAll(specification, pageable)).thenReturn(new PageImpl<>(List.of(bugReportToFind), pageable, 1));

        // when
        var bugReports = bugReportService.getAllBugReports(pageable, null, null, null, null, null);
        var bugReportResponse = bugReports.getContent().iterator().next();

        // then
        assertNotNull(bugReports);
        assertNotNull(bugReports.getMetadata());
        assertEquals(1, bugReports.getMetadata().getTotalElements());
        assertEquals(1, bugReports.getContent().size());

        assertNotNull(bugReportResponse);
        assertEquals(bugReportToFind.getId(), bugReportResponse.getId());
        assertEquals(bugReportToFind.getReporterId(), bugReportResponse.getReporterId());
        assertEquals(bugReportToFind.getProjectId(), bugReportResponse.getProjectId());
        assertEquals(bugReportToFind.getTitle(), bugReportResponse.getTitle());
        assertEquals(bugReportToFind.getDescription(), bugReportResponse.getDescription());
        assertEquals(bugReportToFind.getEnvironment(), bugReportResponse.getEnvironment());
        assertEquals(bugReportToFind.getImageUrls(), bugReportResponse.getImageUrls());
        assertEquals(bugReportToFind.getVideoUrls(), bugReportResponse.getVideoUrls());
        assertEquals(bugReportToFind.getSeverity().getName(), bugReportResponse.getSeverity());
        assertEquals(bugReportToFind.getStatus().getName(), bugReportResponse.getStatus());
        assertEquals(bugReportToFind.getCreatedAt(), bugReportResponse.getCreatedAt());
        assertEquals(bugReportToFind.getUpdatedAt(), bugReportResponse.getUpdatedAt());
        assertTrue(bugReportResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllBugReportsByProjectId() {
        // given
        var bugReportToFind = getNewBugReportWithAllFields();
        Long projectId = 1L;

        var pageable = PageRequest.of(0, 2);

        when(bugReportRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(bugReportToFind), pageable, 1));

        // when
        var bugReports = bugReportService.getAllBugReports(pageable, projectId, null, null, null, null);
        var bugReportResponse = bugReports.getContent().iterator().next();

        // then
        assertNotNull(bugReports);
        assertNotNull(bugReports.getMetadata());
        assertEquals(1, bugReports.getMetadata().getTotalElements());
        assertEquals(1, bugReports.getContent().size());

        assertNotNull(bugReportResponse);
        assertEquals(bugReportToFind.getId(), bugReportResponse.getId());
        assertEquals(bugReportToFind.getReporterId(), bugReportResponse.getReporterId());
        assertEquals(projectId, bugReportResponse.getProjectId());
        assertEquals(bugReportToFind.getTitle(), bugReportResponse.getTitle());
        assertEquals(bugReportToFind.getDescription(), bugReportResponse.getDescription());
        assertEquals(bugReportToFind.getEnvironment(), bugReportResponse.getEnvironment());
        assertEquals(bugReportToFind.getImageUrls(), bugReportResponse.getImageUrls());
        assertEquals(bugReportToFind.getVideoUrls(), bugReportResponse.getVideoUrls());
        assertEquals(bugReportToFind.getSeverity().getName(), bugReportResponse.getSeverity());
        assertEquals(bugReportToFind.getStatus().getName(), bugReportResponse.getStatus());
        assertEquals(bugReportToFind.getCreatedAt(), bugReportResponse.getCreatedAt());
        assertEquals(bugReportToFind.getUpdatedAt(), bugReportResponse.getUpdatedAt());
        assertTrue(bugReportResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllBugReportsByTitle() {
        // given
        var bugReportToFind = getNewBugReportWithAllFields();
        String title = "title";

        var pageable = PageRequest.of(0, 2);

        when(bugReportRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(bugReportToFind), pageable, 1));

        // when
        var bugReports = bugReportService.getAllBugReports(pageable, null, title, null, null, null);
        var bugReportResponse = bugReports.getContent().iterator().next();

        // then
        assertNotNull(bugReports);
        assertNotNull(bugReports.getMetadata());
        assertEquals(1, bugReports.getMetadata().getTotalElements());
        assertEquals(1, bugReports.getContent().size());

        assertNotNull(bugReportResponse);
        assertEquals(bugReportToFind.getId(), bugReportResponse.getId());
        assertEquals(bugReportToFind.getReporterId(), bugReportResponse.getReporterId());
        assertEquals(bugReportToFind.getProjectId(), bugReportResponse.getProjectId());
        assertEquals(title, bugReportResponse.getTitle());
        assertEquals(bugReportToFind.getDescription(), bugReportResponse.getDescription());
        assertEquals(bugReportToFind.getEnvironment(), bugReportResponse.getEnvironment());
        assertEquals(bugReportToFind.getImageUrls(), bugReportResponse.getImageUrls());
        assertEquals(bugReportToFind.getVideoUrls(), bugReportResponse.getVideoUrls());
        assertEquals(bugReportToFind.getSeverity().getName(), bugReportResponse.getSeverity());
        assertEquals(bugReportToFind.getStatus().getName(), bugReportResponse.getStatus());
        assertEquals(bugReportToFind.getCreatedAt(), bugReportResponse.getCreatedAt());
        assertEquals(bugReportToFind.getUpdatedAt(), bugReportResponse.getUpdatedAt());
        assertTrue(bugReportResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllBugReportsByReporterId() {
        // given
        var bugReportToFind = getNewBugReportWithAllFields();
        Long reporterId = 1L;

        var pageable = PageRequest.of(0, 2);

        when(bugReportRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(bugReportToFind), pageable, 1));

        // when
        var bugReports = bugReportService.getAllBugReports(pageable, null, null, reporterId, null, null);
        var bugReportResponse = bugReports.getContent().iterator().next();

        // then
        assertNotNull(bugReports);
        assertNotNull(bugReports.getMetadata());
        assertEquals(1, bugReports.getMetadata().getTotalElements());
        assertEquals(1, bugReports.getContent().size());

        assertNotNull(bugReportResponse);
        assertEquals(bugReportToFind.getId(), bugReportResponse.getId());
        assertEquals(reporterId, bugReportResponse.getReporterId());
        assertEquals(bugReportToFind.getProjectId(), bugReportResponse.getProjectId());
        assertEquals(bugReportToFind.getTitle(), bugReportResponse.getTitle());
        assertEquals(bugReportToFind.getDescription(), bugReportResponse.getDescription());
        assertEquals(bugReportToFind.getEnvironment(), bugReportResponse.getEnvironment());
        assertEquals(bugReportToFind.getImageUrls(), bugReportResponse.getImageUrls());
        assertEquals(bugReportToFind.getVideoUrls(), bugReportResponse.getVideoUrls());
        assertEquals(bugReportToFind.getSeverity().getName(), bugReportResponse.getSeverity());
        assertEquals(bugReportToFind.getStatus().getName(), bugReportResponse.getStatus());
        assertEquals(bugReportToFind.getCreatedAt(), bugReportResponse.getCreatedAt());
        assertEquals(bugReportToFind.getUpdatedAt(), bugReportResponse.getUpdatedAt());
        assertTrue(bugReportResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllBugReportsBySeverityName() {
        // given
        var bugReportToFind = getNewBugReportWithAllFields();
        BugReportSeverityName severityName = BugReportSeverityName.HIGH;

        var pageable = PageRequest.of(0, 2);

        when(bugReportRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(bugReportToFind), pageable, 1));
        when(bugReportSeverityRepository.findByName(severityName)).thenReturn(Optional.of(new BugReportSeverity(1L, severityName)));

        // when
        var bugReports = bugReportService.getAllBugReports(pageable, null, null, null, severityName, null);
        var bugReportResponse = bugReports.getContent().iterator().next();

        // then
        assertNotNull(bugReports);
        assertNotNull(bugReports.getMetadata());
        assertEquals(1, bugReports.getMetadata().getTotalElements());
        assertEquals(1, bugReports.getContent().size());

        assertNotNull(bugReportResponse);
        assertEquals(bugReportToFind.getId(), bugReportResponse.getId());
        assertEquals(bugReportToFind.getReporterId(), bugReportResponse.getReporterId());
        assertEquals(bugReportToFind.getProjectId(), bugReportResponse.getProjectId());
        assertEquals(bugReportToFind.getTitle(), bugReportResponse.getTitle());
        assertEquals(bugReportToFind.getDescription(), bugReportResponse.getDescription());
        assertEquals(bugReportToFind.getEnvironment(), bugReportResponse.getEnvironment());
        assertEquals(bugReportToFind.getImageUrls(), bugReportResponse.getImageUrls());
        assertEquals(bugReportToFind.getVideoUrls(), bugReportResponse.getVideoUrls());
        assertEquals(severityName, bugReportResponse.getSeverity());
        assertEquals(bugReportToFind.getStatus().getName(), bugReportResponse.getStatus());
        assertEquals(bugReportToFind.getCreatedAt(), bugReportResponse.getCreatedAt());
        assertEquals(bugReportToFind.getUpdatedAt(), bugReportResponse.getUpdatedAt());
        assertTrue(bugReportResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllBugReportsByStatusName() {
        // given
        var bugReportToFind = getNewBugReportWithAllFields();
        BugReportStatusName statusName = BugReportStatusName.OPENED;

        var pageable = PageRequest.of(0, 2);

        when(bugReportRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(bugReportToFind), pageable, 1));
        when(bugReportStatusRepository.findByName(statusName)).thenReturn(Optional.of(new BugReportStatus(1L, statusName)));

        // when
        var bugReports = bugReportService.getAllBugReports(pageable, null, null, null, null, statusName);
        var bugReportResponse = bugReports.getContent().iterator().next();

        // then
        assertNotNull(bugReports);
        assertNotNull(bugReports.getMetadata());
        assertEquals(1, bugReports.getMetadata().getTotalElements());
        assertEquals(1, bugReports.getContent().size());

        assertNotNull(bugReportResponse);
        assertEquals(bugReportToFind.getId(), bugReportResponse.getId());
        assertEquals(bugReportToFind.getReporterId(), bugReportResponse.getReporterId());
        assertEquals(bugReportToFind.getProjectId(), bugReportResponse.getProjectId());
        assertEquals(bugReportToFind.getTitle(), bugReportResponse.getTitle());
        assertEquals(bugReportToFind.getDescription(), bugReportResponse.getDescription());
        assertEquals(bugReportToFind.getEnvironment(), bugReportResponse.getEnvironment());
        assertEquals(bugReportToFind.getImageUrls(), bugReportResponse.getImageUrls());
        assertEquals(bugReportToFind.getVideoUrls(), bugReportResponse.getVideoUrls());
        assertEquals(bugReportToFind.getSeverity().getName(), bugReportResponse.getSeverity());
        assertEquals(statusName, bugReportResponse.getStatus());
        assertEquals(bugReportToFind.getCreatedAt(), bugReportResponse.getCreatedAt());
        assertEquals(bugReportToFind.getUpdatedAt(), bugReportResponse.getUpdatedAt());
        assertTrue(bugReportResponse.hasLinks());
    }

    private BugReport getNewBugReportWithAllFields() {
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        BugReportStatus bugReportStatus = new BugReportStatus(1L, BugReportStatusName.OPENED);
        BugReportSeverity bugReportSeverity = new BugReportSeverity(1L, BugReportSeverityName.HIGH);

        return new BugReport(1L, 1L, "title", "description", "environment", 1L,
                List.of("https://image.com", "https://image2.com"), List.of("https://video.com"),
                bugReportSeverity, bugReportStatus, timeOfCreation, timeOfModification);
    }
}
