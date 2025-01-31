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
import com.stepaniuk.testhorizon.event.bugreport.BugReportCreatedEvent;
import com.stepaniuk.testhorizon.event.bugreport.BugReportDeletedEvent;
import com.stepaniuk.testhorizon.event.bugreport.BugReportUpdatedEvent;
import com.stepaniuk.testhorizon.payload.bugreport.BugReportCreateRequest;
import com.stepaniuk.testhorizon.payload.bugreport.BugReportResponse;
import com.stepaniuk.testhorizon.payload.bugreport.BugReportUpdateRequest;
import com.stepaniuk.testhorizon.project.ProjectRepository;
import com.stepaniuk.testhorizon.project.exception.NoSuchProjectByIdException;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.shared.exception.AccessToManageEntityDeniedException;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.user.authority.AuthorityName;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import static com.stepaniuk.testhorizon.security.SecurityUtils.hasAuthority;
import static com.stepaniuk.testhorizon.security.SecurityUtils.isOwner;

@Service
@RequiredArgsConstructor
public class BugReportService {

    private final BugReportRepository bugReportRepository;
    private final BugReportMapper bugReportMapper;
    private final PageMapper pageMapper;
    private final BugReportStatusRepository bugReportStatusRepository;
    private final BugReportSeverityRepository bugReportSeverityRepository;
    private final BugReportProducer bugReportProducer;
    private final ProjectRepository projectRepository;

    public BugReportResponse createBugReport(BugReportCreateRequest bugReportCreateRequest, Long reporterId, String correlationId) {
        BugReport bugReport = new BugReport();

        var projectId = bugReportCreateRequest.getProjectId();

        if (!projectRepository.existsById(projectId)) {
            throw new NoSuchProjectByIdException(projectId);
        }

        bugReport.setProjectId(projectId);
        bugReport.setTitle(bugReportCreateRequest.getTitle());
        bugReport.setDescription(bugReportCreateRequest.getDescription());
        bugReport.setEnvironment(bugReportCreateRequest.getEnvironment());
        bugReport.setReporterId(reporterId);
        bugReport.setImageUrls(bugReportCreateRequest.getImageUrls());
        bugReport.setVideoUrls(bugReportCreateRequest.getVideoUrls());

        bugReport.setSeverity(
                bugReportSeverityRepository.findByName(bugReportCreateRequest.getSeverity())
                        .orElseThrow(() -> new NoSuchBugReportSeverityByNameException(bugReportCreateRequest.getSeverity()))
        );

        bugReport.setStatus(
                bugReportStatusRepository.findByName(BugReportStatusName.OPENED)
                        .orElseThrow(() -> new NoSuchBugReportStatusByNameException(BugReportStatusName.OPENED))
        );

        var savedBugReport = bugReportRepository.save(bugReport);

        bugReportProducer.send(
                new BugReportCreatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        savedBugReport.getId(), savedBugReport.getProjectId(), savedBugReport.getReporterId()
                )
        );

        return bugReportMapper.toResponse(savedBugReport);
    }

    public BugReportResponse getBugReportById(Long id) {
        return bugReportRepository.findById(id)
                .map(bugReportMapper::toResponse)
                .orElseThrow(() -> new NoSuchBugReportByIdException(id));
    }

    public void deleteBugReportById(Long id, String correlationId, AuthInfo authInfo) {
        var bugReport = bugReportRepository.findById(id)
                .orElseThrow(() -> new NoSuchBugReportByIdException(id));

        if (hasNoAccessToManageBugReport(bugReport.getReporterId(), bugReport.getProjectId(), authInfo)) {
            throw new AccessToManageEntityDeniedException("BugReport", "/bug-reports");
        }

        bugReportRepository.delete(bugReport);

        bugReportProducer.send(
                new BugReportDeletedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        id
                )
        );
    }

    public BugReportResponse updateBugReport(Long bugReportId, BugReportUpdateRequest bugReportUpdateRequest, String correlationId, AuthInfo authInfo) {
        var bugReport = bugReportRepository.findById(bugReportId)
                .orElseThrow(() -> new NoSuchBugReportByIdException(bugReportId));

        if (hasNoAccessToManageBugReport(bugReport.getReporterId(), bugReport.getProjectId(), authInfo)) {
            throw new AccessToManageEntityDeniedException("BugReport", "/bug-reports");
        }

        var bugReportData = new BugReportUpdatedEvent.Data();

        if (bugReportUpdateRequest.getTitle() != null && !bugReportUpdateRequest.getTitle().isEmpty()) {
            bugReport.setTitle(bugReportUpdateRequest.getTitle());
            bugReportData.setTitle(bugReportUpdateRequest.getTitle());
        }

        if (bugReportUpdateRequest.getDescription() != null && !bugReportUpdateRequest.getDescription().isEmpty()) {
            bugReport.setDescription(bugReportUpdateRequest.getDescription());
            bugReportData.setDescription(bugReportUpdateRequest.getDescription());
        }

        if (bugReportUpdateRequest.getEnvironment() != null && !bugReportUpdateRequest.getEnvironment().isEmpty()) {
            bugReport.setEnvironment(bugReportUpdateRequest.getEnvironment());
            bugReportData.setEnvironment(bugReportUpdateRequest.getEnvironment());
        }
        if (bugReportUpdateRequest.getImageUrls() != null && !bugReportUpdateRequest.getImageUrls().isEmpty()) {
            bugReport.setImageUrls(bugReportUpdateRequest.getImageUrls());
            bugReportData.setImageUrls(bugReportUpdateRequest.getImageUrls());
        }

        if (bugReportUpdateRequest.getVideoUrls() != null && !bugReportUpdateRequest.getVideoUrls().isEmpty()) {
            bugReport.setVideoUrls(bugReportUpdateRequest.getVideoUrls());
            bugReportData.setVideoUrls(bugReportUpdateRequest.getVideoUrls());
        }

        if (bugReportUpdateRequest.getSeverity() != null) {
            bugReport.setSeverity(
                    bugReportSeverityRepository.findByName(bugReportUpdateRequest.getSeverity())
                            .orElseThrow(() -> new NoSuchBugReportSeverityByNameException(bugReportUpdateRequest.getSeverity()))
            );

            bugReportData.setSeverity(bugReportUpdateRequest.getSeverity());
        }

        if (bugReportUpdateRequest.getStatus() != null) {
            bugReport.setStatus(
                    bugReportStatusRepository.findByName(bugReportUpdateRequest.getStatus())
                            .orElseThrow(() -> new NoSuchBugReportStatusByNameException(bugReportUpdateRequest.getStatus()))
            );

            bugReportData.setStatus(bugReportUpdateRequest.getStatus());
        }

        var updatedBugReport = bugReportRepository.save(bugReport);

        bugReportProducer.send(
                new BugReportUpdatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        updatedBugReport.getId(), bugReportData
                )
        );

        return bugReportMapper.toResponse(updatedBugReport);
    }

    public PagedModel<BugReportResponse> getAllBugReports(Pageable pageable,
                                                          @Nullable Long projectId,
                                                          @Nullable String title,
                                                          @Nullable Long reporterId,
                                                          @Nullable BugReportSeverityName severityName,
                                                          @Nullable BugReportStatusName statusName) {

        Specification<BugReport> specification = Specification.where(null);

        if (projectId != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get("projectId"), projectId)
            );
        }

        if (title != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%")
            );
        }

        if (reporterId != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get("reporterId"), reporterId)
            );
        }

        if (severityName != null) {
            BugReportSeverity severity = bugReportSeverityRepository.findByName(severityName)
                    .orElseThrow(() -> new NoSuchBugReportSeverityByNameException(severityName));

            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get("severity"), severity)
            );
        }

        if (statusName != null) {
            BugReportStatus status = bugReportStatusRepository.findByName(statusName)
                    .orElseThrow(() -> new NoSuchBugReportStatusByNameException(statusName));

            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get("status"), status)
            );
        }

        var bugReports = bugReportRepository.findAll(specification, pageable);

        return pageMapper.toResponse(
                bugReports.map(
                        bugReportMapper::toResponse
                ), URI.create("/bug-reports")
        );
    }

    private boolean hasNoAccessToManageBugReport(Long reporterId, Long projectId, AuthInfo authInfo) {
        Long ownerId = projectRepository.findProjectOwnerIdById(projectId);

        return !(isOwner(authInfo, reporterId) || hasAuthority(authInfo, AuthorityName.ADMIN.name()) || isOwner(authInfo, ownerId));
    }
}
