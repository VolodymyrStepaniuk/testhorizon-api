package com.stepaniuk.testhorizon.payload.testcase;

import com.stepaniuk.testhorizon.payload.info.ProjectInfo;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.types.testcase.TestCasePriorityName;
import com.stepaniuk.testhorizon.validation.shared.Description;
import com.stepaniuk.testhorizon.validation.shared.Id;
import com.stepaniuk.testhorizon.validation.shared.Title;
import com.stepaniuk.testhorizon.validation.testcase.InputData;
import com.stepaniuk.testhorizon.validation.testcase.Preconditions;
import com.stepaniuk.testhorizon.validation.testcase.Step;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Relation(collectionRelation = "testCases", itemRelation = "testCases")
public class TestCaseResponse extends RepresentationModel<TestCaseResponse> {

    @Id
    @NotNull
    private final Long id;

    @NotNull
    private final ProjectInfo project;

    @NotNull
    private final UserInfo author;

    @Title
    @NotNull
    private final String title;

    @Description
    @NotNull
    private final String description;

    @Preconditions
    @NotNull
    private final String preconditions;

    @InputData
    @NotNull
    private final String inputData;

    @NotNull
    @Size(min = 1)
    private final List<@Step String> steps;

    @NotNull
    private final TestCasePriorityName priority;

    @NotNull
    private final Instant createdAt;

    @NotNull
    private final Instant updatedAt;
}
