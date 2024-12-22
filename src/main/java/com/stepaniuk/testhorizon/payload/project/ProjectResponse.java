package com.stepaniuk.testhorizon.payload.project;

import com.stepaniuk.testhorizon.payload.user.UserResponse;
import com.stepaniuk.testhorizon.project.status.ProjectStatusName;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
@Relation(collectionRelation = "projects", itemRelation = "projects")
public class ProjectResponse extends RepresentationModel<UserResponse> {

    @NotNull
    private Long id;

    @NotNull
    private Long ownerId;

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

    @NotNull
    private ProjectStatusName status;

    @NotNull
    private Instant createdAt;

    @NotNull
    private Instant updatedAt;
}
