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
    private Long id;

    @NotNull
    private ProjectInfo project;

    @NotNull
    private UserInfo author;

    @Title
    @NotNull
    private String title;

    @Description
    @NotNull
    private String description;

    @Preconditions
    @NotNull
    private String preconditions;

    @InputData
    @NotNull
    private String inputData;

    @NotNull
    @Size(min = 1)
    private List<@Step String> steps;

    @NotNull
    private TestCasePriorityName priority;

    @NotNull
    private Instant createdAt;

    @NotNull
    private Instant updatedAt;
}
