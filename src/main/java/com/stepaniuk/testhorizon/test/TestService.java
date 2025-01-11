package com.stepaniuk.testhorizon.test;

import com.stepaniuk.testhorizon.payload.test.TestCreateRequest;
import com.stepaniuk.testhorizon.payload.test.TestResponse;
import com.stepaniuk.testhorizon.payload.test.TestUpdateRequest;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.test.exceptions.NoSuchTestByIdException;
import com.stepaniuk.testhorizon.test.exceptions.NoSuchTestTypeByNameException;
import com.stepaniuk.testhorizon.test.type.TestType;
import com.stepaniuk.testhorizon.test.type.TestTypeName;
import com.stepaniuk.testhorizon.test.type.TestTypeRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;
    private final TestTypeRepository testTypeRepository;
    private final TestMapper testMapper;
    private final PageMapper pageMapper;

    public TestResponse createTest(TestCreateRequest testCreateRequest, Long authorId){
        Test test = new Test();

        test.setProjectId(testCreateRequest.getProjectId());
        test.setTestCaseId(testCreateRequest.getTestCaseId());
        test.setAuthorId(authorId);
        test.setTitle(testCreateRequest.getTitle());
        test.setDescription(testCreateRequest.getDescription());
        test.setInstructions(testCreateRequest.getInstructions());
        test.setGithubUrl(testCreateRequest.getGithubUrl());

        test.setType(
                testTypeRepository.findByName(testCreateRequest.getType())
                        .orElseThrow(() -> new NoSuchTestTypeByNameException(testCreateRequest.getType()))
        );

        var savedTest = testRepository.save(test);

        return testMapper.toResponse(savedTest);
    }

    public TestResponse getTestById(Long id){
        var test = testRepository.findById(id)
                .orElseThrow(() -> new NoSuchTestByIdException(id));

        return testMapper.toResponse(test);
    }

    public void deleteTestById(Long id){
        var test = testRepository.findById(id)
                .orElseThrow(() -> new NoSuchTestByIdException(id));

        testRepository.delete(test);
    }

    public TestResponse updateTest(Long id, TestUpdateRequest testUpdateRequest){

        var test = testRepository.findById(id)
                .orElseThrow(() -> new NoSuchTestByIdException(id));

        if (testUpdateRequest.getTestCaseId() != null){
            test.setTestCaseId(testUpdateRequest.getTestCaseId());
        }

        if (testUpdateRequest.getTitle() != null){
            test.setTitle(testUpdateRequest.getTitle());
        }

        if (testUpdateRequest.getDescription() != null){
            test.setDescription(testUpdateRequest.getDescription());
        }

        if (testUpdateRequest.getInstructions() != null){
            test.setInstructions(testUpdateRequest.getInstructions());
        }

        if (testUpdateRequest.getGithubUrl() != null){
            test.setGithubUrl(testUpdateRequest.getGithubUrl());
        }

        if (testUpdateRequest.getType() != null){
            test.setType(
                    testTypeRepository.findByName(testUpdateRequest.getType())
                            .orElseThrow(() -> new NoSuchTestTypeByNameException(testUpdateRequest.getType()))
            );
        }

        var savedTest = testRepository.save(test);

        return testMapper.toResponse(savedTest);
    }

    public PagedModel<TestResponse> getAllTests(Pageable pageable,
                                                @Nullable Long projectId,
                                                @Nullable Long authorId,
                                                @Nullable Long testCaseId,
                                                @Nullable TestTypeName typeName){

        Specification<Test> specification = Specification.where(null);

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

        if (testCaseId != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get("testCaseId"), testCaseId)
            );
        }

        if(typeName != null){
            TestType type = testTypeRepository.findByName(typeName)
                    .orElseThrow(() -> new NoSuchTestTypeByNameException(typeName));

            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get("type"), type)
            );
        }

        var tests = testRepository.findAll(specification, pageable);

        return pageMapper.toResponse(
                tests.map(testMapper::toResponse),
                URI.create("/tests")
        );
    }
}
