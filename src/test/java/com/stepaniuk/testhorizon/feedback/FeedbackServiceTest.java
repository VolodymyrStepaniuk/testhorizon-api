package com.stepaniuk.testhorizon.feedback;

import com.stepaniuk.testhorizon.feedback.exceptions.NoSuchFeedbackFoundByIdException;
import com.stepaniuk.testhorizon.payload.feedback.FeedbackCreateRequest;
import com.stepaniuk.testhorizon.payload.feedback.FeedbackUpdateRequest;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.shared.PageMapperImpl;
import com.stepaniuk.testhorizon.shared.UserInfoService;
import com.stepaniuk.testhorizon.testspecific.ServiceLevelUnitTest;
import com.stepaniuk.testhorizon.event.feedback.FeedbackCreatedEvent;
import com.stepaniuk.testhorizon.event.feedback.FeedbackDeletedEvent;
import com.stepaniuk.testhorizon.event.feedback.FeedbackEvent;
import com.stepaniuk.testhorizon.event.feedback.FeedbackUpdatedEvent;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ServiceLevelUnitTest
@ContextConfiguration(classes = {FeedbackService.class, FeedbackMapperImpl.class, PageMapperImpl.class})
class FeedbackServiceTest {

    @Autowired
    private FeedbackService feedbackService;

    @MockitoBean
    private FeedbackProducer feedbackProducer;

    @MockitoBean
    private FeedbackRepository feedbackRepository;

    @MockitoBean
    private UserInfoService userInfoService;


    @Test
    void shouldReturnFeedbackResponseWhenCreatingFeedback() {
        // given
        FeedbackCreateRequest feedbackCreateRequest = new FeedbackCreateRequest(
                5, "comment"
        );
        var userInfo = new UserInfo(1L, "John", "Doe");

        when(userInfoService.getUserInfo(1L)).thenReturn(userInfo);
        when(feedbackRepository.save(any())).thenAnswer(answer(getFakeSave(1L)));

        final var receivedEventWrapper = new FeedbackCreatedEvent[1];
        when(
                feedbackProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (FeedbackCreatedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        // when
        var feedbackResponse = feedbackService.createFeedback(feedbackCreateRequest, 1L, UUID.randomUUID().toString());

        // then
        assertNotNull(feedbackResponse);
        assertNotNull(feedbackResponse.getOwner());
        assertEquals(1L, feedbackResponse.getOwner().getId());
        assertEquals(userInfo.getFirstName(), feedbackResponse.getOwner().getFirstName());
        assertEquals(userInfo.getLastName(), feedbackResponse.getOwner().getLastName());
        assertEquals(feedbackCreateRequest.getComment(), feedbackResponse.getComment());
        assertEquals(feedbackCreateRequest.getRating(), feedbackResponse.getRating());
        assertTrue(feedbackResponse.hasLinks());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(1L, receivedEvent.getFeedbackId());

        verify(feedbackRepository, times(1)).save(any());
    }

    @Test
    void shouldReturnFeedbackResponseWhenGettingFeedbackById() {
        // given
        Feedback feedback = getNewFeedbackWithAllFields();
        var userInfo = new UserInfo(1L, "John", "Doe");
        var authInfo = new AuthInfo(1L, List.of());

        when(userInfoService.getUserInfo(1L)).thenReturn(userInfo);
        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));

        // when
        var feedbackResponse = feedbackService.getFeedback(1L, authInfo);

        // then
        assertNotNull(feedbackResponse);
        assertEquals(feedback.getId(), feedbackResponse.getId());
        assertNotNull(feedbackResponse.getOwner());
        assertEquals(feedback.getOwnerId(), feedbackResponse.getOwner().getId());
        assertEquals(userInfo.getFirstName(), feedbackResponse.getOwner().getFirstName());
        assertEquals(userInfo.getLastName(), feedbackResponse.getOwner().getLastName());
        assertEquals(feedback.getComment(), feedbackResponse.getComment());
        assertEquals(feedback.getRating(), feedbackResponse.getRating());
        assertEquals(feedback.getCreatedAt(), feedbackResponse.getCreatedAt());
        assertEquals(feedback.getUpdatedAt(), feedbackResponse.getUpdatedAt());
        assertTrue(feedbackResponse.hasLinks());
    }

    @Test
    void shouldThrowNoSuchFeedbackFoundByIdExceptionWhenGettingFeedbackById() {
        // given
        var authInfo = new AuthInfo(1L, List.of());
        when(feedbackRepository.findById(1L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchFeedbackFoundByIdException.class, () -> feedbackService.getFeedback(1L, authInfo));
    }

    @Test
    void shouldUpdateAndReturnFeedbackResponseWhenChangingFeedbackComment() {
        // given
        Feedback feedback = getNewFeedbackWithAllFields();
        FeedbackUpdateRequest feedbackUpdateRequest = new FeedbackUpdateRequest(null,"new comment");
        var userInfo = new UserInfo(1L, "John", "Doe");

        when(userInfoService.getUserInfo(1L)).thenReturn(userInfo);
        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));
        when(feedbackRepository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());

        final var receivedEventWrapper = new FeedbackUpdatedEvent[1];
        when(
                feedbackProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (FeedbackUpdatedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        // when
        var feedbackResponse = feedbackService.updateFeedback(1L, feedbackUpdateRequest, UUID.randomUUID().toString());

        // then
        assertNotNull(feedbackResponse);
        assertEquals(feedback.getId(), feedbackResponse.getId());
        assertNotNull(feedbackResponse.getOwner());
        assertEquals(feedback.getOwnerId(), feedbackResponse.getOwner().getId());
        assertEquals(userInfo.getFirstName(), feedbackResponse.getOwner().getFirstName());
        assertEquals(userInfo.getLastName(), feedbackResponse.getOwner().getLastName());
        assertEquals(feedbackUpdateRequest.getComment(), feedbackResponse.getComment());
        assertEquals(feedback.getRating(), feedbackResponse.getRating());
        assertEquals(feedback.getCreatedAt(), feedbackResponse.getCreatedAt());
        assertEquals(feedback.getUpdatedAt(), feedbackResponse.getUpdatedAt());
        assertTrue(feedbackResponse.hasLinks());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(feedbackResponse.getId(), receivedEvent.getFeedbackId());
        assertEquals(feedbackResponse.getComment(), receivedEvent.getData().getComment());
        assertNull(receivedEvent.getData().getRating());

        verify(feedbackRepository, times(1)).save(any());
    }

    @Test
    void shouldUpdateAndReturnFeedbackResponseWhenChangingFeedbackRating() {
        // given
        Feedback feedback = getNewFeedbackWithAllFields();
        FeedbackUpdateRequest feedbackUpdateRequest = new FeedbackUpdateRequest(4, null);
        var userInfo = new UserInfo(1L, "John", "Doe");

        when(userInfoService.getUserInfo(1L)).thenReturn(userInfo);
        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));
        when(feedbackRepository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());

        final var receivedEventWrapper = new FeedbackUpdatedEvent[1];
        when(
                feedbackProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (FeedbackUpdatedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        // when
        var feedbackResponse = feedbackService.updateFeedback(1L, feedbackUpdateRequest, UUID.randomUUID().toString());

        // then
        assertNotNull(feedbackResponse);
        assertEquals(feedback.getId(), feedbackResponse.getId());
        assertNotNull(feedbackResponse.getOwner());
        assertEquals(feedback.getOwnerId(), feedbackResponse.getOwner().getId());
        assertEquals(userInfo.getFirstName(), feedbackResponse.getOwner().getFirstName());
        assertEquals(userInfo.getLastName(), feedbackResponse.getOwner().getLastName());
        assertEquals(feedback.getComment(), feedbackResponse.getComment());
        assertEquals(feedbackUpdateRequest.getRating(), feedbackResponse.getRating());
        assertTrue(feedbackResponse.hasLinks());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(feedbackResponse.getId(), receivedEvent.getFeedbackId());
        assertNull(receivedEvent.getData().getComment());
        assertEquals(feedbackUpdateRequest.getRating(), receivedEvent.getData().getRating());

        verify(feedbackRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowNoSuchFeedbackFoundByIdExceptionWhenUpdatingFeedback() {
        // given
        var correlationId = UUID.randomUUID().toString();
        FeedbackUpdateRequest feedbackUpdateRequest = new FeedbackUpdateRequest(5,"new comment");

        when(feedbackRepository.findById(1L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchFeedbackFoundByIdException.class, () -> feedbackService.updateFeedback(1L, feedbackUpdateRequest, correlationId));
    }

    @Test
    void shouldDeleteAndReturnVoidWhenDeletingExistingFeedback() {
        // given
        Feedback feedback = getNewFeedbackWithAllFields();
        var correlationId = UUID.randomUUID().toString();

        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));
        final var receivedEventWrapper = new FeedbackDeletedEvent[1];
        when(
                feedbackProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (FeedbackDeletedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        // when
        feedbackService.deleteFeedback(1L, correlationId);

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(feedback.getId(), receivedEvent.getFeedbackId());

        // then
        verify(feedbackRepository, times(1)).delete(feedback);
    }

    @Test
    void shouldThrowNoSuchFeedbackFoundByIdExceptionWhenDeletingFeedback() {
        // given
        var correlationId = UUID.randomUUID().toString();

        when(feedbackRepository.findById(1L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchFeedbackFoundByIdException.class, () -> feedbackService.deleteFeedback(1L, correlationId));
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllFeedback() {
        // given
        var feedbackToFind = getNewFeedbackWithAllFields();

        var pageable = PageRequest.of(0, 2);
        Specification<Feedback> specification = Specification.where(null);
        var userInfo = new UserInfo(1L, "John", "Doe");

        when(userInfoService.getUserInfo(1L)).thenReturn(userInfo);
        when(feedbackRepository.findAll(specification, pageable)).thenReturn(new PageImpl<>(List.of(feedbackToFind), pageable, 1));

        // when
        var feedbacks = feedbackService.getAllFeedbacks(pageable, null, null);
        var feedbackResponse = feedbacks.getContent().iterator().next();

        // then
        assertNotNull(feedbacks);
        assertNotNull(feedbacks.getMetadata());
        assertEquals(1, feedbacks.getMetadata().getTotalElements());
        assertEquals(1, feedbacks.getContent().size());

        assertNotNull(feedbackResponse);
        assertEquals(feedbackToFind.getId(), feedbackResponse.getId());
        assertNotNull(feedbackResponse.getOwner());
        assertEquals(feedbackToFind.getOwnerId(), feedbackResponse.getOwner().getId());
        assertEquals(userInfo.getFirstName(), feedbackResponse.getOwner().getFirstName());
        assertEquals(userInfo.getLastName(), feedbackResponse.getOwner().getLastName());
        assertEquals(feedbackToFind.getComment(), feedbackResponse.getComment());
        assertEquals(feedbackToFind.getRating(), feedbackResponse.getRating());
        assertEquals(feedbackToFind.getCreatedAt(), feedbackResponse.getCreatedAt());
        assertEquals(feedbackToFind.getUpdatedAt(), feedbackResponse.getUpdatedAt());
        assertTrue(feedbackResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllFeedbackByOwnerId() {
        // given
        var feedbackToFind = getNewFeedbackWithAllFields();
        Long ownerId = 1L;

        var pageable = PageRequest.of(0, 2);
        var userInfo = new UserInfo(1L, "John", "Doe");

        when(userInfoService.getUserInfo(1L)).thenReturn(userInfo);
        when(feedbackRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(feedbackToFind), pageable, 1));

        // when
        var feedbacks = feedbackService.getAllFeedbacks(pageable, ownerId, null);
        var feedbackResponse = feedbacks.getContent().iterator().next();

        // then
        assertNotNull(feedbacks);
        assertNotNull(feedbacks.getMetadata());
        assertEquals(1, feedbacks.getMetadata().getTotalElements());
        assertEquals(1, feedbacks.getContent().size());

        assertNotNull(feedbackResponse);
        assertEquals(feedbackToFind.getId(), feedbackResponse.getId());
        assertNotNull(feedbackResponse.getOwner());
        assertEquals(ownerId, feedbackResponse.getOwner().getId());
        assertEquals(userInfo.getFirstName(), feedbackResponse.getOwner().getFirstName());
        assertEquals(userInfo.getLastName(), feedbackResponse.getOwner().getLastName());
        assertEquals(feedbackToFind.getComment(), feedbackResponse.getComment());
        assertEquals(feedbackToFind.getRating(), feedbackResponse.getRating());
        assertEquals(feedbackToFind.getCreatedAt(), feedbackResponse.getCreatedAt());
        assertEquals(feedbackToFind.getUpdatedAt(), feedbackResponse.getUpdatedAt());
        assertTrue(feedbackResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllFeedbackByFeedbackIds() {
        // given
        var feedbackToFind = getNewFeedbackWithAllFields();
        List<Long> feedbackIds = List.of(1L);

        var pageable = PageRequest.of(0, 2);
        var userInfo = new UserInfo(1L, "John", "Doe");

        when(userInfoService.getUserInfo(1L)).thenReturn(userInfo);
        when(feedbackRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(feedbackToFind), pageable, 1));

        // when
        var feedbacks = feedbackService.getAllFeedbacks(pageable, null, feedbackIds);
        var feedbackResponse = feedbacks.getContent().iterator().next();

        // then
        assertNotNull(feedbacks);
        assertNotNull(feedbacks.getMetadata());
        assertEquals(1, feedbacks.getMetadata().getTotalElements());
        assertEquals(1, feedbacks.getContent().size());

        assertNotNull(feedbackResponse);
        assertEquals(feedbackToFind.getId(), feedbackResponse.getId());
        assertNotNull(feedbackResponse.getOwner());
        assertEquals(feedbackToFind.getOwnerId(), feedbackResponse.getOwner().getId());
        assertEquals(userInfo.getFirstName(), feedbackResponse.getOwner().getFirstName());
        assertEquals(userInfo.getLastName(), feedbackResponse.getOwner().getLastName());
        assertEquals(feedbackToFind.getComment(), feedbackResponse.getComment());
        assertEquals(feedbackToFind.getRating(), feedbackResponse.getRating());
        assertEquals(feedbackToFind.getCreatedAt(), feedbackResponse.getCreatedAt());
        assertEquals(feedbackToFind.getUpdatedAt(), feedbackResponse.getUpdatedAt());
        assertTrue(feedbackResponse.hasLinks());
    }

    private Answer1<Feedback, Feedback> getFakeSave(long id) {
        return feedback -> {
            feedback.setId(id);
            return feedback;
        };
    }

    private Answer1<CompletableFuture<SendResult<String, FeedbackEvent>>, FeedbackEvent> getFakeSendResult() {
        return event -> CompletableFuture.completedFuture(
                new SendResult<>(new ProducerRecord<>("feedbacks", event),
                        new RecordMetadata(new TopicPartition("feedbacks", 0), 0L, 0, 0L, 0, 0)));
    }

    private Feedback getNewFeedbackWithAllFields() {
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        return new Feedback(1L, 1L, 5, "comment", timeOfCreation, timeOfModification);
    }
}
