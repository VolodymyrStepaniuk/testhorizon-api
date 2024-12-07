package com.stepaniuk.testhorizon.payload.project;

import com.stepaniuk.testhorizon.project.status.ProjectStatusName;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProjectUpdateRequest {
    @Nullable
    private String name;

    @Nullable
    private String description;

    @Nullable
    private ProjectStatusName status;

    @Nullable
    private String instruction;

    @Nullable
    private List<String> imageUrls;
}
