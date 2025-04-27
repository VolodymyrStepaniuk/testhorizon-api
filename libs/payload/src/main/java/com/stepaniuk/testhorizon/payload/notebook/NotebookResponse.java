package com.stepaniuk.testhorizon.payload.notebook;

import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.validation.shared.Description;
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
@Relation(collectionRelation = "notebooks", itemRelation = "notebooks")
public class NotebookResponse extends RepresentationModel<NotebookResponse> {
    @Id
    @NotNull
    private final Long id;

    @NotNull
    private final UserInfo owner;

    @Title
    @NotNull
    private final String title;

    @Description
    @Nullable
    private final String description;

    @NotNull
    private final Instant createdAt;

    @NotNull
    private final Instant updatedAt;
}
