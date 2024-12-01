package com.stepaniuk.testhorizon.payload.user;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
@Relation(collectionRelation = "users", itemRelation = "users")
public class UserResponse extends RepresentationModel<UserResponse> {
    @NotNull
    private final String email;
    @NotNull
    private final String firstName;
    @NotNull
    private final String lastName;
    @NotNull
    private final Instant createdAt;
    @NotNull
    private final Instant lastModifiedAt;
}
