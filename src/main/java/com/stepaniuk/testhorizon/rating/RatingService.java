package com.stepaniuk.testhorizon.rating;

import com.stepaniuk.testhorizon.event.rating.RatingUpdatedEvent;
import com.stepaniuk.testhorizon.payload.rating.RatingResponse;
import com.stepaniuk.testhorizon.payload.rating.RatingUpdateRequest;
import com.stepaniuk.testhorizon.rating.exceptions.UserCannotChangeOwnRatingException;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.user.User;
import com.stepaniuk.testhorizon.user.UserRepository;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchUserByIdException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.PagedModel;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final RatingMapper ratingMapper;
    private final PageMapper pageMapper;
    private final RatingProducer ratingProducer;

    public RatingResponse changeRating(RatingUpdateRequest request, Long ratedByUserId, String correlationId) {

        Long userId = request.getUserId();

        if (userId.equals(ratedByUserId)) {
            throw new UserCannotChangeOwnRatingException(ratedByUserId);
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchUserByIdException(userId));

        Rating rating = new Rating();
        rating.setUserId(userId);
        rating.setRatingPoints(request.getRatingPoints());
        rating.setComment(request.getComment());
        rating.setRatedByUserId(ratedByUserId);

        Rating savedRating = ratingRepository.save(rating);

        user.setTotalRating(user.getTotalRating() + rating.getRatingPoints());
        userRepository.save(user);

        ratingProducer.send(
                new RatingUpdatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        savedRating.getUserId(), savedRating.getRatedByUserId(), savedRating.getRatingPoints()
                )
        );

        return ratingMapper.toResponse(savedRating);
    }

    public PagedModel<RatingResponse> getRatings(Pageable pageable,
                                                 @Nullable Long userId,
                                                 @Nullable Long ratedByUserId) {

        Specification<Rating> specification = Specification.where(null);

        if (userId != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get("userId"), userId)
            );
        }

        if (ratedByUserId != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get("ratedByUserId"), ratedByUserId)
            );
        }

        var ratings = ratingRepository.findAll(specification, pageable);

        return pageMapper.toResponse(
                ratings.map(
                        ratingMapper::toResponse
                ), URI.create("/projects")
        );
    }

}
