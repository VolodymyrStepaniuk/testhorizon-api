package com.stepaniuk.testhorizon.payload.testcase;

import com.stepaniuk.testhorizon.testcase.priority.TestCasePriorityName;
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
public class TestCaseUpdateRequest {

    @Nullable
    private String title;

    @Nullable
    private String description;

    @Nullable
    private String preconditions;

    @Nullable
    private String inputData;

    @Nullable
    private List<String> steps;

    @Nullable
    private TestCasePriorityName priority;
}
