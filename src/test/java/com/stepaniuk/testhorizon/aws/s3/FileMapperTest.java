package com.stepaniuk.testhorizon.aws.s3;

import com.stepaniuk.testhorizon.payload.file.FileResponse;
import com.stepaniuk.testhorizon.testspecific.MapperLevelUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MapperLevelUnitTest
@ContextConfiguration(classes = {FileMapperImpl.class})
class FileMapperTest {

    @Autowired
    private FileMapper fileMapper;

    @Test
    void shouldMapUrlToFileResponse() {
        // given
        String fileUrl = "https://testhorizon.s3.eu-central-1.amazonaws.com/1.jpg";

        // when
        FileResponse fileResponse = fileMapper.toResponse(fileUrl);

        // then
        assertNotNull(fileResponse);
        assertEquals(fileUrl, fileResponse.getFileUrl());
    }
}
