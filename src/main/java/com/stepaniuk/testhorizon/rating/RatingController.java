package com.stepaniuk.testhorizon.rating;

import com.stepaniuk.testhorizon.payload.rating.RatingResponse;
import com.stepaniuk.testhorizon.payload.rating.RatingUpdateRequest;
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

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/ratings", produces = "application/json")
@Validated
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<RatingResponse> changeRating(@Valid @RequestBody RatingUpdateRequest request, AuthInfo authInfo) {
        return new ResponseEntity<>(ratingService.changeRating(request, authInfo.getUserId(), UUID.randomUUID().toString()), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PagedModel<RatingResponse>> getRatings(Pageable pageable,
                                                                 @Nullable @RequestParam(required = false) Long userId,
                                                                 @Nullable @RequestParam(required = false) Long ratedByUserId) {
        return ResponseEntity.ok(ratingService.getRatings(pageable, userId, ratedByUserId));
    }
}
