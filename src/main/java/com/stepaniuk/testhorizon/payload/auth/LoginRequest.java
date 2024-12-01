package com.stepaniuk.testhorizon.payload.auth;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LoginRequest {
    @NotNull
    private final String email;
    @NotNull
    private final String password;
}
