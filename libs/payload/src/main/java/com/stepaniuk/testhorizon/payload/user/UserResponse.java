package com.stepaniuk.testhorizon.payload.user;

import com.stepaniuk.testhorizon.types.user.AuthorityName;
import com.stepaniuk.testhorizon.validation.shared.Email;
import com.stepaniuk.testhorizon.validation.shared.Id;
import com.stepaniuk.testhorizon.validation.shared.Rating;
import com.stepaniuk.testhorizon.validation.user.FirstName;
import com.stepaniuk.testhorizon.validation.user.LastName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;
import java.util.Set;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
@Relation(collectionRelation = "users", itemRelation = "users")
public class UserResponse extends RepresentationModel<UserResponse> {
    @Id
    @NotNull
    private final Long id;
    @Email
    @NotNull
    private final String email;
    @FirstName
    @NotNull
    private final String firstName;
    @LastName
    @NotNull
    private final String lastName;
    @Rating
    @NotNull
    private final Integer totalRating;
    @Size(min = 1)
    @NotNull
    private final Set<AuthorityName> authorities;
    @NotNull
    private final Instant createdAt;
    @NotNull
    private final Instant updatedAt;
}
