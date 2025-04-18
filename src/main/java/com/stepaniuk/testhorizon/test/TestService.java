package com.stepaniuk.testhorizon.test;

import com.stepaniuk.testhorizon.event.test.TestCreatedEvent;
import com.stepaniuk.testhorizon.event.test.TestDeletedEvent;
import com.stepaniuk.testhorizon.event.test.TestUpdatedEvent;
import com.stepaniuk.testhorizon.payload.info.ProjectInfo;
import com.stepaniuk.testhorizon.payload.info.TestCaseInfo;
import com.stepaniuk.testhorizon.payload.test.TestCreateRequest;
import com.stepaniuk.testhorizon.payload.test.TestResponse;
import com.stepaniuk.testhorizon.payload.test.TestUpdateRequest;
import com.stepaniuk.testhorizon.project.ProjectRepository;
import com.stepaniuk.testhorizon.project.exceptions.NoSuchProjectByIdException;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.shared.UserInfoService;
import com.stepaniuk.testhorizon.shared.exceptions.AccessToManageEntityDeniedException;
import com.stepaniuk.testhorizon.test.exceptions.NoSuchTestByIdException;
import com.stepaniuk.testhorizon.test.exceptions.NoSuchTestTypeByNameException;
import com.stepaniuk.testhorizon.test.type.TestType;
import com.stepaniuk.testhorizon.test.type.TestTypeRepository;
import com.stepaniuk.testhorizon.testcase.TestCaseRepository;
import com.stepaniuk.testhorizon.testcase.exceptions.NoSuchTestCaseByIdException;
import com.stepaniuk.testhorizon.types.test.TestTypeName;
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
public class TestService {

    private final TestRepository testRepository;
    private final TestTypeRepository testTypeRepository;
    private final ProjectRepository projectRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestMapper testMapper;
    private final PageMapper pageMapper;
    private final TestProducer testProducer;
    private final UserInfoService userInfoService;

    public TestResponse createTest(TestCreateRequest testCreateRequest, Long authorId, String correlationId) {
        Test test = new Test();

        var projectId = testCreateRequest.getProjectId();
        var testCaseId = testCreateRequest.getTestCaseId();

        var projectInfo = projectRepository.findById(projectId)
                .map(project -> new ProjectInfo(projectId, project.getTitle(), project.getOwnerId()))
                .orElseThrow(() -> new NoSuchProjectByIdException(projectId));

        test.setProjectId(testCreateRequest.getProjectId());
        test.setTestCaseId(testCaseId);
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
        var authorInfo = userInfoService.getUserInfo(savedTest.getAuthorId());

        testProducer.send(
                new TestCreatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        savedTest.getId(), savedTest.getProjectId(), savedTest.getAuthorId()
                )
        );

        TestCaseInfo testCaseInfo = null;

        if (testCaseId != null) {
            testCaseInfo = findTestCaseInfo(testCaseId);
        }

        return testMapper.toResponse(savedTest, projectInfo, authorInfo, testCaseInfo);
    }

    public TestResponse getTestById(Long id) {
        var test = testRepository.findById(id)
                .orElseThrow(() -> new NoSuchTestByIdException(id));
        var projectInfo = projectRepository.findById(test.getProjectId())
                .map(project -> new ProjectInfo(project.getId(), project.getTitle(), project.getOwnerId()))
                .orElseThrow(() -> new NoSuchProjectByIdException(test.getProjectId()));
        var authorInfo = userInfoService.getUserInfo(test.getAuthorId());

        var testCaseInfo = test.getTestCaseId() != null
                ? findTestCaseInfo(test.getTestCaseId())
                : null;

        return testMapper.toResponse(test, projectInfo, authorInfo, testCaseInfo);
    }

    public void deleteTestById(Long id, String correlationId, AuthInfo authInfo) {
        var test = testRepository.findById(id)
                .orElseThrow(() -> new NoSuchTestByIdException(id));

        if (hasNoAccessToManageTest(test.getAuthorId(), test.getProjectId(), authInfo)) {
            throw new AccessToManageEntityDeniedException("Test", "/projects");
        }

        testRepository.delete(test);

        testProducer.send(
                new TestDeletedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        id
                )
        );
    }

    public TestResponse updateTest(Long id, TestUpdateRequest testUpdateRequest, String correlationId, AuthInfo authInfo) {

        var test = testRepository.findById(id)
                .orElseThrow(() -> new NoSuchTestByIdException(id));

        if (hasNoAccessToManageTest(test.getAuthorId(), test.getProjectId(), authInfo)) {
            throw new AccessToManageEntityDeniedException("Test", "/projects");
        }

        var testData = new TestUpdatedEvent.Data();
        TestCaseInfo testCaseInfo = null;

        if (testUpdateRequest.getTestCaseId() != null) {
            test.setTestCaseId(testUpdateRequest.getTestCaseId());
            testData.setTestCaseId(testUpdateRequest.getTestCaseId());
            testCaseInfo = findTestCaseInfo(testUpdateRequest.getTestCaseId());
        }

        if (testUpdateRequest.getTitle() != null) {
            test.setTitle(testUpdateRequest.getTitle());
            testData.setTitle(testUpdateRequest.getTitle());
        }

        if (testUpdateRequest.getDescription() != null) {
            test.setDescription(testUpdateRequest.getDescription());
            testData.setDescription(testUpdateRequest.getDescription());
        }

        if (testUpdateRequest.getInstructions() != null) {
            test.setInstructions(testUpdateRequest.getInstructions());
            testData.setInstructions(testUpdateRequest.getInstructions());
        }

        if (testUpdateRequest.getGithubUrl() != null) {
            test.setGithubUrl(testUpdateRequest.getGithubUrl());
            testData.setGithubUrl(testUpdateRequest.getGithubUrl());
        }

        if (testUpdateRequest.getType() != null) {
            test.setType(
                    testTypeRepository.findByName(testUpdateRequest.getType())
                            .orElseThrow(() -> new NoSuchTestTypeByNameException(testUpdateRequest.getType()))
            );

            testData.setType(testUpdateRequest.getType());
        }

        var savedTest = testRepository.save(test);
        var projectInfo = projectRepository.findById(savedTest.getProjectId())
                .map(project -> new ProjectInfo(project.getId(), project.getTitle(), project.getOwnerId()))
                .orElseThrow(() -> new NoSuchProjectByIdException(savedTest.getProjectId()));
        var authorInfo = userInfoService.getUserInfo(savedTest.getAuthorId());

        testProducer.send(
                new TestUpdatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        savedTest.getId(), testData
                )
        );

        return testMapper.toResponse(savedTest, projectInfo, authorInfo, testCaseInfo);
    }

    public PagedModel<TestResponse> getAllTests(Pageable pageable,
                                                String title,
                                                @Nullable List<Long> projectIds,
                                                @Nullable Long authorId,
                                                @Nullable Long testCaseId,
                                                @Nullable TestTypeName typeName) {

        Specification<Test> specification = Specification.where(null);

        if (title != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%")
            );
        }

        if (projectIds != null && !projectIds.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .in(root.get("projectId")).value(projectIds)
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

        if (typeName != null) {
            TestType type = testTypeRepository.findByName(typeName)
                    .orElseThrow(() -> new NoSuchTestTypeByNameException(typeName));

            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get("type"), type)
            );
        }

        var tests = testRepository.findAll(specification, pageable);

        return pageMapper.toResponse(
                tests.map(test -> testMapper.toResponse(
                        test,
                        projectRepository.findById(test.getProjectId())
                                .map(project -> new ProjectInfo(project.getId(), project.getTitle(), project.getOwnerId()))
                                .orElseThrow(() -> new NoSuchProjectByIdException(test.getProjectId())),
                        userInfoService.getUserInfo(test.getAuthorId()),
                        test.getTestCaseId() != null
                                ? findTestCaseInfo(test.getTestCaseId())
                                : null
                )),
                URI.create("/tests")
        );
    }

    private TestCaseInfo findTestCaseInfo(Long testCaseId) {
        return testCaseRepository.findById(testCaseId)
                .map(testCase -> new TestCaseInfo(testCase.getId(), testCase.getTitle()))
                .orElseThrow(() -> new NoSuchTestCaseByIdException(testCaseId));
    }

    private boolean hasNoAccessToManageTest(Long ownerId, Long projectId, AuthInfo authInfo) {
        Long projectOwnerId = projectRepository.findProjectOwnerIdById(projectId);

        return !(isOwner(authInfo, ownerId) || hasAuthority(authInfo, AuthorityName.ADMIN.name()) || isOwner(authInfo, projectOwnerId));
    }
}
