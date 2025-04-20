package com.stepaniuk.testhorizon.payload.notebook;

import com.stepaniuk.testhorizon.validation.shared.Description;
import com.stepaniuk.testhorizon.validation.shared.Title;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NotebookUpdateRequest {
    @Title
    @Nullable
    private String title;

    @Description
    @Nullable
    private String description;
}
