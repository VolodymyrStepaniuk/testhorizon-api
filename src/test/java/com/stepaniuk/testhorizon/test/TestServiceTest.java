package com.stepaniuk.testhorizon.test;

import com.stepaniuk.testhorizon.event.test.TestCreatedEvent;
import com.stepaniuk.testhorizon.event.test.TestDeletedEvent;
import com.stepaniuk.testhorizon.event.test.TestEvent;
import com.stepaniuk.testhorizon.event.test.TestUpdatedEvent;
import com.stepaniuk.testhorizon.payload.test.TestCreateRequest;
import com.stepaniuk.testhorizon.payload.test.TestUpdateRequest;
import com.stepaniuk.testhorizon.shared.PageMapperImpl;
import com.stepaniuk.testhorizon.test.exceptions.NoSuchTestByIdException;
import com.stepaniuk.testhorizon.test.exceptions.NoSuchTestTypeByNameException;
import com.stepaniuk.testhorizon.test.type.TestType;
import com.stepaniuk.testhorizon.test.type.TestTypeName;
import com.stepaniuk.testhorizon.test.type.TestTypeRepository;
import com.stepaniuk.testhorizon.testspecific.ServiceLevelUnitTest;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.mockito.AdditionalAnswers;
import org.mockito.stubbing.Answer1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ServiceLevelUnitTest
@ContextConfiguration(classes = {TestService.class, TestMapperImpl.class, PageMapperImpl.class})
class TestServiceTest {

    @Autowired
    private TestService testService;

    @MockitoBean
    private TestProducer testProducer;

    @MockitoBean
    private TestRepository testRepository;

    @MockitoBean
    private TestTypeRepository testTypeRepository;

    @org.junit.jupiter.api.Test
    void shouldReturnTestResponseWhenCreatingTest() {
        // given
        var testCreateRequest = new TestCreateRequest(1L, 1L, "title", "description", "instructions", "githubUrl", TestTypeName.UNIT);
        var testType = new TestType(1L, TestTypeName.UNIT);

        when(testTypeRepository.findByName(TestTypeName.UNIT)).thenReturn(Optional.of(testType));
        when(testRepository.save(any())).thenAnswer(answer(getFakeSave(1L)));
        final var receivedEventWrapper = new TestCreatedEvent[1];
        when(
                testProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (TestCreatedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        // when
        var testResponse = testService.createTest(testCreateRequest, 1L, UUID.randomUUID().toString());

        // then
        assertNotNull(testResponse);
        assertEquals(1L, testResponse.getAuthorId());
        assertEquals(testCreateRequest.getProjectId(), testResponse.getProjectId());
        assertEquals(testCreateRequest.getTestCaseId(), testResponse.getTestCaseId());
        assertEquals(testCreateRequest.getTitle(), testResponse.getTitle());
        assertEquals(testCreateRequest.getDescription(), testResponse.getDescription());
        assertEquals(testCreateRequest.getInstructions(), testResponse.getInstructions());
        assertEquals(testCreateRequest.getGithubUrl(), testResponse.getGithubUrl());
        assertEquals(testCreateRequest.getType(), testResponse.getType());
        assertTrue(testResponse.hasLinks());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(1L, receivedEvent.getTestId());
        assertEquals(testResponse.getProjectId(), receivedEvent.getProjectId());
        assertEquals(testResponse.getAuthorId(), receivedEvent.getAuthorId());

        verify(testRepository, times(1)).save(any());
    }

    @org.junit.jupiter.api.Test
    void shouldThrowNoSuchTestTypeByNameExceptionWhenCreatingTestWithNonExistingType() {
        // given
        var correlationId = UUID.randomUUID().toString();
        var testCreateRequest = new TestCreateRequest(1L, 1L, "title", "description", "instructions", "githubUrl", TestTypeName.INTEGRATION);

        when(testTypeRepository.findByName(TestTypeName.INTEGRATION)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchTestTypeByNameException.class, () -> testService.createTest(testCreateRequest, 1L, correlationId));
    }

    @org.junit.jupiter.api.Test
    void shouldReturnTestResponseWhenGetByExistingId() {
        // given
        Test testToFind = getNewTestWithAllFields();

        when(testRepository.findById(1L)).thenReturn(Optional.of(testToFind));

        // when
        var testResponse = testService.getTestById(1L);

        // then
        assertNotNull(testResponse);
        assertEquals(testToFind.getId(), testResponse.getId());
        assertEquals(testToFind.getProjectId(), testResponse.getProjectId());
        assertEquals(testToFind.getAuthorId(), testResponse.getAuthorId());
        assertEquals(testToFind.getTitle(), testResponse.getTitle());
        assertEquals(testToFind.getDescription(), testResponse.getDescription());
        assertEquals(testToFind.getInstructions(), testResponse.getInstructions());
        assertEquals(testToFind.getGithubUrl(), testResponse.getGithubUrl());
        assertEquals(testToFind.getType().getName(), testResponse.getType());
        assertEquals(testToFind.getCreatedAt(), testResponse.getCreatedAt());
        assertEquals(testToFind.getUpdatedAt(), testResponse.getUpdatedAt());
        assertTrue(testResponse.hasLinks());
    }

    @org.junit.jupiter.api.Test
    void shouldThrowNoSuchTestByIdExceptionWhenGetByNonExistingId() {
        // given
        when(testRepository.findById(100L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchTestByIdException.class, () -> testService.getTestById(100L));
    }

    @org.junit.jupiter.api.Test
    void shouldUpdateAndReturnTestResponseWhenChangingTitle() {
        // given
        Test testToUpdate = getNewTestWithAllFields();
        var testUpdateRequest = new TestUpdateRequest(null, "newTitle", null, null, null, null);

        when(testRepository.findById(1L)).thenReturn(Optional.of(testToUpdate));
        when(testRepository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
        final var receivedEventWrapper = new TestUpdatedEvent[1];
        when(
                testProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (TestUpdatedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        // when
        var testResponse = testService.updateTest(1L, testUpdateRequest, UUID.randomUUID().toString());

        // then
        assertNotNull(testResponse);
        assertEquals(testToUpdate.getId(), testResponse.getId());
        assertEquals(testToUpdate.getProjectId(), testResponse.getProjectId());
        assertEquals(testToUpdate.getAuthorId(), testResponse.getAuthorId());
        assertEquals(testUpdateRequest.getTitle(), testResponse.getTitle());
        assertEquals(testToUpdate.getDescription(), testResponse.getDescription());
        assertEquals(testToUpdate.getInstructions(), testResponse.getInstructions());
        assertEquals(testToUpdate.getGithubUrl(), testResponse.getGithubUrl());
        assertEquals(testToUpdate.getType().getName(), testResponse.getType());
        assertEquals(testToUpdate.getCreatedAt(), testResponse.getCreatedAt());
        assertEquals(testToUpdate.getUpdatedAt(), testResponse.getUpdatedAt());
        assertTrue(testResponse.hasLinks());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(testResponse.getId(), receivedEvent.getTestId());
        assertEquals(testResponse.getTitle(), receivedEvent.getData().getTitle());
        assertNull(receivedEvent.getData().getType());

        verify(testRepository, times(1)).save(any());
    }

    @org.junit.jupiter.api.Test
    void shouldThrowNoSuchTestByIdExceptionWhenChangingTitleOfNonExistingTest() {
        // given
        var correlationId = UUID.randomUUID().toString();
        var testUpdateRequest = new TestUpdateRequest(null, "newTitle", null, null, null, null);

        when(testRepository.findById(1L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchTestByIdException.class, () -> testService.updateTest(1L, testUpdateRequest, correlationId));
    }

    @org.junit.jupiter.api.Test
    void shouldThrowNoSuchTestTypeByNameExceptionWhenChangingTypeWithNonExistingType() {
        // given
        var correlationId = UUID.randomUUID().toString();
        Test testToUpdate = getNewTestWithAllFields();
        var testUpdateRequest = new TestUpdateRequest(null, null, null, null, null, TestTypeName.INTEGRATION);

        when(testRepository.findById(1L)).thenReturn(Optional.of(testToUpdate));
        when(testTypeRepository.findByName(TestTypeName.INTEGRATION)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchTestTypeByNameException.class, () -> testService.updateTest(1L, testUpdateRequest, correlationId));
    }

    @org.junit.jupiter.api.Test
    void shouldDeleteAndReturnVoidWhenDeletingExistingTest() {
        // given
        Test testToDelete = getNewTestWithAllFields();
        final var receivedEventWrapper = new TestDeletedEvent[1];

        when(
                testProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (TestDeletedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        when(testRepository.findById(1L)).thenReturn(Optional.of(testToDelete));

        // when
        testService.deleteTestById(1L, UUID.randomUUID().toString());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(testToDelete.getId(), receivedEvent.getTestId());

        // then
        verify(testRepository, times(1)).delete(testToDelete);
    }

    @org.junit.jupiter.api.Test
    void shouldThrowNoSuchTestByIdExceptionWhenDeletingNonExistingTest() {
        // given
        var correlationId = UUID.randomUUID().toString();
        when(testRepository.findById(1L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchTestByIdException.class, () -> testService.deleteTestById(1L, correlationId));
    }

    @org.junit.jupiter.api.Test
    void shouldReturnPagedModelWhenGettingAllTests() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        TestType type = new TestType(1L, TestTypeName.UNIT);
        var testToFind = new Test(1L, 1L, 1L, 1L, "title", "description", "instructions", "githubUrl", type, timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);
        Specification<Test> specification = Specification.where(null);

        when(testRepository.findAll(specification, pageable)).thenReturn(new PageImpl<>(List.of(testToFind), pageable, 1));

        var testResponsePage = testService.getAllTests(pageable, null, null, null, null);
        var testResponse = testResponsePage.getContent().iterator().next();

        //then
        assertNotNull(testResponsePage);
        assertNotNull(testResponsePage.getMetadata());
        assertEquals(1, testResponsePage.getMetadata().getTotalElements());
        assertEquals(1, testResponsePage.getContent().size());

        assertNotNull(testResponse);
        assertEquals(testToFind.getId(), testResponse.getId());
        assertEquals(testToFind.getProjectId(), testResponse.getProjectId());
        assertEquals(testToFind.getTestCaseId(), testResponse.getTestCaseId());
        assertEquals(testToFind.getAuthorId(), testResponse.getAuthorId());
        assertEquals(testToFind.getTitle(), testResponse.getTitle());
        assertEquals(testToFind.getDescription(), testResponse.getDescription());
        assertEquals(testToFind.getInstructions(), testResponse.getInstructions());
        assertEquals(testToFind.getGithubUrl(), testResponse.getGithubUrl());
        assertEquals(testToFind.getType().getName(), testResponse.getType());
        assertEquals(testToFind.getCreatedAt(), testResponse.getCreatedAt());
        assertEquals(testToFind.getUpdatedAt(), testResponse.getUpdatedAt());
        assertTrue(testResponse.hasLinks());
    }

    @org.junit.jupiter.api.Test
    void shouldReturnPagedModelWhenGettingTestsByProjectId() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        Long projectId = 1L;

        TestType type = new TestType(1L, TestTypeName.UNIT);
        var testToFind = new Test(1L, projectId, 1L, 1L, "title", "description", "instructions", "githubUrl", type, timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);

        when(testRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(testToFind), pageable, 1));

        var testResponsePage = testService.getAllTests(pageable, projectId, null, null, null);
        var testResponse = testResponsePage.getContent().iterator().next();

        //then
        assertNotNull(testResponsePage);
        assertNotNull(testResponsePage.getMetadata());
        assertEquals(1, testResponsePage.getMetadata().getTotalElements());
        assertEquals(1, testResponsePage.getContent().size());

        assertNotNull(testResponse);
        assertEquals(testToFind.getId(), testResponse.getId());
        assertEquals(projectId, testResponse.getProjectId());
        assertEquals(testToFind.getTestCaseId(), testResponse.getTestCaseId());
        assertEquals(testToFind.getAuthorId(), testResponse.getAuthorId());
        assertEquals(testToFind.getTitle(), testResponse.getTitle());
        assertEquals(testToFind.getDescription(), testResponse.getDescription());
        assertEquals(testToFind.getInstructions(), testResponse.getInstructions());
        assertEquals(testToFind.getGithubUrl(), testResponse.getGithubUrl());
        assertEquals(testToFind.getType().getName(), testResponse.getType());
        assertEquals(testToFind.getCreatedAt(), testResponse.getCreatedAt());
        assertEquals(testToFind.getUpdatedAt(), testResponse.getUpdatedAt());
        assertTrue(testResponse.hasLinks());
    }

    @org.junit.jupiter.api.Test
    void shouldReturnPagedModelWhenGettingTestsByTestAuthorId() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        Long authorId = 1L;

        TestType type = new TestType(1L, TestTypeName.UNIT);
        var testToFind = new Test(1L, 1L, 1L, authorId, "title", "description", "instructions", "githubUrl", type, timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);

        when(testRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(testToFind), pageable, 1));

        var testResponsePage = testService.getAllTests(pageable, null, authorId, null, null);
        var testResponse = testResponsePage.getContent().iterator().next();

        //then
        assertNotNull(testResponsePage);
        assertNotNull(testResponsePage.getMetadata());
        assertEquals(1, testResponsePage.getMetadata().getTotalElements());
        assertEquals(1, testResponsePage.getContent().size());

        assertNotNull(testResponse);
        assertEquals(testToFind.getId(), testResponse.getId());
        assertEquals(testToFind.getProjectId(), testResponse.getProjectId());
        assertEquals(testToFind.getTestCaseId(), testResponse.getTestCaseId());
        assertEquals(authorId, testResponse.getAuthorId());
        assertEquals(testToFind.getTitle(), testResponse.getTitle());
        assertEquals(testToFind.getDescription(), testResponse.getDescription());
        assertEquals(testToFind.getInstructions(), testResponse.getInstructions());
        assertEquals(testToFind.getGithubUrl(), testResponse.getGithubUrl());
        assertEquals(testToFind.getType().getName(), testResponse.getType());
        assertEquals(testToFind.getCreatedAt(), testResponse.getCreatedAt());
        assertEquals(testToFind.getUpdatedAt(), testResponse.getUpdatedAt());
        assertTrue(testResponse.hasLinks());
    }

    @org.junit.jupiter.api.Test
    void shouldReturnPagedModelWhenGettingTestsByTestCaseId() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        Long testCaseId = 1L;

        TestType type = new TestType(1L, TestTypeName.UNIT);
        var testToFind = new Test(1L, 1L, testCaseId, 1L, "title", "description", "instructions", "githubUrl", type, timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);

        when(testRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(testToFind), pageable, 1));

        var testResponsePage = testService.getAllTests(pageable, null, null, testCaseId, null);
        var testResponse = testResponsePage.getContent().iterator().next();

        //then
        assertNotNull(testResponsePage);
        assertNotNull(testResponsePage.getMetadata());
        assertEquals(1, testResponsePage.getMetadata().getTotalElements());
        assertEquals(1, testResponsePage.getContent().size());

        assertNotNull(testResponse);
        assertEquals(testToFind.getId(), testResponse.getId());
        assertEquals(testToFind.getProjectId(), testResponse.getProjectId());
        assertEquals(testCaseId, testResponse.getTestCaseId());
        assertEquals(testToFind.getAuthorId(), testResponse.getAuthorId());
        assertEquals(testToFind.getTitle(), testResponse.getTitle());
        assertEquals(testToFind.getDescription(), testResponse.getDescription());
        assertEquals(testToFind.getInstructions(), testResponse.getInstructions());
        assertEquals(testToFind.getGithubUrl(), testResponse.getGithubUrl());
        assertEquals(testToFind.getType().getName(), testResponse.getType());
        assertEquals(testToFind.getCreatedAt(), testResponse.getCreatedAt());
        assertEquals(testToFind.getUpdatedAt(), testResponse.getUpdatedAt());
        assertTrue(testResponse.hasLinks());
    }

    @org.junit.jupiter.api.Test
    void shouldReturnPagedModelWhenGettingTestsByTestTypeName() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        TestTypeName typeName = TestTypeName.UNIT;

        TestType type = new TestType(1L, TestTypeName.UNIT);
        var testToFind = new Test(1L, 1L, 1L, 1L, "title", "description", "instructions", "githubUrl", type, timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);

        when(testRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(testToFind), pageable, 1));
        when(testTypeRepository.findByName(typeName)).thenReturn(Optional.of(type));

        var testResponsePage = testService.getAllTests(pageable, null, null, null, typeName);
        var testResponse = testResponsePage.getContent().iterator().next();

        //then
        assertNotNull(testResponsePage);
        assertNotNull(testResponsePage.getMetadata());
        assertEquals(1, testResponsePage.getMetadata().getTotalElements());
        assertEquals(1, testResponsePage.getContent().size());

        assertNotNull(testResponse);
        assertEquals(testToFind.getId(), testResponse.getId());
        assertEquals(testToFind.getProjectId(), testResponse.getProjectId());
        assertEquals(testToFind.getTestCaseId(), testResponse.getTestCaseId());
        assertEquals(testToFind.getAuthorId(), testResponse.getAuthorId());
        assertEquals(testToFind.getTitle(), testResponse.getTitle());
        assertEquals(testToFind.getDescription(), testResponse.getDescription());
        assertEquals(testToFind.getInstructions(), testResponse.getInstructions());
        assertEquals(testToFind.getGithubUrl(), testResponse.getGithubUrl());
        assertEquals(typeName, testResponse.getType());
        assertEquals(testToFind.getCreatedAt(), testResponse.getCreatedAt());
        assertEquals(testToFind.getUpdatedAt(), testResponse.getUpdatedAt());
        assertTrue(testResponse.hasLinks());
    }

    @org.junit.jupiter.api.Test
    void shouldThrowNoSuchTestTypeByNameExceptionWhenGettingTestsByNonExistingTestTypeName() {
        // given
        TestTypeName typeName = TestTypeName.INTEGRATION;
        Pageable pageable = PageRequest.of(0, 2);

        when(testTypeRepository.findByName(typeName)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchTestTypeByNameException.class, () -> testService.getAllTests(pageable, null, null, null, typeName));
    }

    private Answer1<Test, Test> getFakeSave(long id) {
        return test -> {
            test.setId(id);
            return test;
        };
    }

    private Answer1<CompletableFuture<SendResult<String, TestEvent>>, TestEvent> getFakeSendResult() {
        return event -> CompletableFuture.completedFuture(
                new SendResult<>(new ProducerRecord<>("tests", event),
                        new RecordMetadata(new TopicPartition("tests", 0), 0L, 0, 0L, 0, 0)));
    }

    private Test getNewTestWithAllFields() {
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        TestType type = new TestType(1L, TestTypeName.UNIT);

        return new Test(1L, 1L, 1L, 1L, "title", "description", "instructions", "githubUrl", type, timeOfCreation, timeOfModification);
    }
}
