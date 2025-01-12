package com.stepaniuk.testhorizon.testcase;

import com.stepaniuk.testhorizon.payload.testcase.TestCaseCreateRequest;
import com.stepaniuk.testhorizon.payload.testcase.TestCaseResponse;
import com.stepaniuk.testhorizon.payload.testcase.TestCaseUpdateRequest;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.testcase.exceptions.NoSuchTestCasePriorityByNameException;
import com.stepaniuk.testhorizon.testcase.exceptions.NoSuchTestCaseByIdException;
import com.stepaniuk.testhorizon.testcase.priority.TestCasePriority;
import com.stepaniuk.testhorizon.testcase.priority.TestCasePriorityName;
import com.stepaniuk.testhorizon.testcase.priority.TestCasePriorityRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final TestCaseMapper testCaseMapper;
    private final PageMapper pageMapper;
    private final TestCasePriorityRepository testCasePriorityRepository;

    public TestCaseResponse createTestCase(TestCaseCreateRequest testCaseCreateRequest, Long authorId){
        TestCase testCase = new TestCase();

        testCase.setProjectId(testCaseCreateRequest.getProjectId());
        testCase.setAuthorId(authorId);
        testCase.setTitle(testCaseCreateRequest.getTitle());
        testCase.setDescription(testCaseCreateRequest.getDescription());
        testCase.setPreconditions(testCaseCreateRequest.getPreconditions());
        testCase.setInputData(testCaseCreateRequest.getInputData());
        testCase.setSteps(testCaseCreateRequest.getSteps());

        testCase.setPriority(
                testCasePriorityRepository.findByName(testCaseCreateRequest.getPriority())
                        .orElseThrow(() -> new NoSuchTestCasePriorityByNameException(testCaseCreateRequest.getPriority()))
        );

        var savedTestCase = testCaseRepository.save(testCase);

        return testCaseMapper.toResponse(savedTestCase);
    }

    public TestCaseResponse getTestCaseById(Long id){
        var testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new NoSuchTestCaseByIdException(id));

        return testCaseMapper.toResponse(testCase);
    }

    public void deleteTestCaseById(Long id){
        var testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new NoSuchTestCaseByIdException(id));

        testCaseRepository.delete(testCase);
    }

    public TestCaseResponse updateTestCase(Long id, TestCaseUpdateRequest testCaseUpdateRequest){
        var testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new NoSuchTestCaseByIdException(id));

        if(testCaseUpdateRequest.getTitle() != null){
            testCase.setTitle(testCaseUpdateRequest.getTitle());
        }

        if (testCaseUpdateRequest.getDescription() != null){
            testCase.setDescription(testCaseUpdateRequest.getDescription());
        }

        if (testCaseUpdateRequest.getPreconditions() != null){
            testCase.setPreconditions(testCaseUpdateRequest.getPreconditions());
        }

        if (testCaseUpdateRequest.getInputData() != null){
            testCase.setInputData(testCaseUpdateRequest.getInputData());
        }

        if (testCaseUpdateRequest.getSteps() != null){
            testCase.setSteps(testCaseUpdateRequest.getSteps());
        }

        if (testCaseUpdateRequest.getPriority() != null){
            testCase.setPriority(
                    testCasePriorityRepository.findByName(testCaseUpdateRequest.getPriority())
                            .orElseThrow(() -> new NoSuchTestCasePriorityByNameException(testCaseUpdateRequest.getPriority()))
            );
        }

        var updatedTestCase = testCaseRepository.save(testCase);

        return testCaseMapper.toResponse(updatedTestCase);
    }

    public PagedModel<TestCaseResponse> getAllTestCases(Pageable pageable,
                                                        @Nullable Long projectId,
                                                        @Nullable Long authorId,
                                                        @Nullable TestCasePriorityName priorityName){

        Specification<TestCase> specification = Specification.where(null);

        if (projectId != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get("projectId"), projectId)
            );
        }

        if (authorId != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get("authorId"), authorId)
            );
        }

        if (priorityName != null) {
            TestCasePriority priority = testCasePriorityRepository.findByName(priorityName)
                    .orElseThrow(() -> new NoSuchTestCasePriorityByNameException(priorityName));

            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get("priority"), priority)
            );
        }

        var testCases = testCaseRepository.findAll(specification, pageable);

        return pageMapper.toResponse(
                testCases.map(testCaseMapper::toResponse),
                URI.create("/test-cases")
        );
    }
}
