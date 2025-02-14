package com.stepaniuk.testhorizon.payload.test;

import com.stepaniuk.testhorizon.validation.shared.Id;
import com.stepaniuk.testhorizon.validation.shared.Title;
import com.stepaniuk.testhorizon.validation.shared.Description;
import com.stepaniuk.testhorizon.validation.shared.Instructions;
import com.stepaniuk.testhorizon.validation.shared.GithubUrl;
import com.stepaniuk.testhorizon.types.test.TestTypeName;

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

    @Id
    @NotNull
    private Long projectId;

    @Id
    @Nullable
    private Long testCaseId;

    @Title
    @NotNull
    private String title;

    @Description
    @Nullable
    private String description;

    @Instructions
    @Nullable
    private String instructions;

    @GithubUrl
    @NotNull
    private String githubUrl;

    @NotNull
    private TestTypeName type;
}
