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
import com.stepaniuk.testhorizon.payload.bugreport.BugReportResponse;
import com.stepaniuk.testhorizon.payload.bugreport.BugReportUpdateRequest;
import com.stepaniuk.testhorizon.shared.PageMapper;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class BugReportService {

    private final BugReportRepository bugReportRepository;
    private final BugReportMapper bugReportMapper;
    private final PageMapper pageMapper;
    private final BugReportStatusRepository bugReportStatusRepository;
    private final BugReportSeverityRepository bugReportSeverityRepository;

    public BugReportResponse createBugReport(BugReportCreateRequest bugReportCreateRequest, Long reporterId){
        BugReport bugReport = new BugReport();

        bugReport.setProjectId(bugReportCreateRequest.getProjectId());
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

        return bugReportMapper.toResponse(savedBugReport);
    }

    public BugReportResponse getBugReportById(Long id){
        return bugReportRepository.findById(id)
                .map(bugReportMapper::toResponse)
                .orElseThrow(() -> new NoSuchBugReportByIdException(id));
    }

    public void deleteBugReportById(Long id){
        var bugReport = bugReportRepository.findById(id)
                .orElseThrow(() -> new NoSuchBugReportByIdException(id));

        bugReportRepository.delete(bugReport);
    }

    public BugReportResponse updateBugReport(Long bugReportId, BugReportUpdateRequest bugReportUpdateRequest){
        var bugReport = bugReportRepository.findById(bugReportId)
                .orElseThrow(() -> new NoSuchBugReportByIdException(bugReportId));

        if (bugReportUpdateRequest.getTitle() != null && !bugReportUpdateRequest.getTitle().isEmpty())
            bugReport.setTitle(bugReportUpdateRequest.getTitle());

        if (bugReportUpdateRequest.getDescription() != null && !bugReportUpdateRequest.getDescription().isEmpty())
            bugReport.setDescription(bugReportUpdateRequest.getDescription());

        if (bugReportUpdateRequest.getEnvironment() != null && !bugReportUpdateRequest.getEnvironment().isEmpty())
            bugReport.setEnvironment(bugReportUpdateRequest.getEnvironment());

        if (bugReportUpdateRequest.getImageUrls() != null && !bugReportUpdateRequest.getImageUrls().isEmpty())
            bugReport.setImageUrls(bugReportUpdateRequest.getImageUrls());

        if (bugReportUpdateRequest.getVideoUrls() != null && !bugReportUpdateRequest.getVideoUrls().isEmpty())
            bugReport.setVideoUrls(bugReportUpdateRequest.getVideoUrls());

        if (bugReportUpdateRequest.getSeverity() != null)
            bugReport.setSeverity(
                    bugReportSeverityRepository.findByName(bugReportUpdateRequest.getSeverity())
                            .orElseThrow(() -> new NoSuchBugReportSeverityByNameException(bugReportUpdateRequest.getSeverity()))
            );

        if (bugReportUpdateRequest.getStatus() != null)
            bugReport.setStatus(
                    bugReportStatusRepository.findByName(bugReportUpdateRequest.getStatus())
                            .orElseThrow(() -> new NoSuchBugReportStatusByNameException(bugReportUpdateRequest.getStatus()))
            );

        var updatedBugReport = bugReportRepository.save(bugReport);

        return bugReportMapper.toResponse(updatedBugReport);
    }

    public PagedModel<BugReportResponse> getAllBugReports(Pageable pageable,
                                                          @Nullable Long projectId,
                                                          @Nullable String title,
                                                          @Nullable Long reporterId,
                                                          @Nullable BugReportSeverityName severityName,
                                                          @Nullable BugReportStatusName statusName){

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
}
