package com.stepaniuk.testhorizon.payload.project;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProjectCreateRequest {

    @NotNull
    private String name;

    @NotNull
    private String description;

    @Nullable
    private String instruction;

    @NotNull
    private String githubUrl;

    @NotNull
    private List<String> imageUrls;
}
