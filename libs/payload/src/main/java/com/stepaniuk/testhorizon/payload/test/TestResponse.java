package com.stepaniuk.testhorizon.payload.test;

import com.stepaniuk.testhorizon.payload.info.ProjectInfo;
import com.stepaniuk.testhorizon.payload.info.TestCaseInfo;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.types.test.TestTypeName;
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
@Relation(collectionRelation = "tests", itemRelation = "tests")
public class TestResponse extends RepresentationModel<TestResponse> {

    @Id
    @NotNull
    private Long id;

    @NotNull
    private ProjectInfo project;

    @Nullable
    private TestCaseInfo testCase;

    @NotNull
    private UserInfo author;

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

    @NotNull
    private Instant createdAt;

    @NotNull
    private Instant updatedAt;
}
