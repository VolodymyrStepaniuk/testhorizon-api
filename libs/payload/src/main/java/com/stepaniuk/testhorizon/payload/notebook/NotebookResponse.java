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
    private Long id;

    @NotNull
    private UserInfo owner;

    @Title
    @NotNull
    private String title;

    @Description
    @Nullable
    private String description;

    @NotNull
    private Instant createdAt;

    @NotNull
    private Instant updatedAt;
}
