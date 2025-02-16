package com.stepaniuk.testhorizon.payload.testcase;

import com.stepaniuk.testhorizon.types.testcase.TestCasePriorityName;
import com.stepaniuk.testhorizon.validation.shared.Description;
import com.stepaniuk.testhorizon.validation.shared.Id;
import com.stepaniuk.testhorizon.validation.shared.Title;
import com.stepaniuk.testhorizon.validation.testcase.InputData;
import com.stepaniuk.testhorizon.validation.testcase.Preconditions;
import com.stepaniuk.testhorizon.validation.testcase.Step;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
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
public class TestCaseCreateRequest {

    @Id
    @NotNull
    private Long projectId;

    @Title
    @NotNull
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

    @NotNull
    @Size(min = 1)
    private List<@Step String> steps;

    @NotNull
    private TestCasePriorityName priority;
}
