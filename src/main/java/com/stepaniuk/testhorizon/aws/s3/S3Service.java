package com.stepaniuk.testhorizon.aws.s3;

import com.stepaniuk.testhorizon.aws.exceptions.NoSuchFilesByNamesException;
import com.stepaniuk.testhorizon.aws.exceptions.UnableUploadFileException;
import com.stepaniuk.testhorizon.event.file.FileDeleteEvent;
import com.stepaniuk.testhorizon.event.file.FileUploadEvent;
import com.stepaniuk.testhorizon.payload.file.FileResponse;
import com.stepaniuk.testhorizon.shared.PageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final FileRepository fileRepository;
    private final FileMapper fileMapper;
    private final PageMapper pageMapper;
    private final FileProducer fileProducer;

    @Value("${s3.bucket.name}")
    private String bucketName;

    @Value("${spring.cloud.aws.s3.endpoint}")
    private String s3EndpointUrl;


    public PagedModel<FileResponse> uploadFiles(List<MultipartFile> files, String entityType, Long entityId, String correlationId) {
        String folderPath = buildFolderPath(entityType, entityId);
        createFolderIfNotExists(folderPath);

        List<FileResponse> fileResponses = files.stream()
                .map(file -> processFile(file, folderPath, entityType, entityId, correlationId))
                .collect(Collectors.toList());

        var fileResponsesSize = fileResponses.size();

        return pageMapper.toResponse(
                new PageImpl<>(fileResponses, Pageable.ofSize(fileResponsesSize), fileResponsesSize),
                URI.create("/files")
        );
    }

    private FileResponse processFile(MultipartFile file, String folderPath, String entityType, Long entityId, String correlationId) {
        String fileName = file.getOriginalFilename();
        String filePath = buildFilePath(folderPath, fileName);

        try {
            uploadFileToS3(file, filePath);
        } catch (IOException e) {
            throw new UnableUploadFileException(fileName);
        }

        File fileEntity = createFileEntity(fileName, entityType, entityId);
        fileRepository.save(fileEntity);

        fileProducer.send(
                new FileUploadEvent("FileUploadEvent", Instant.now(), UUID.randomUUID().toString(), correlationId, fileName, entityType, entityId)
        );

        String fileUrl = buildFileUrl(filePath);

        return fileMapper.toResponse(fileUrl);
    }

    public void deleteFiles(String entityType, Long entityId, List<String> fileNames, Pageable pageable, String correlationId) {
        Page<File> files = getFilesByNames(entityType, entityId, fileNames, pageable);

        if (files.isEmpty()) {
            throw new NoSuchFilesByNamesException(fileNames);
        }

        String folderName = buildFolderPath(entityType, entityId);
        deleteFilesFromS3(files, folderName);

        for (File file : files) {
            fileProducer.send(
                    new FileDeleteEvent("FileDeleteEvent", Instant.now(), UUID.randomUUID().toString(), correlationId, file.getOriginalName(), entityType, entityId)
            );
        }

        fileRepository.deleteAll(files);
    }

    public void deleteFolder(String entityType, Long entityId, String correlationId) {
        String folderKey = buildFolderPath(entityType, entityId);

        List<S3Object> objects = listObjectsInFolder(folderKey);
        if (!objects.isEmpty()) {
            deleteObjectsFromS3(objects);
        }

        Specification<File> spec = (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("entityType"), entityType),
                criteriaBuilder.equal(root.get("entityId"), entityId)
        );

        List<File> files = fileRepository.findAll(spec);

        files.forEach(file -> fileProducer.send(
                new FileDeleteEvent("FileDeleteEvent", Instant.now(), UUID.randomUUID().toString(), correlationId, file.getOriginalName(), entityType, entityId)
        ));

        if (!files.isEmpty()) {
            fileRepository.deleteAll(files);
        }
    }

    public PagedModel<FileResponse> listFiles(String entityType, Long entityId) {
        return listFilesInS3(entityType, entityId);
    }

    private String buildFolderPath(String entityType, Long entityId) {
        return String.format("%s/%s", entityType, entityId);
    }

    private void createFolderIfNotExists(String folderKey) {
        if (isFolderEmpty(folderKey)) {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(folderKey + "/")
                            .build(),
                    RequestBody.empty());
        }
    }

    private boolean isFolderEmpty(String folderKey) {
        return s3Client.listObjectsV2(ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(folderKey)
                        .build())
                .contents()
                .isEmpty();
    }

    private Page<File> getFilesByNames(String entityType, Long entityId, List<String> fileNames, Pageable pageable) {
        Specification<File> spec = (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("entityType"), entityType),
                criteriaBuilder.equal(root.get("entityId"), entityId),
                root.get("originalName").in(fileNames)
        );

        return fileRepository.findAll(spec, pageable);
    }

    private String buildFilePath(String folder, String fileName) {
        return folder + "/" + fileName;
    }

    private String buildFileUrl(String filePath) {
        return String.format("%s/%s/%s", s3EndpointUrl, bucketName, filePath);
    }

    private void uploadFileToS3(MultipartFile file, String filePath) throws IOException {
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(filePath)
                        .build(),
                RequestBody.fromBytes(file.getBytes()));
    }

    private File createFileEntity(String fileName, String entityType, Long entityId) {
        return new File(null, fileName, entityType, entityId, null);
    }

    private void deleteFilesFromS3(Page<File> files, String folderName) {
        List<ObjectIdentifier> objectsToDelete = files.stream()
                .map(file -> ObjectIdentifier.builder().key(folderName + "/" + file.getOriginalName()).build())
                .toList();

        s3Client.deleteObjects(DeleteObjectsRequest.builder()
                .bucket(bucketName)
                .delete(del -> del.objects(objectsToDelete))
                .build());
    }

    private List<S3Object> listObjectsInFolder(String folderKey) {
        return s3Client.listObjectsV2(ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(folderKey)
                        .build())
                .contents();
    }

    private void deleteObjectsFromS3(List<S3Object> objects) {
        List<ObjectIdentifier> objectIdentifiers = objects.stream()
                .map(obj -> ObjectIdentifier.builder().key(obj.key()).build())
                .toList();

        s3Client.deleteObjects(DeleteObjectsRequest.builder()
                .bucket(bucketName)
                .delete(del -> del.objects(objectIdentifiers))
                .build());
    }

    private PagedModel<FileResponse> listFilesInS3(String entityType, Long entityId) {

        String folderName = buildFolderPath(entityType, entityId);
        List<S3Object> objects = listS3Objects(folderName);

        List<FileResponse> fileResponses = objects.stream()
                .map(S3Object::key)
                .filter(key -> !key.endsWith("/"))
                .map(key -> key.substring(key.lastIndexOf("/") + 1))
                .filter(originalName -> fileRepository.existsByOriginalNameAndEntityTypeAndEntityId(originalName, entityType, entityId))
                .map(originalName -> fileMapper.toResponse(buildFileUrl(folderName + "/" + originalName)))
                .collect(Collectors.toList());

        Pageable pageable = fileResponses.isEmpty() ? PageRequest.of(0, 1) : Pageable.ofSize(fileResponses.size());

        return pageMapper.toResponse(
                new PageImpl<>(fileResponses, pageable, fileResponses.size()),
                URI.create("/files")
        );
    }

    private List<S3Object> listS3Objects(String folderKey) {
        return s3Client.listObjectsV2(ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(folderKey)
                        .build())
                .contents();
    }
}
