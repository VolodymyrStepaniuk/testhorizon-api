package com.stepaniuk.testhorizon.payload.notebook.note;

import com.stepaniuk.testhorizon.validation.note.Content;
import com.stepaniuk.testhorizon.validation.shared.Title;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class NoteCreateRequest {

    @Title
    @NotNull
    private String title;

    @Content
    @Nullable
    private String content;
}
