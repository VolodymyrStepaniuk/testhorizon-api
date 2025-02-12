package com.stepaniuk.testhorizon.aws.s3;


import com.stepaniuk.testhorizon.aws.exceptions.NoSuchFilesByNamesException;
import com.stepaniuk.testhorizon.aws.exceptions.UnableUploadFileException;
import com.stepaniuk.testhorizon.event.file.FileDeleteEvent;
import com.stepaniuk.testhorizon.event.file.FileEvent;
import com.stepaniuk.testhorizon.event.file.FileUploadEvent;
import com.stepaniuk.testhorizon.payload.file.FileResponse;
import com.stepaniuk.testhorizon.shared.PageMapperImpl;
import com.stepaniuk.testhorizon.testspecific.ServiceLevelUnitTest;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.PagedModel;
import org.springframework.kafka.support.SendResult;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.*;

@ServiceLevelUnitTest
@ContextConfiguration(classes = {S3Service.class, FileMapperImpl.class, PageMapperImpl.class})
@TestPropertySource(properties = {
        "s3.bucket.name=testbucket",
        "spring.cloud.aws.s3.endpoint=http://localhost:4566"
})
class S3ServiceTest {

    @Autowired
    private S3Service s3Service;

    @MockitoBean
    private FileRepository fileRepository;

    @MockitoBean
    private S3Client s3Client;

    @MockitoBean
    private FileProducer fileProducer;

    @Test
    void shouldUploadFiles() {
        // given
        var fileName = "file1.jpg";
        List<MultipartFile> files = List.of(
                new MockMultipartFile("file1", fileName, "image/jpeg", new byte[0])
        );
        String entityType = "entityType";
        Long entityId = 1L;
        String correlationId = "correlationId";

        when(fileRepository.save(any(File.class))).then(answer(getFakeSave(UUID.fromString("00000000-0000-0000-0000-000000000001"))));
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenReturn(ListObjectsV2Response.builder()
                        .contents(S3Object.builder()
                                .key(fileName)
                                .build())
                        .build());

        final var receivedEventWrapper = new FileUploadEvent[1];
        when(
                fileProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (FileUploadEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        // when
        PagedModel<FileResponse> fileResponses = s3Service.uploadFiles(files, entityType, entityId, correlationId);

        var fileResponse = fileResponses.getContent().iterator().next();
        // then
        assertNotNull(fileResponses);
        assertEquals(1, fileResponses.getContent().size());

        assertEquals(String.format("%s/%s/%s/%s/%s", "http://localhost:4566", "testbucket", entityType, entityId, fileName), fileResponse.getFileUrl());
        assertTrue(fileResponse.getFileUrl().contains(fileName));
        assertTrue(fileResponse.getFileUrl().contains("entityType"));
        assertTrue(fileResponse.getFileUrl().contains("1"));

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(fileName, receivedEvent.getFileName());
        assertEquals(entityType, receivedEvent.getEntityType());
        assertEquals(entityId, receivedEvent.getEntityId());

        verify(fileRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowUnableUploadFileExceptionWhenUploadingFiles() throws IOException {
        // given
        MultipartFile mockFile = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(mockFile);
        String entityType = "entityType";
        Long entityId = 1L;
        String correlationId = "correlationId";

        // Mock for the getBytes method throwing IOException
        when(mockFile.getBytes()).thenThrow(IOException.class);

        // Mock the listObjectsV2 method
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenReturn(ListObjectsV2Response.builder().contents(Collections.emptyList()).build());

        // Act and Assert
        assertThrows(UnableUploadFileException.class, () ->
                s3Service.uploadFiles(files, entityType, entityId, correlationId));
    }

    @Test
    void shouldDeleteAllFilesAndReturnVoid(){
        List<String> fileNames = List.of("file1.jpg");
        String entityType = "entityType";
        Long entityId = 1L;

        List<File> files = List.of(
                new File(UUID.randomUUID(), "file1.jpg",  entityType, entityId, Instant.now())
        );

        var pageable = PageRequest.of(0, 1);

        final var receivedEventWrapper = new FileDeleteEvent[1];
        when(fileProducer.send(
                assertArg(event -> receivedEventWrapper[0] = (FileDeleteEvent) event))).thenAnswer(
                answer(getFakeSendResult())
        );

        when(fileRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(files));

        s3Service.deleteFiles(entityType,entityId,fileNames,pageable,UUID.randomUUID().toString());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals("file1.jpg", receivedEvent.getFileName());
        assertEquals(entityType, receivedEvent.getEntityType());
        assertEquals(entityId, receivedEvent.getEntityId());

        verify(fileRepository, times(1)).deleteAll(any(Page.class));
    }

    @Test
    void shouldThrowNoSuchFilesByNamesExceptionWhenDeleteAllFiles() {
        // given
        List<String> fileNames = List.of("file1.jpg");
        String entityType = "entityType";
        Long entityId = 1L;
        String correlationId = UUID.randomUUID().toString();

        var pageable = PageRequest.of(0, 1);

        when(fileRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        assertThrows(NoSuchFilesByNamesException.class, () ->
                s3Service.deleteFiles(entityType, entityId, fileNames, pageable, correlationId));
    }

    @Test
    void shouldDeleteFolderWithFiles() {
        // given
        String entityType = "entityType";
        Long entityId = 1L;
        String correlationId = UUID.randomUUID().toString();
        String folderKey = String.format("%s/%s", entityType, entityId);

        List<S3Object> s3Objects = List.of(S3Object.builder().key(folderKey + "/file1.jpg").build());
        List<File> files = List.of(new File(UUID.randomUUID(), "file1.jpg", entityType, entityId, Instant.now()));

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(ListObjectsV2Response.builder().contents(s3Objects).build());
        when(fileRepository.findAll(any(Specification.class))).thenReturn(files);

        // when
        s3Service.deleteFolder(entityType, entityId, correlationId);

        // then
        verify(s3Client, times(1)).deleteObjects(any(DeleteObjectsRequest.class));
        verify(fileRepository, times(1)).deleteAll(files);
        verify(fileProducer, times(1)).send(any(FileDeleteEvent.class));
    }

    @Test
    void shouldDeleteFolderWithoutFiles() {
        // given
        String entityType = "entityType";
        Long entityId = 1L;
        String correlationId = UUID.randomUUID().toString();

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(ListObjectsV2Response.builder().contents(Collections.emptyList()).build());
        when(fileRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        // when
        s3Service.deleteFolder(entityType, entityId, correlationId);

        // then
        verify(s3Client, never()).deleteObjects(any(DeleteObjectsRequest.class));
        verify(fileRepository, never()).deleteAll(anyList());
        verify(fileProducer, never()).send(any(FileDeleteEvent.class));
    }

    @Test
    void shouldReturnPagedModelWithFileResponsesWhenFilesExist() {
        // given
        String entityType = "entityType";
        Long entityId = 1L;
        String folderName = String.format("%s/%s", entityType, entityId);
        List<S3Object> s3Objects = List.of(
                S3Object.builder().key(folderName + "/file1.jpg").build(),
                S3Object.builder().key(folderName + "/file2.jpg").build()
        );

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(ListObjectsV2Response.builder().contents(s3Objects).build());
        when(fileRepository.existsByOriginalNameAndEntityTypeAndEntityId("file1.jpg", entityType, entityId)).thenReturn(true);
        when(fileRepository.existsByOriginalNameAndEntityTypeAndEntityId("file2.jpg", entityType, entityId)).thenReturn(true);

        // when
        PagedModel<FileResponse> result = s3Service.listFiles(entityType, entityId);

        // then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream().anyMatch(fileResponse -> fileResponse.getFileUrl().contains("file1.jpg")));
        assertTrue(result.getContent().stream().anyMatch(fileResponse -> fileResponse.getFileUrl().contains("file2.jpg")));
    }

    @Test
    void shouldReturnEmptyPagedModelWhenNoFilesExist() {
        // given
        String entityType = "entityType";
        Long entityId = 1L;

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(ListObjectsV2Response.builder().contents(Collections.emptyList()).build());

        // when
        PagedModel<FileResponse> result = s3Service.listFiles(entityType, entityId);

        // then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void shouldReturnPagedModelWithOnlyExistingFilesInRepository() {
        // given
        String entityType = "entityType";
        Long entityId = 1L;
        String folderName = String.format("%s/%s", entityType, entityId);
        List<S3Object> s3Objects = List.of(
                S3Object.builder().key(folderName + "/file1.jpg").build(),
                S3Object.builder().key(folderName + "/file2.jpg").build()
        );

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(ListObjectsV2Response.builder().contents(s3Objects).build());
        when(fileRepository.existsByOriginalNameAndEntityTypeAndEntityId("file1.jpg", entityType, entityId)).thenReturn(true);
        when(fileRepository.existsByOriginalNameAndEntityTypeAndEntityId("file2.jpg", entityType, entityId)).thenReturn(false);

        // when
        PagedModel<FileResponse> result = s3Service.listFiles(entityType, entityId);

        // then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().stream().anyMatch(fileResponse -> fileResponse.getFileUrl().contains("file1.jpg")));
        assertFalse(result.getContent().stream().anyMatch(fileResponse -> fileResponse.getFileUrl().contains("file2.jpg")));
    }

    private Answer1<File, File> getFakeSave(UUID id) {
        return file -> {
            file.setId(id);
            return file;
        };
    }

    private Answer1<CompletableFuture<SendResult<String, FileEvent>>, FileEvent> getFakeSendResult() {
        return event -> CompletableFuture.completedFuture(
                new SendResult<>(new ProducerRecord<>("files", event),
                        new RecordMetadata(new TopicPartition("files", 0), 0L, 0, 0L, 0, 0)));
    }

}
