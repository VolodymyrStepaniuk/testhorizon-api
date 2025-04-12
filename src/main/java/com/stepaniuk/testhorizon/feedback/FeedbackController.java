package com.stepaniuk.testhorizon.feedback;

import com.stepaniuk.testhorizon.payload.feedback.FeedbackCreateRequest;
import com.stepaniuk.testhorizon.payload.feedback.FeedbackResponse;
import com.stepaniuk.testhorizon.payload.feedback.FeedbackUpdateRequest;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/feedbacks", produces = "application/json")
@Validated
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<FeedbackResponse> createFeedback(@Valid @RequestBody FeedbackCreateRequest feedbackCreateRequest, AuthInfo authInfo) {
        return new ResponseEntity<>(feedbackService.createFeedback(feedbackCreateRequest, authInfo.getUserId(), UUID.randomUUID().toString()), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponse> getFeedbackById(@PathVariable Long id, AuthInfo authInfo) {
        return ResponseEntity.ok(feedbackService.getFeedback(id, authInfo));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FeedbackResponse> updateFeedback(@PathVariable Long id, @Valid @RequestBody FeedbackUpdateRequest feedbackUpdateRequest) {
        return ResponseEntity.ok(feedbackService.updateFeedback(id, feedbackUpdateRequest, UUID.randomUUID().toString()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedbackById(@PathVariable Long id) {
        feedbackService.deleteFeedback(id, UUID.randomUUID().toString());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedModel<FeedbackResponse>> getAllFeedbacks(Pageable pageable,
                                                                        @Nullable @RequestParam(required = false) Long ownerId,
                                                                        @Nullable @RequestParam(required = false) List<Long> feedbackIds) {

        return ResponseEntity.ok(feedbackService.getAllFeedbacks(pageable, ownerId, feedbackIds));
    }
}
