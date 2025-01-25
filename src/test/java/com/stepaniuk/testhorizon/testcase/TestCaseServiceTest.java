package com.stepaniuk.testhorizon.testcase;

import com.stepaniuk.testhorizon.event.testcase.TestCaseCreatedEvent;
import com.stepaniuk.testhorizon.event.testcase.TestCaseDeletedEvent;
import com.stepaniuk.testhorizon.event.testcase.TestCaseEvent;
import com.stepaniuk.testhorizon.event.testcase.TestCaseUpdatedEvent;
import com.stepaniuk.testhorizon.payload.testcase.TestCaseCreateRequest;
import com.stepaniuk.testhorizon.payload.testcase.TestCaseUpdateRequest;
import com.stepaniuk.testhorizon.shared.PageMapperImpl;
import com.stepaniuk.testhorizon.testcase.exceptions.NoSuchTestCaseByIdException;
import com.stepaniuk.testhorizon.testcase.exceptions.NoSuchTestCasePriorityByNameException;
import com.stepaniuk.testhorizon.testcase.priority.TestCasePriority;
import com.stepaniuk.testhorizon.testcase.priority.TestCasePriorityName;
import com.stepaniuk.testhorizon.testcase.priority.TestCasePriorityRepository;
import com.stepaniuk.testhorizon.testspecific.ServiceLevelUnitTest;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.stubbing.Answer1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
@ContextConfiguration(classes = {TestCaseService.class, TestCaseMapperImpl.class, PageMapperImpl.class})
class TestCaseServiceTest {

    @Autowired
    private TestCaseService testCaseService;

    @MockitoBean
    private TestCaseProducer testCaseProducer;

    @MockitoBean
    private TestCaseRepository testCaseRepository;

    @MockitoBean
    private TestCasePriorityRepository testCasePriorityRepository;

    @Test
    void shouldReturnTestCaseResponseWhenCreatingTestCase() {
        // given
        var testCaseCreateRequest = new TestCaseCreateRequest(1L, "title", "description", "preconditions", "inputData", List.of("step1", "step2"), TestCasePriorityName.LOW);
        var testCasePriority = new TestCasePriority(1L, TestCasePriorityName.LOW);

        when(testCasePriorityRepository.findByName(TestCasePriorityName.LOW)).thenReturn(Optional.of(testCasePriority));
        when(testCaseRepository.save(any())).thenAnswer(answer(getFakeSave(1L)));
        final var receivedEventWrapper = new TestCaseCreatedEvent[1];
        when(
                testCaseProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (TestCaseCreatedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        // when
        var testCaseResponse = testCaseService.createTestCase(testCaseCreateRequest, 1L, UUID.randomUUID().toString());

        // then
        assertNotNull(testCaseResponse);
        assertEquals(testCaseCreateRequest.getProjectId(), testCaseResponse.getProjectId());
        assertEquals(1L, testCaseResponse.getAuthorId());
        assertEquals(testCaseCreateRequest.getTitle(), testCaseResponse.getTitle());
        assertEquals(testCaseCreateRequest.getDescription(), testCaseResponse.getDescription());
        assertEquals(testCaseCreateRequest.getPreconditions(), testCaseResponse.getPreconditions());
        assertEquals(testCaseCreateRequest.getInputData(), testCaseResponse.getInputData());
        assertEquals(testCaseCreateRequest.getSteps(), testCaseResponse.getSteps());
        assertEquals(testCaseCreateRequest.getPriority(), testCaseResponse.getPriority());
        assertTrue(testCaseResponse.hasLinks());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(1L, receivedEvent.getTestCaseId());
        assertEquals(testCaseResponse.getProjectId(), receivedEvent.getProjectId());
        assertEquals(testCaseResponse.getAuthorId(), receivedEvent.getAuthorId());

        verify(testCaseRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowNoSuchTestCasePriorityByNameExceptionWhenCreatingTestCaseWithNonExistingPriority() {
        // given
        var testCaseCreateRequest = new TestCaseCreateRequest(1L, "title", "description", "preconditions", "inputData", List.of("step1", "step2"), TestCasePriorityName.HIGH);
        var correlationId = UUID.randomUUID().toString();

        when(testCasePriorityRepository.findByName(TestCasePriorityName.HIGH)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchTestCasePriorityByNameException.class, () -> testCaseService.createTestCase(testCaseCreateRequest, 1L, correlationId));
    }

    @Test
    void shouldReturnTestCaseResponseWhenGetByExistingId() {
        // given
        TestCase testCaseToFind = getNewTestCaseWithAllFields();

        when(testCaseRepository.findById(1L)).thenReturn(Optional.of(testCaseToFind));

        // when
        var testCaseResponse = testCaseService.getTestCaseById(1L);

        // then
        assertNotNull(testCaseResponse);
        assertEquals(testCaseToFind.getId(), testCaseResponse.getId());
        assertEquals(testCaseToFind.getProjectId(), testCaseResponse.getProjectId());
        assertEquals(testCaseToFind.getAuthorId(), testCaseResponse.getAuthorId());
        assertEquals(testCaseToFind.getTitle(), testCaseResponse.getTitle());
        assertEquals(testCaseToFind.getDescription(), testCaseResponse.getDescription());
        assertEquals(testCaseToFind.getPreconditions(), testCaseResponse.getPreconditions());
        assertEquals(testCaseToFind.getInputData(), testCaseResponse.getInputData());
        assertEquals(testCaseToFind.getSteps(), testCaseResponse.getSteps());
        assertEquals(testCaseToFind.getPriority().getName(), testCaseResponse.getPriority());
        assertEquals(testCaseToFind.getCreatedAt(), testCaseResponse.getCreatedAt());
        assertEquals(testCaseToFind.getUpdatedAt(), testCaseResponse.getUpdatedAt());
        assertTrue(testCaseResponse.hasLinks());
    }

    @Test
    void shouldThrowNoSuchTestCaseByIdExceptionWhenGetByNonExistingId() {
        // given
        when(testCaseRepository.findById(100L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchTestCaseByIdException.class, () -> testCaseService.getTestCaseById(100L));
    }

    @Test
    void shouldUpdateAndReturnTestCaseResponseWhenChangingTitle() {
        // given
        TestCase testCaseToUpdate = getNewTestCaseWithAllFields();
        var testCaseUpdateRequest = new TestCaseUpdateRequest("newTitle", null, null, null, null, null);

        when(testCaseRepository.findById(1L)).thenReturn(Optional.of(testCaseToUpdate));
        when(testCaseRepository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
        final var receivedEventWrapper = new TestCaseUpdatedEvent[1];
        when(
                testCaseProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (TestCaseUpdatedEvent) event))).thenAnswer(
                answer(getFakeSendResult())
        );

        // when
        var testCaseResponse = testCaseService.updateTestCase(1L, testCaseUpdateRequest, UUID.randomUUID().toString());

        // then
        assertNotNull(testCaseResponse);
        assertEquals(testCaseToUpdate.getId(), testCaseResponse.getId());
        assertEquals(testCaseToUpdate.getProjectId(), testCaseResponse.getProjectId());
        assertEquals(testCaseToUpdate.getAuthorId(), testCaseResponse.getAuthorId());
        assertEquals(testCaseUpdateRequest.getTitle(), testCaseResponse.getTitle());
        assertEquals(testCaseToUpdate.getDescription(), testCaseResponse.getDescription());
        assertEquals(testCaseToUpdate.getPreconditions(), testCaseResponse.getPreconditions());
        assertEquals(testCaseToUpdate.getInputData(), testCaseResponse.getInputData());
        assertEquals(testCaseToUpdate.getSteps(), testCaseResponse.getSteps());
        assertEquals(testCaseToUpdate.getPriority().getName(), testCaseResponse.getPriority());
        assertEquals(testCaseToUpdate.getCreatedAt(), testCaseResponse.getCreatedAt());
        assertEquals(testCaseToUpdate.getUpdatedAt(), testCaseResponse.getUpdatedAt());
        assertTrue(testCaseResponse.hasLinks());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(testCaseResponse.getId(), receivedEvent.getTestCaseId());
        assertEquals(testCaseResponse.getTitle(), receivedEvent.getData().getTitle());
        assertNull(receivedEvent.getData().getDescription());

        verify(testCaseRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowNoSuchTestCaseByIdExceptionWhenChangingTitleOfNonExistingTestCase() {
        // given
        var correlationId = UUID.randomUUID().toString();
        var testCaseUpdateRequest = new TestCaseUpdateRequest("newTitle", null, null, null, null, null);

        when(testCaseRepository.findById(1L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchTestCaseByIdException.class, () -> testCaseService.updateTestCase(1L, testCaseUpdateRequest, correlationId));
    }

    @Test
    void shouldThrowNoSuchTestCasePriorityByNameExceptionWhenUpdatingTestCaseWithNonExistingPriority() {
        // given
        var correlationId = UUID.randomUUID().toString();
        var testCaseToUpdate = getNewTestCaseWithAllFields();
        var testCaseUpdateRequest = new TestCaseUpdateRequest(null, null, null, null, null, TestCasePriorityName.HIGH);

        when(testCaseRepository.findById(1L)).thenReturn(Optional.of(testCaseToUpdate));
        when(testCasePriorityRepository.findByName(TestCasePriorityName.HIGH)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchTestCasePriorityByNameException.class, () -> testCaseService.updateTestCase(1L, testCaseUpdateRequest, correlationId));
    }

    @Test
    void shouldDeleteAndReturnVoidWhenDeletingExistingTestCase() {
        // given
        TestCase testCaseToDelete = getNewTestCaseWithAllFields();

        final var receivedEventWrapper = new TestCaseDeletedEvent[1];
        when(
                testCaseProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (TestCaseDeletedEvent) event))).thenAnswer(
                answer(getFakeSendResult())
        );

        when(testCaseRepository.findById(1L)).thenReturn(Optional.of(testCaseToDelete));

        // when
        testCaseService.deleteTestCaseById(1L, UUID.randomUUID().toString());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(testCaseToDelete.getId(), receivedEvent.getTestCaseId());

        // then
        verify(testCaseRepository, times(1)).delete(testCaseToDelete);
    }

    @Test
    void shouldThrowNoSuchTestCaseByIdExceptionWhenDeletingNonExistingTestCase() {
        // given
        var correlationId = UUID.randomUUID().toString();

        when(testCaseRepository.findById(100L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchTestCaseByIdException.class, () -> testCaseService.deleteTestCaseById(100L, correlationId));
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllTestCases() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        var testCaseToFind = new TestCase(1L, 1L, 1L, "title", "description", "preconditions", "inputData", List.of("step1", "step2"),
                new TestCasePriority(1L, TestCasePriorityName.LOW), timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);
        Specification<TestCase> specification = Specification.where(null);

        when(testCaseRepository.findAll(specification, pageable)).thenReturn(
                new PageImpl<>(List.of(testCaseToFind), pageable, 1));
        // when
        var testCaseResponses = testCaseService.getAllTestCases(pageable, null, null, null);
        var testCaseResponse = testCaseResponses.getContent().iterator().next();

        //then
        assertNotNull(testCaseResponses);
        assertNotNull(testCaseResponses.getMetadata());
        assertEquals(1, testCaseResponses.getMetadata().getTotalElements());
        assertEquals(1, testCaseResponses.getContent().size());

        assertEquals(testCaseToFind.getId(), testCaseResponse.getId());
        assertEquals(testCaseToFind.getProjectId(), testCaseResponse.getProjectId());
        assertEquals(testCaseToFind.getAuthorId(), testCaseResponse.getAuthorId());
        assertEquals(testCaseToFind.getTitle(), testCaseResponse.getTitle());
        assertEquals(testCaseToFind.getDescription(), testCaseResponse.getDescription());
        assertEquals(testCaseToFind.getPreconditions(), testCaseResponse.getPreconditions());
        assertEquals(testCaseToFind.getInputData(), testCaseResponse.getInputData());
        assertEquals(testCaseToFind.getSteps(), testCaseResponse.getSteps());
        assertEquals(testCaseToFind.getPriority().getName(), testCaseResponse.getPriority());
        assertEquals(testCaseToFind.getCreatedAt(), testCaseResponse.getCreatedAt());
        assertEquals(testCaseToFind.getUpdatedAt(), testCaseResponse.getUpdatedAt());
        assertTrue(testCaseResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllTestCasesAndProjectIdNotNull() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        Long projectId = 1L;

        var testCaseToFind = new TestCase(1L, projectId, 1L, "title", "description", "preconditions", "inputData", List.of("step1", "step2"),
                new TestCasePriority(1L, TestCasePriorityName.LOW), timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 1);

        when(testCaseRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(
                new PageImpl<>(List.of(testCaseToFind), pageable, 1));
        // when
        var testCaseResponses = testCaseService.getAllTestCases(pageable, projectId, null, null);
        var testCaseResponse = testCaseResponses.getContent().iterator().next();

        //then
        assertNotNull(testCaseResponses);
        assertNotNull(testCaseResponses.getMetadata());
        assertEquals(1, testCaseResponses.getMetadata().getTotalElements());
        assertEquals(1, testCaseResponses.getContent().size());

        assertEquals(testCaseToFind.getId(), testCaseResponse.getId());
        assertEquals(projectId, testCaseResponse.getProjectId());
        assertEquals(testCaseToFind.getAuthorId(), testCaseResponse.getAuthorId());
        assertEquals(testCaseToFind.getTitle(), testCaseResponse.getTitle());
        assertEquals(testCaseToFind.getDescription(), testCaseResponse.getDescription());
        assertEquals(testCaseToFind.getPreconditions(), testCaseResponse.getPreconditions());
        assertEquals(testCaseToFind.getInputData(), testCaseResponse.getInputData());
        assertEquals(testCaseToFind.getSteps(), testCaseResponse.getSteps());
        assertEquals(testCaseToFind.getPriority().getName(), testCaseResponse.getPriority());
        assertEquals(testCaseToFind.getCreatedAt(), testCaseResponse.getCreatedAt());
        assertEquals(testCaseToFind.getUpdatedAt(), testCaseResponse.getUpdatedAt());
        assertTrue(testCaseResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllTestCasesAndAuthorIdNotNull() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        Long authorId = 1L;

        var testCaseToFind = new TestCase(1L, 1L, authorId, "title", "description", "preconditions", "inputData", List.of("step1", "step2"),
                new TestCasePriority(1L, TestCasePriorityName.LOW), timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 1);

        when(testCaseRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(
                new PageImpl<>(List.of(testCaseToFind), pageable, 1));
        // when
        var testCaseResponses = testCaseService.getAllTestCases(pageable, null, authorId, null);
        var testCaseResponse = testCaseResponses.getContent().iterator().next();

        //then
        assertNotNull(testCaseResponses);
        assertNotNull(testCaseResponses.getMetadata());
        assertEquals(1, testCaseResponses.getMetadata().getTotalElements());
        assertEquals(1, testCaseResponses.getContent().size());

        assertEquals(testCaseToFind.getId(), testCaseResponse.getId());
        assertEquals(testCaseToFind.getProjectId(), testCaseResponse.getProjectId());
        assertEquals(authorId, testCaseResponse.getAuthorId());
        assertEquals(testCaseToFind.getTitle(), testCaseResponse.getTitle());
        assertEquals(testCaseToFind.getDescription(), testCaseResponse.getDescription());
        assertEquals(testCaseToFind.getPreconditions(), testCaseResponse.getPreconditions());
        assertEquals(testCaseToFind.getInputData(), testCaseResponse.getInputData());
        assertEquals(testCaseToFind.getSteps(), testCaseResponse.getSteps());
        assertEquals(testCaseToFind.getPriority().getName(), testCaseResponse.getPriority());
        assertEquals(testCaseToFind.getCreatedAt(), testCaseResponse.getCreatedAt());
        assertEquals(testCaseToFind.getUpdatedAt(), testCaseResponse.getUpdatedAt());
        assertTrue(testCaseResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllTestCasesAndPriorityNameNotNull() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        TestCasePriorityName priorityName = TestCasePriorityName.LOW;

        var testCaseToFind = new TestCase(1L, 1L, 1L, "title", "description", "preconditions", "inputData", List.of("step1", "step2"),
                new TestCasePriority(1L, priorityName), timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 1);

        when(testCasePriorityRepository.findByName(priorityName)).thenReturn(Optional.of(testCaseToFind.getPriority()));
        when(testCaseRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(
                new PageImpl<>(List.of(testCaseToFind), pageable, 1));
        // when
        var testCaseResponses = testCaseService.getAllTestCases(pageable, null, null, priorityName);
        var testCaseResponse = testCaseResponses.getContent().iterator().next();

        //then
        assertNotNull(testCaseResponses);
        assertNotNull(testCaseResponses.getMetadata());
        assertEquals(1, testCaseResponses.getMetadata().getTotalElements());
        assertEquals(1, testCaseResponses.getContent().size());

        assertEquals(testCaseToFind.getId(), testCaseResponse.getId());
        assertEquals(testCaseToFind.getProjectId(), testCaseResponse.getProjectId());
        assertEquals(testCaseToFind.getAuthorId(), testCaseResponse.getAuthorId());
        assertEquals(testCaseToFind.getTitle(), testCaseResponse.getTitle());
        assertEquals(testCaseToFind.getDescription(), testCaseResponse.getDescription());
        assertEquals(testCaseToFind.getPreconditions(), testCaseResponse.getPreconditions());
        assertEquals(testCaseToFind.getInputData(), testCaseResponse.getInputData());
        assertEquals(testCaseToFind.getSteps(), testCaseResponse.getSteps());
        assertEquals(testCaseToFind.getPriority().getName(), testCaseResponse.getPriority());
        assertEquals(testCaseToFind.getCreatedAt(), testCaseResponse.getCreatedAt());
        assertEquals(testCaseToFind.getUpdatedAt(), testCaseResponse.getUpdatedAt());
        assertTrue(testCaseResponse.hasLinks());
    }

    private Answer1<TestCase, TestCase> getFakeSave(long id) {
        return testCase -> {
            testCase.setId(id);
            return testCase;
        };
    }

    private Answer1<CompletableFuture<SendResult<String, TestCaseEvent>>, TestCaseEvent> getFakeSendResult() {
        return event -> CompletableFuture.completedFuture(
                new SendResult<>(new ProducerRecord<>("test-cases", event),
                        new RecordMetadata(new TopicPartition("test-cases", 0), 0L, 0, 0L, 0, 0)));
    }

    private TestCase getNewTestCaseWithAllFields() {
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        TestCasePriority priority = new TestCasePriority(1L, TestCasePriorityName.LOW);

        return new TestCase(1L, 1L, 1L, "title", "description", "preconditions", "inputData", List.of("step1", "step2"),
                priority, timeOfCreation, timeOfModification
        );
    }
}
