package com.stepaniuk.testhorizon.payload.auth;

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
public class PasswordResetConfirmRequest {

    @NotNull
    @Password
    private String newPassword;

}
