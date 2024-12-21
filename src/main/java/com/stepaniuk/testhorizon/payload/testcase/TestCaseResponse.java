package com.stepaniuk.testhorizon.payload.testcase;

import com.stepaniuk.testhorizon.testcase.priority.TestCasePriorityName;
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
@Relation(collectionRelation = "testCases", itemRelation = "testCases")
public class TestCaseResponse extends RepresentationModel<TestCaseResponse> {

    @NotNull
    private Long projectId;

    @NotNull
    private String title;

    @NotNull
    private String description;

    @NotNull
    private String preconditions;

    @NotNull
    private String inputData;

    @NotNull
    private List<String> steps;

    @NotNull
    private TestCasePriorityName priority;

    @NotNull
    private Instant createdAt;

    @NotNull
    private Instant updatedAt;
}
