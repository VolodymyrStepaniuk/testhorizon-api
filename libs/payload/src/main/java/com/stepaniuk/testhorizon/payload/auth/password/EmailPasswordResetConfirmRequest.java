package com.stepaniuk.testhorizon.payload.auth.password;

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
public class EmailPasswordResetConfirmRequest {

    @Password
    @NotNull
    private final String newPassword;

    @Password
    @NotNull
    private final String confirmPassword;

}
