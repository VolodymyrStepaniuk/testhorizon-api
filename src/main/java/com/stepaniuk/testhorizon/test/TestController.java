package com.stepaniuk.testhorizon.test;

import com.stepaniuk.testhorizon.payload.test.TestCreateRequest;
import com.stepaniuk.testhorizon.payload.test.TestResponse;
import com.stepaniuk.testhorizon.payload.test.TestUpdateRequest;
import com.stepaniuk.testhorizon.test.type.TestTypeName;
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
@RequestMapping(path = "/tests", produces = "application/json")
public class TestController {

    private final TestService testService;

    @PostMapping
    public ResponseEntity<TestResponse> createTest(@RequestBody TestCreateRequest testCreateRequest, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return new ResponseEntity<>(testService.createTest(testCreateRequest, user.getId()), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestResponse> getTestById(@PathVariable Long id) {
        return ResponseEntity.ok(testService.getTestById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TestResponse> updateTest(@PathVariable Long id, @RequestBody TestUpdateRequest testUpdateRequest) {
        return ResponseEntity.ok(testService.updateTest(id, testUpdateRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestById(@PathVariable Long id) {
        testService.deleteTestById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedModel<TestResponse>> getAllTests(Pageable pageable,
                                                                @Nullable @RequestParam(required = false) Long projectId,
                                                                @Nullable @RequestParam(required = false) Long authorId,
                                                                @Nullable @RequestParam(required = false) Long testCaseId,
                                                                @Nullable @RequestParam(required = false) TestTypeName type) {

        return ResponseEntity.ok(testService.getAllTests(pageable, projectId, authorId, testCaseId, type));
    }

}
