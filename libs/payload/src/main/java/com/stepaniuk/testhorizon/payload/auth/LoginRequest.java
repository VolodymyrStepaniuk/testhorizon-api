package com.stepaniuk.testhorizon.payload.auth;

import com.stepaniuk.testhorizon.validation.shared.Email;
import com.stepaniuk.testhorizon.validation.shared.password.Password;
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
    @Email
    @NotNull
    private final String email;
    @Password
    @NotNull
    private final String password;
}
