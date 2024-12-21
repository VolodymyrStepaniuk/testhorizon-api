package com.stepaniuk.testhorizon.payload.testcase;

import com.stepaniuk.testhorizon.testcase.priority.TestCasePriorityName;
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
public class TestCaseCreateRequest {

    @NotNull
    private Long projectId;

    @NotNull
    private String title;

    @Nullable
    private String description;

    @Nullable
    private String preconditions;

    @Nullable
    private String inputData;

    @NotNull
    private List<String> steps;

    @NotNull
    private TestCasePriorityName priority;
}
