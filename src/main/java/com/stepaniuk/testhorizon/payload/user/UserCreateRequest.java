package com.stepaniuk.testhorizon.payload.user;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UserCreateRequest {
    @NotNull
    private final String email;
    @NotNull
    private final String password;
    @NotNull
    private final String firstName;
    @NotNull
    private final String lastName;
}
