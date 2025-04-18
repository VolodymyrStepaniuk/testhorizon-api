package com.stepaniuk.testhorizon.testcase;

import com.stepaniuk.testhorizon.event.testcase.TestCaseCreatedEvent;
import com.stepaniuk.testhorizon.event.testcase.TestCaseDeletedEvent;
import com.stepaniuk.testhorizon.event.testcase.TestCaseUpdatedEvent;
import com.stepaniuk.testhorizon.payload.info.ProjectInfo;
import com.stepaniuk.testhorizon.payload.testcase.TestCaseCreateRequest;
import com.stepaniuk.testhorizon.payload.testcase.TestCaseResponse;
import com.stepaniuk.testhorizon.payload.testcase.TestCaseUpdateRequest;
import com.stepaniuk.testhorizon.project.ProjectRepository;
import com.stepaniuk.testhorizon.project.exceptions.NoSuchProjectByIdException;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.shared.UserInfoService;
import com.stepaniuk.testhorizon.shared.exceptions.AccessToManageEntityDeniedException;
import com.stepaniuk.testhorizon.testcase.exceptions.NoSuchTestCaseByIdException;
import com.stepaniuk.testhorizon.testcase.exceptions.NoSuchTestCasePriorityByNameException;
import com.stepaniuk.testhorizon.testcase.priority.TestCasePriority;
import com.stepaniuk.testhorizon.testcase.priority.TestCasePriorityRepository;
import com.stepaniuk.testhorizon.types.testcase.TestCasePriorityName;
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
public class TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final ProjectRepository projectRepository;
    private final TestCaseMapper testCaseMapper;
    private final PageMapper pageMapper;
    private final TestCasePriorityRepository testCasePriorityRepository;
    private final TestCaseProducer testCaseProducer;
    private final UserInfoService userInfoService;

    public TestCaseResponse createTestCase(TestCaseCreateRequest testCaseCreateRequest, Long authorId, String correlationId) {
        TestCase testCase = new TestCase();

        var projectId = testCaseCreateRequest.getProjectId();

        var projectInfo = projectRepository.findById(projectId)
                .map(project -> new ProjectInfo(projectId, project.getTitle(), project.getOwnerId()))
                .orElseThrow(() -> new NoSuchProjectByIdException(projectId));

        testCase.setProjectId(projectId);
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
        var authorInfo = userInfoService.getUserInfo(savedTestCase.getAuthorId());


        testCaseProducer.send(
                new TestCaseCreatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        savedTestCase.getId(), savedTestCase.getProjectId(), savedTestCase.getAuthorId()
                )
        );

        return testCaseMapper.toResponse(savedTestCase, projectInfo, authorInfo);
    }

    public TestCaseResponse getTestCaseById(Long id) {
        var testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new NoSuchTestCaseByIdException(id));
        var authorInfo = userInfoService.getUserInfo(testCase.getAuthorId());
        var projectInfo = projectRepository.findById(testCase.getProjectId())
                .map(project -> new ProjectInfo(project.getId(), project.getTitle(), project.getOwnerId()))
                .orElseThrow(() -> new NoSuchProjectByIdException(testCase.getProjectId()));

        return testCaseMapper.toResponse(testCase, projectInfo, authorInfo);
    }

    public void deleteTestCaseById(Long id, String correlationId, AuthInfo authInfo) {

        var testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new NoSuchTestCaseByIdException(id));

        if (hasNoAccessToManageTestCase(testCase.getAuthorId(), testCase.getProjectId(), authInfo)) {
            throw new AccessToManageEntityDeniedException("TestCase", "/test-cases");
        }

        testCaseRepository.delete(testCase);

        testCaseProducer.send(
                new TestCaseDeletedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        id
                )
        );
    }

    public TestCaseResponse updateTestCase(Long id, TestCaseUpdateRequest testCaseUpdateRequest, String correlationId, AuthInfo authInfo) {
        var testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new NoSuchTestCaseByIdException(id));

        if (hasNoAccessToManageTestCase(testCase.getAuthorId(), testCase.getProjectId(), authInfo)) {
            throw new AccessToManageEntityDeniedException("TestCase", "/test-cases");
        }

        var testCaseData = new TestCaseUpdatedEvent.Data();

        if (testCaseUpdateRequest.getTitle() != null) {
            testCase.setTitle(testCaseUpdateRequest.getTitle());
            testCaseData.setTitle(testCaseUpdateRequest.getTitle());
        }

        if (testCaseUpdateRequest.getDescription() != null) {
            testCase.setDescription(testCaseUpdateRequest.getDescription());
            testCaseData.setDescription(testCaseUpdateRequest.getDescription());
        }

        if (testCaseUpdateRequest.getPreconditions() != null) {
            testCase.setPreconditions(testCaseUpdateRequest.getPreconditions());
            testCaseData.setPreconditions(testCaseUpdateRequest.getPreconditions());
        }

        if (testCaseUpdateRequest.getInputData() != null) {
            testCase.setInputData(testCaseUpdateRequest.getInputData());
            testCaseData.setInputData(testCaseUpdateRequest.getInputData());
        }

        if (testCaseUpdateRequest.getSteps() != null) {
            testCase.setSteps(testCaseUpdateRequest.getSteps());
            testCaseData.setSteps(testCaseUpdateRequest.getSteps());
        }

        if (testCaseUpdateRequest.getPriority() != null) {
            testCase.setPriority(
                    testCasePriorityRepository.findByName(testCaseUpdateRequest.getPriority())
                            .orElseThrow(() -> new NoSuchTestCasePriorityByNameException(testCaseUpdateRequest.getPriority()))
            );

            testCaseData.setPriority(testCaseUpdateRequest.getPriority());
        }

        var updatedTestCase = testCaseRepository.save(testCase);
        var authorInfo = userInfoService.getUserInfo(updatedTestCase.getAuthorId());
        var projectInfo = projectRepository.findById(updatedTestCase.getProjectId())
                .map(project -> new ProjectInfo(project.getId(), project.getTitle(), project.getOwnerId()))
                .orElseThrow(() -> new NoSuchProjectByIdException(updatedTestCase.getProjectId()));

        testCaseProducer.send(
                new TestCaseUpdatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        updatedTestCase.getId(), testCaseData
                )
        );

        return testCaseMapper.toResponse(updatedTestCase, projectInfo, authorInfo);
    }

    public PagedModel<TestCaseResponse> getAllTestCases(Pageable pageable,
                                                        String title,
                                                        @Nullable List<Long> projectIds,
                                                        @Nullable Long authorId,
                                                        @Nullable TestCasePriorityName priorityName) {

        Specification<TestCase> specification = Specification.where(null);

        if(title != null) {
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

        if (priorityName != null) {
            TestCasePriority priority = testCasePriorityRepository.findByName(priorityName)
                    .orElseThrow(() -> new NoSuchTestCasePriorityByNameException(priorityName));

            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get("priority"), priority)
            );
        }

        var testCases = testCaseRepository.findAll(specification, pageable);

        return pageMapper.toResponse(
                testCases.map(testCase -> testCaseMapper.toResponse(
                        testCase,
                        projectRepository.findById(testCase.getProjectId())
                                .map(project -> new ProjectInfo(project.getId(), project.getTitle(), project.getOwnerId()))
                                .orElseThrow(() -> new NoSuchProjectByIdException(testCase.getProjectId())),
                        userInfoService.getUserInfo(testCase.getAuthorId())
                )),
                URI.create("/test-cases")
        );
    }

    private boolean hasNoAccessToManageTestCase(Long ownerId, Long projectId, AuthInfo authInfo) {
        Long projectOwnerId = projectRepository.findProjectOwnerIdById(projectId);

        return !(isOwner(authInfo, ownerId) || hasAuthority(authInfo, AuthorityName.ADMIN.name()) || isOwner(authInfo, projectOwnerId));
    }
}
