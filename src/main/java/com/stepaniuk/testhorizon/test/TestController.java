package com.stepaniuk.testhorizon.test;

import com.stepaniuk.testhorizon.payload.test.TestCreateRequest;
import com.stepaniuk.testhorizon.payload.test.TestResponse;
import com.stepaniuk.testhorizon.payload.test.TestUpdateRequest;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.test.type.TestTypeName;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/tests", produces = "application/json")
@Validated
public class TestController {

    private final TestService testService;

    @PostMapping
    public ResponseEntity<TestResponse> createTest(@Valid @RequestBody TestCreateRequest testCreateRequest, AuthInfo authInfo) {
        return new ResponseEntity<>(testService.createTest(testCreateRequest, authInfo.getUserId(), UUID.randomUUID().toString()), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestResponse> getTestById(@PathVariable Long id) {
        return ResponseEntity.ok(testService.getTestById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TestResponse> updateTest(@PathVariable Long id, @Valid @RequestBody TestUpdateRequest testUpdateRequest, AuthInfo authInfo) {
        return ResponseEntity.ok(testService.updateTest(id, testUpdateRequest, UUID.randomUUID().toString(), authInfo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTestById(@PathVariable Long id, AuthInfo authInfo) {
        testService.deleteTestById(id, UUID.randomUUID().toString(), authInfo);
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
