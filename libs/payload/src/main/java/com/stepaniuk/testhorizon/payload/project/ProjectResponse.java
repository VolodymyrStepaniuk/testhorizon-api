package com.stepaniuk.testhorizon.payload.project;

import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.types.project.ProjectStatusName;
import com.stepaniuk.testhorizon.validation.shared.Id;
import com.stepaniuk.testhorizon.validation.shared.Title;
import com.stepaniuk.testhorizon.validation.shared.Description;
import com.stepaniuk.testhorizon.validation.shared.Instructions;
import com.stepaniuk.testhorizon.validation.shared.GithubUrl;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
@Relation(collectionRelation = "projects", itemRelation = "projects")
public class ProjectResponse extends RepresentationModel<ProjectResponse> {

    @Id
    @NotNull
    private final Long id;

    @NotNull
    private final UserInfo owner;

    @Title
    @NotNull
    private final String title;

    @Description
    @NotNull
    private final String description;

    @Instructions
    @Nullable
    private final String instructions;

    @GithubUrl
    @NotNull
    private final String githubUrl;

    @NotNull
    private final ProjectStatusName status;

    @NotNull
    private final Instant createdAt;

    @NotNull
    private final Instant updatedAt;
}
