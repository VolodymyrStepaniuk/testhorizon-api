package com.stepaniuk.testhorizon.aws.s3;

import com.stepaniuk.testhorizon.payload.file.FileResponse;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface FileMapper {
    FileResponse toResponse(String fileUrl);
}
