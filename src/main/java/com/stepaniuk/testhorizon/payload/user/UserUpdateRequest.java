package com.stepaniuk.testhorizon.payload.user;


import com.stepaniuk.testhorizon.validation.shared.Email;
import com.stepaniuk.testhorizon.validation.shared.password.Password;
import com.stepaniuk.testhorizon.validation.user.FirstName;
import com.stepaniuk.testhorizon.validation.user.LastName;
import jakarta.annotation.Nullable;
import lombok.*;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UserUpdateRequest {
    @Email
    @Nullable
    private final String email;
    @Password
    @Nullable
    private final String password;
    @FirstName
    @Nullable
    private final String firstName;
    @LastName
    @Nullable
    private final String lastName;
}
