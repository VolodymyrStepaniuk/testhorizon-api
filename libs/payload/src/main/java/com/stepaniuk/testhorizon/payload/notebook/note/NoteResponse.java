package com.stepaniuk.testhorizon.payload.notebook.note;

import com.stepaniuk.testhorizon.validation.shared.Content;
import com.stepaniuk.testhorizon.validation.shared.Id;
import com.stepaniuk.testhorizon.validation.shared.Title;
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
@Relation(collectionRelation = "notes", itemRelation = "notes")
public class NoteResponse extends RepresentationModel<NoteResponse> {

    @Id
    @NotNull
    private final Long id;

    @Id
    @NotNull
    private final Long notebookId;

    @Title
    @NotNull
    private final String title;

    @Content
    @Nullable
    private final String content;

    @NotNull
    private final Instant createdAt;

    @NotNull
    private final Instant updatedAt;
}
