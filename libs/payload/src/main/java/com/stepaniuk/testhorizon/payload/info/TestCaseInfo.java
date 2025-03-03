package com.stepaniuk.testhorizon.payload.info;

import com.stepaniuk.testhorizon.validation.shared.Id;
import com.stepaniuk.testhorizon.validation.shared.Title;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TestCaseInfo {
    @Id
    @NotNull
    private final Long id;

    @Title
    @NotNull
    private final String title;
}
