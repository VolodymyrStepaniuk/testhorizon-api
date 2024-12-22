package com.stepaniuk.testhorizon.payload.test;

import com.stepaniuk.testhorizon.test.type.TestTypeName;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TestCreateRequest {

    @NotNull
    private Long projectId;

    @Nullable
    private Long testCaseId;

    @NotNull
    private String title;

    @Nullable
    private String description;

    @Nullable
    private String instructions;

    @NotNull
    private String githubUrl;

    @NotNull
    private TestTypeName type;
}
