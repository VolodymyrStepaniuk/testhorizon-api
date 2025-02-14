package com.stepaniuk.testhorizon.payload.comment.user;

import com.stepaniuk.testhorizon.validation.user.FirstName;
import com.stepaniuk.testhorizon.validation.user.LastName;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UserInfo {

    @FirstName
    @NotNull
    private final String firstName;

    @LastName
    @NotNull
    private final String lastName;
}
