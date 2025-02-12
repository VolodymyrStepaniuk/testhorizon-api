package com.stepaniuk.testhorizon.payload.project;

import com.stepaniuk.testhorizon.project.status.ProjectStatusName;
import com.stepaniuk.testhorizon.validation.shared.Description;
import com.stepaniuk.testhorizon.validation.shared.Instructions;
import com.stepaniuk.testhorizon.validation.shared.Title;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProjectUpdateRequest {

    @Title
    @Nullable
    private String title;

    @Description
    @Nullable
    private String description;

    @Nullable
    private ProjectStatusName status;

    @Instructions
    @Nullable
    private String instructions;
}
