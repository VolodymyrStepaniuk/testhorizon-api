package com.stepaniuk.testhorizon.testcase;

import com.stepaniuk.testhorizon.payload.testcase.TestCaseCreateRequest;
import com.stepaniuk.testhorizon.payload.testcase.TestCaseResponse;
import com.stepaniuk.testhorizon.payload.testcase.TestCaseUpdateRequest;
import com.stepaniuk.testhorizon.testcase.priority.TestCasePriorityName;
import com.stepaniuk.testhorizon.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/test-cases", produces = "application/json")
public class TestCaseController {

    private final TestCaseService testCaseService;

    @PostMapping
    public ResponseEntity<TestCaseResponse> createTestCase(@RequestBody TestCaseCreateRequest testCaseCreateRequest, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return new ResponseEntity<>(testCaseService.createTestCase(testCaseCreateRequest, user.getId()), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestCaseResponse> getTestCaseById(@PathVariable Long id) {
        return ResponseEntity.ok(testCaseService.getTestCaseById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TestCaseResponse> updateTestCase(@PathVariable Long id, @RequestBody TestCaseUpdateRequest testCaseUpdateRequest) {
        return ResponseEntity.ok(testCaseService.updateTestCase(id, testCaseUpdateRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestCaseById(@PathVariable Long id) {
        testCaseService.deleteTestCaseById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedModel<TestCaseResponse>> getAllTestCases(Pageable pageable,
                                                                        @Nullable @RequestParam(required = false) Long projectId,
                                                                        @Nullable @RequestParam(required = false) Long authorId,
                                                                        @Nullable @RequestParam(required = false)TestCasePriorityName priority) {

        return ResponseEntity.ok(testCaseService.getAllTestCases(pageable, projectId, authorId, priority));
    }
}
