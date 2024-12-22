package com.stepaniuk.testhorizon.payload.test;

import com.stepaniuk.testhorizon.test.type.TestTypeName;
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
@Relation(collectionRelation = "testCases", itemRelation = "testCases")
public class TestResponse extends RepresentationModel<TestResponse> {

    @NotNull
    private Long id;

    @NotNull
    private Long projectId;

    @Nullable
    private Long testCaseId;

    @NotNull
    private Long authorId;

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

    @NotNull
    private Instant createdAt;

    @NotNull
    private Instant updatedAt;
}
