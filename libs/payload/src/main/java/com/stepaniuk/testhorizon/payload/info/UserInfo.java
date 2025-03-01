package com.stepaniuk.testhorizon.payload.info;

import com.stepaniuk.testhorizon.validation.shared.Id;
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

    @Id
    @NotNull
    private final Long id;

    @FirstName
    @NotNull
    private final String firstName;

    @LastName
    @NotNull
    private final String lastName;
}
