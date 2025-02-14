package com.stepaniuk.testhorizon.payload.testcase;

import com.stepaniuk.testhorizon.types.testcase.TestCasePriorityName;
import com.stepaniuk.testhorizon.validation.shared.Description;
import com.stepaniuk.testhorizon.validation.shared.Title;
import com.stepaniuk.testhorizon.validation.testcase.InputData;
import com.stepaniuk.testhorizon.validation.testcase.Preconditions;
import com.stepaniuk.testhorizon.validation.testcase.Step;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
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

    @Title
    @Nullable
    private String title;

    @Description
    @Nullable
    private String description;

    @Preconditions
    @Nullable
    private String preconditions;

    @InputData
    @Nullable
    private String inputData;

    @Nullable
    @Size(min = 1)
    private List<@Step String> steps;

    @Nullable
    private TestCasePriorityName priority;
}
