package com.stepaniuk.testhorizon.rating;

import com.stepaniuk.testhorizon.event.rating.RatingEvent;
import com.stepaniuk.testhorizon.event.rating.RatingUpdatedEvent;
import com.stepaniuk.testhorizon.payload.rating.RatingResponse;
import com.stepaniuk.testhorizon.payload.rating.RatingUpdateRequest;
import com.stepaniuk.testhorizon.rating.exceptions.UserCannotChangeOwnRatingException;
import com.stepaniuk.testhorizon.shared.PageMapperImpl;
import com.stepaniuk.testhorizon.testspecific.ServiceLevelUnitTest;
import com.stepaniuk.testhorizon.user.User;
import com.stepaniuk.testhorizon.user.UserRepository;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchUserByIdException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ServiceLevelUnitTest
@ContextConfiguration(classes = {RatingService.class, RatingMapperImpl.class, PageMapperImpl.class})
class RatingServiceTest {

    @Autowired
    private RatingService ratingService;

    @MockitoBean
    private RatingProducer ratingProducer;

    @MockitoBean
    private RatingRepository ratingRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldReturnRatingResponseWhenChangingRating() {
        // given
        Long ratedByUserId = 1L;
        RatingUpdateRequest request = new RatingUpdateRequest(2L, 5, "comment");
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        User newUser = new User(null, "John", "Doe", "johndoe@gmail.com", 120, "Password+123",
                true, true, true, true,
                Set.of(), timeOfCreation, timeOfModification);

        // when
        when(ratingRepository.save(any())).thenAnswer(answer(getFakeSave(1L)));
        when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
        final var receivedEventWrapper = new RatingUpdatedEvent[1];
        when(
                ratingProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (RatingUpdatedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        RatingResponse ratingResponse = ratingService.changeRating(request, ratedByUserId, UUID.randomUUID().toString());

        // then
        assertNotNull(ratingResponse);
        assertEquals(request.getUserId(), ratingResponse.getUserId());
        assertEquals(request.getRatingPoints(), ratingResponse.getRatingPoints());
        assertEquals(request.getComment(), ratingResponse.getComment());
        assertEquals(ratedByUserId, ratingResponse.getRatedByUserId());
        assertTrue(ratingResponse.hasLinks());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(ratingResponse.getUserId(), receivedEvent.getUserId());
        assertEquals(ratingResponse.getRatedByUserId(), receivedEvent.getRatedByUserId());
        assertEquals(ratingResponse.getRatingPoints(), receivedEvent.getRatingPoints());
    }

    @Test
    void shouldThrowNoSuchUserByIdExceptionWhenChangingRating() {
        // given
        var correlationId = UUID.randomUUID().toString();
        Long ratedByUserId = 1L;
        RatingUpdateRequest request = new RatingUpdateRequest(2L, 5, "comment");

        // when
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // then
        assertThrows(NoSuchUserByIdException.class, () -> ratingService.changeRating(request, ratedByUserId, correlationId));
    }

    @Test
    void shouldThrowUserCannotChangeOwnRatingExceptionWhenChangingRating() {
        // given
        var correlationId = UUID.randomUUID().toString();
        Long ratedByUserId = 1L;
        RatingUpdateRequest request = new RatingUpdateRequest(1L, 5, "comment");

        // then
        assertThrows(UserCannotChangeOwnRatingException.class, () -> ratingService.changeRating(request, ratedByUserId, correlationId));
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllRatings() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Long userId = 1L;
        Long ratedByUserId = 2L;

        Rating ratingToFind = new Rating(1L, userId, ratedByUserId, 5, "comment", timeOfCreation);
        var pageable = PageRequest.of(0, 2);

        Specification<Rating> specification = Specification.where(null);

        // when
        when(ratingRepository.findAll(specification, pageable)).thenReturn(new PageImpl<>(List.of(ratingToFind), pageable, 1));

        var ratings = ratingService.getRatings(pageable, null, null);
        var ratingResponse = ratings.getContent().iterator().next();

        // then
        assertNotNull(ratings);
        assertNotNull(ratings.getMetadata());
        assertEquals(1, ratings.getMetadata().getTotalElements());
        assertEquals(1, ratings.getContent().size());

        assertNotNull(ratingResponse);
        assertEquals(ratingToFind.getId(), ratingResponse.getId());
        assertEquals(ratingToFind.getUserId(), ratingResponse.getUserId());
        assertEquals(ratingToFind.getRatedByUserId(), ratingResponse.getRatedByUserId());
        assertEquals(ratingToFind.getRatingPoints(), ratingResponse.getRatingPoints());
        assertEquals(ratingToFind.getComment(), ratingResponse.getComment());
        assertEquals(ratingToFind.getCreatedAt(), ratingResponse.getCreatedAt());
        assertTrue(ratingResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingRatingByUserId() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Long userId = 1L;
        Long ratedByUserId = 2L;

        Rating ratingToFind = new Rating(1L, userId, ratedByUserId, 5, "comment", timeOfCreation);
        var pageable = PageRequest.of(0, 2);

        // when
        when(ratingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(ratingToFind), pageable, 1));

        var ratings = ratingService.getRatings(pageable, userId, null);
        var ratingResponse = ratings.getContent().iterator().next();

        // then
        assertNotNull(ratings);
        assertNotNull(ratings.getMetadata());
        assertEquals(1, ratings.getMetadata().getTotalElements());
        assertEquals(1, ratings.getContent().size());

        assertNotNull(ratingResponse);
        assertEquals(ratingToFind.getId(), ratingResponse.getId());
        assertEquals(userId, ratingResponse.getUserId());
        assertEquals(ratingToFind.getRatedByUserId(), ratingResponse.getRatedByUserId());
        assertEquals(ratingToFind.getRatingPoints(), ratingResponse.getRatingPoints());
        assertEquals(ratingToFind.getComment(), ratingResponse.getComment());
        assertEquals(ratingToFind.getCreatedAt(), ratingResponse.getCreatedAt());
        assertTrue(ratingResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingRatingByRatedByUserId() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Long userId = 1L;
        Long ratedByUserId = 2L;

        Rating ratingToFind = new Rating(1L, userId, ratedByUserId, 5, "comment", timeOfCreation);
        var pageable = PageRequest.of(0, 2);

        // when
        when(ratingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(ratingToFind), pageable, 1));

        var ratings = ratingService.getRatings(pageable, null, ratedByUserId);
        var ratingResponse = ratings.getContent().iterator().next();

        // then
        assertNotNull(ratings);
        assertNotNull(ratings.getMetadata());
        assertEquals(1, ratings.getMetadata().getTotalElements());
        assertEquals(1, ratings.getContent().size());

        assertNotNull(ratingResponse);
        assertEquals(ratingToFind.getId(), ratingResponse.getId());
        assertEquals(ratingToFind.getUserId(), ratingResponse.getUserId());
        assertEquals(ratedByUserId, ratingResponse.getRatedByUserId());
        assertEquals(ratingToFind.getRatingPoints(), ratingResponse.getRatingPoints());
        assertEquals(ratingToFind.getComment(), ratingResponse.getComment());
        assertEquals(ratingToFind.getCreatedAt(), ratingResponse.getCreatedAt());
        assertTrue(ratingResponse.hasLinks());
    }

    private Answer1<Rating, Rating> getFakeSave(long id) {
        return rating -> {
            rating.setId(id);
            return rating;
        };
    }

    private Answer1<CompletableFuture<SendResult<String, RatingEvent>>, RatingEvent> getFakeSendResult() {
        return event -> CompletableFuture.completedFuture(
                new SendResult<>(new ProducerRecord<>("ratings", event),
                        new RecordMetadata(new TopicPartition("ratings", 0), 0L, 0, 0L, 0, 0)));
    }
}
