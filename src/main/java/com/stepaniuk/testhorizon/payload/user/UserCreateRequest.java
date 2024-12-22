package com.stepaniuk.testhorizon.payload.user;

import com.stepaniuk.testhorizon.validation.shared.Email;
import com.stepaniuk.testhorizon.validation.shared.password.Password;
import com.stepaniuk.testhorizon.validation.user.FirstName;
import com.stepaniuk.testhorizon.validation.user.LastName;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UserCreateRequest {
    @Email
    @NotNull
    private final String email;

    @Password
    @NotNull
    private final String password;

    @FirstName
    @NotNull
    private final String firstName;

    @LastName
    @NotNull
    private final String lastName;
}
