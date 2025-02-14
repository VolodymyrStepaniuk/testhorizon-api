package com.stepaniuk.testhorizon.payload.auth;

import com.stepaniuk.testhorizon.validation.shared.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PasswordResetRequest {
    @Email
    @NotNull
    private final String email;
}
