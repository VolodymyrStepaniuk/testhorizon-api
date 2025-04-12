package com.stepaniuk.testhorizon.feedback;

import com.stepaniuk.testhorizon.event.feedback.FeedbackCreatedEvent;
import com.stepaniuk.testhorizon.event.feedback.FeedbackDeletedEvent;
import com.stepaniuk.testhorizon.event.feedback.FeedbackUpdatedEvent;
import com.stepaniuk.testhorizon.feedback.exceptions.NoSuchFeedbackFoundByIdException;
import com.stepaniuk.testhorizon.payload.feedback.FeedbackCreateRequest;
import com.stepaniuk.testhorizon.payload.feedback.FeedbackResponse;
import com.stepaniuk.testhorizon.payload.feedback.FeedbackUpdateRequest;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.shared.UserInfoService;
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

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final FeedbackMapper feedbackMapper;
    private final PageMapper pageMapper;
    private final FeedbackProducer feedbackProducer;
    private final UserInfoService userInfoService;

    public FeedbackResponse createFeedback(FeedbackCreateRequest feedbackCreateRequest,
                                           Long userId, String correlationId) {
        var feedback = new Feedback();

        feedback.setRating(feedbackCreateRequest.getRating());
        feedback.setComment(feedbackCreateRequest.getComment());
        feedback.setOwnerId(userId);

        var savedFeedback = feedbackRepository.save(feedback);

        feedbackProducer.send(
                new FeedbackCreatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        feedback.getId()
                )
        );

        return feedbackMapper.toResponse(savedFeedback,
                userInfoService.getUserInfo(userId));
    }

    public FeedbackResponse getFeedback(Long feedbackId, AuthInfo authInfo) {
        var feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new NoSuchFeedbackFoundByIdException(feedbackId));

        return feedbackMapper.toResponse(feedback,
                userInfoService.getUserInfo(authInfo.getUserId()));
    }

    public FeedbackResponse updateFeedback(Long id, FeedbackUpdateRequest request,
                                           String correlationId) {
        var feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new NoSuchFeedbackFoundByIdException(id));

        var data = new FeedbackUpdatedEvent.Data();

        if (request.getRating() != null) {
            feedback.setRating(request.getRating());
            data.setRating(request.getRating());
        }

        if (request.getComment() != null) {
            feedback.setComment(request.getComment());
            data.setComment(request.getComment());
        }

        feedbackProducer.send(
                new FeedbackUpdatedEvent(Instant.now(), UUID.randomUUID().toString(), correlationId, id,
                        data));

        var updatedFeedback = feedbackRepository.save(feedback);

        return feedbackMapper.toResponse(updatedFeedback,
                userInfoService.getUserInfo(updatedFeedback.getOwnerId()));
    }

    public void deleteFeedback(Long id, String correlationId) {
        var feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new NoSuchFeedbackFoundByIdException(id));

        feedbackRepository.delete(feedback);
        feedbackProducer.send(
                new FeedbackDeletedEvent(Instant.now(), UUID.randomUUID().toString(), correlationId, id));
    }

    public PagedModel<FeedbackResponse> getAllFeedbacks(Pageable pageable,
                                                        @Nullable Long ownerId,
                                                        @Nullable List<Long> feedbackIds){
        Specification<Feedback> specification = Specification.where(null);
        if (ownerId != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get("ownerId"), ownerId)
            );
        }
        if (feedbackIds != null && !feedbackIds.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .in(root.get("id")).value(feedbackIds)
            );
        }

        var feedbacks = feedbackRepository.findAll(specification, pageable);

        return pageMapper.toResponse(feedbacks.map(feedback -> {
            var userInfo = userInfoService.getUserInfo(feedback.getOwnerId());
            return feedbackMapper.toResponse(feedback, userInfo);
        }), URI.create("/feedbacks"));

    }
}
