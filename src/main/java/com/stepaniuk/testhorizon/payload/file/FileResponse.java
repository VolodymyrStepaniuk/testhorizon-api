package com.stepaniuk.testhorizon.payload.file;

import com.stepaniuk.testhorizon.validation.shared.Url;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
@Relation(collectionRelation = "files", itemRelation = "files")
public class FileResponse extends RepresentationModel<FileResponse> {

    @NotNull
    @Url
    private String fileUrl;
}
