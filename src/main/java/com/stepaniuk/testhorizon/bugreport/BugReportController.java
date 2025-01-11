package com.stepaniuk.testhorizon.bugreport;

import com.stepaniuk.testhorizon.bugreport.severity.BugReportSeverityName;
import com.stepaniuk.testhorizon.bugreport.status.BugReportStatusName;
import com.stepaniuk.testhorizon.payload.bugreport.BugReportCreateRequest;
import com.stepaniuk.testhorizon.payload.bugreport.BugReportResponse;
import com.stepaniuk.testhorizon.payload.bugreport.BugReportUpdateRequest;
import com.stepaniuk.testhorizon.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bug-reports", produces = "application/json")
@Validated
public class BugReportController {

    private final BugReportService bugReportService;

    @PostMapping
    public ResponseEntity<BugReportResponse> createBugReport(@Valid @RequestBody BugReportCreateRequest bugReportCreateRequest, @AuthenticationPrincipal User user) {
        return new ResponseEntity<>(bugReportService.createBugReport(bugReportCreateRequest, user.getId()), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BugReportResponse> getBugReportById(@PathVariable Long id) {
        return ResponseEntity.ok(bugReportService.getBugReportById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BugReportResponse> updateBugReport(@PathVariable Long id, @Valid @RequestBody BugReportUpdateRequest bugReportUpdateRequest) {
        return ResponseEntity.ok(bugReportService.updateBugReport(id, bugReportUpdateRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBugReportById(@PathVariable Long id) {
        bugReportService.deleteBugReportById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedModel<BugReportResponse>> getAllBugReports(Pageable pageable,
                                                                          @Nullable @RequestParam(required = false) Long projectId,
                                                                          @Nullable @RequestParam(required = false) String title,
                                                                          @Nullable @RequestParam(required = false) Long reporterId,
                                                                          @Nullable @RequestParam(required = false) BugReportSeverityName severityName,
                                                                          @Nullable @RequestParam(required = false) BugReportStatusName status) {

        return ResponseEntity.ok(bugReportService.getAllBugReports(pageable, projectId, title, reporterId, severityName, status));
    }
}
