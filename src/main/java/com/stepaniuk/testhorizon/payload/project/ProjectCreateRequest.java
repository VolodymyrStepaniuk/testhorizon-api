package com.stepaniuk.testhorizon.payload.project;

import com.stepaniuk.testhorizon.validation.shared.*;
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
public class ProjectCreateRequest {

    @Title
    @NotNull
    private String title;

    @Description
    @NotNull
    private String description;

    @Instructions
    @Nullable
    private String instructions;

    @GithubUrl
    @NotNull
    private String githubUrl;
}
