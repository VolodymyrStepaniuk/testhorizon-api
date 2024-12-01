package com.stepaniuk.testhorizon.payload.user;


import jakarta.annotation.Nullable;
import lombok.*;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UserUpdateRequest {
    @Nullable
    private final String email;
    @Nullable
    private final String password;
    @Nullable
    private final String firstName;
    @Nullable
    private final String lastName;
}
