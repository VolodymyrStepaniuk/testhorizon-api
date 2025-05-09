package com.stepaniuk.testhorizon.bugreport;

import com.stepaniuk.testhorizon.bugreport.exceptions.NoSuchBugReportByIdException;
import com.stepaniuk.testhorizon.bugreport.exceptions.NoSuchBugReportSeverityByNameException;
import com.stepaniuk.testhorizon.bugreport.exceptions.NoSuchBugReportStatusByNameException;
import com.stepaniuk.testhorizon.bugreport.severity.BugReportSeverity;
import com.stepaniuk.testhorizon.bugreport.severity.BugReportSeverityRepository;
import com.stepaniuk.testhorizon.bugreport.status.BugReportStatus;
import com.stepaniuk.testhorizon.bugreport.status.BugReportStatusRepository;
import com.stepaniuk.testhorizon.event.bugreport.BugReportCreatedEvent;
import com.stepaniuk.testhorizon.event.bugreport.BugReportDeletedEvent;
import com.stepaniuk.testhorizon.event.bugreport.BugReportUpdatedEvent;
import com.stepaniuk.testhorizon.payload.bugreport.BugReportCreateRequest;
import com.stepaniuk.testhorizon.payload.bugreport.BugReportResponse;
import com.stepaniuk.testhorizon.payload.bugreport.BugReportUpdateRequest;
import com.stepaniuk.testhorizon.payload.info.ProjectInfo;
import com.stepaniuk.testhorizon.project.ProjectRepository;
import com.stepaniuk.testhorizon.project.exceptions.NoSuchProjectByIdException;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.shared.UserInfoService;
import com.stepaniuk.testhorizon.shared.exceptions.AccessToManageEntityDeniedException;
import com.stepaniuk.testhorizon.types.bugreport.BugReportSeverityName;
import com.stepaniuk.testhorizon.types.bugreport.BugReportStatusName;
import com.stepaniuk.testhorizon.types.user.AuthorityName;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.List;
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
    private final UserInfoService userInfoService;

    public BugReportResponse createBugReport(BugReportCreateRequest bugReportCreateRequest, Long reporterId, String correlationId) {
        BugReport bugReport = new BugReport();

        var projectId = bugReportCreateRequest.getProjectId();

        var projectInfo = projectRepository.findById(projectId)
                .map(project -> new ProjectInfo(projectId, project.getTitle(), project.getOwnerId()))
                .orElseThrow(() -> new NoSuchProjectByIdException(projectId));

        bugReport.setProjectId(projectId);
        bugReport.setTitle(bugReportCreateRequest.getTitle());
        bugReport.setDescription(bugReportCreateRequest.getDescription());
        bugReport.setEnvironment(bugReportCreateRequest.getEnvironment());
        bugReport.setReporterId(reporterId);

        bugReport.setSeverity(
                bugReportSeverityRepository.findByName(bugReportCreateRequest.getSeverity())
                        .orElseThrow(() -> new NoSuchBugReportSeverityByNameException(bugReportCreateRequest.getSeverity()))
        );

        bugReport.setStatus(
                bugReportStatusRepository.findByName(BugReportStatusName.OPENED)
                        .orElseThrow(() -> new NoSuchBugReportStatusByNameException(BugReportStatusName.OPENED))
        );

        var savedBugReport = bugReportRepository.save(bugReport);
        var reporter = userInfoService.getUserInfo(reporterId);

        bugReportProducer.send(
                new BugReportCreatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        savedBugReport.getId(), savedBugReport.getProjectId(), savedBugReport.getReporterId()
                )
        );

        return bugReportMapper.toResponse(savedBugReport, projectInfo, reporter);
    }

    public BugReportResponse getBugReportById(Long id) {
        var bugReport = bugReportRepository.findById(id)
                .orElseThrow(() -> new NoSuchBugReportByIdException(id));
        var projectInfo = projectRepository.findById(bugReport.getProjectId())
                .map(project -> new ProjectInfo(project.getId(), project.getTitle(), project.getOwnerId()))
                .orElseThrow(() -> new NoSuchProjectByIdException(bugReport.getProjectId()));
        var reporter = userInfoService.getUserInfo(bugReport.getReporterId());

        return bugReportMapper.toResponse(bugReport, projectInfo, reporter);
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
        var reporter = userInfoService.getUserInfo(updatedBugReport.getReporterId());
        var projectInfo = projectRepository.findById(updatedBugReport.getProjectId())
                .map(project -> new ProjectInfo(project.getId(), project.getTitle(), project.getOwnerId()))
                .orElseThrow(() -> new NoSuchProjectByIdException(updatedBugReport.getProjectId()));

        bugReportProducer.send(
                new BugReportUpdatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        updatedBugReport.getId(), bugReportData
                )
        );

        return bugReportMapper.toResponse(updatedBugReport, projectInfo, reporter);
    }

    public PagedModel<BugReportResponse> getAllBugReports(Pageable pageable,
                                                          @Nullable List<Long> projectIds,
                                                          @Nullable String title,
                                                          @Nullable Long reporterId,
                                                          @Nullable BugReportSeverityName severityName,
                                                          @Nullable BugReportStatusName statusName) {

        Specification<BugReport> specification = Specification.where(null);

        if (projectIds != null && !projectIds.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .in(root.get("projectId")).value(projectIds)
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
                bugReports.map(bugReport -> bugReportMapper.toResponse(bugReport,
                        projectRepository.findById(bugReport.getProjectId())
                                .map(project -> new ProjectInfo(project.getId(), project.getTitle(), project.getOwnerId()))
                                .orElseThrow(() -> new NoSuchProjectByIdException(bugReport.getProjectId())),
                        userInfoService.getUserInfo(bugReport.getReporterId()))),
                URI.create("/bug-reports")
        );
    }

    private boolean hasNoAccessToManageBugReport(Long reporterId, Long projectId, AuthInfo authInfo) {
        Long ownerId = projectRepository.findProjectOwnerIdById(projectId);

        return !(isOwner(authInfo, reporterId) || hasAuthority(authInfo, AuthorityName.ADMIN.name()) || isOwner(authInfo, ownerId));
    }
}
