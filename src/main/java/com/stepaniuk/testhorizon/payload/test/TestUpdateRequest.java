package com.stepaniuk.testhorizon.payload.test;

import com.stepaniuk.testhorizon.test.type.TestTypeName;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TestUpdateRequest {

    @Nullable
    private Long testCaseId;

    @Nullable
    private String title;

    @Nullable
    private String description;

    @Nullable
    private String instructions;

    @Nullable
    private String githubUrl;

    @Nullable
    private TestTypeName type;
}
