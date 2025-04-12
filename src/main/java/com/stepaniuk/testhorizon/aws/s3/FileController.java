package com.stepaniuk.testhorizon.aws.s3;

import com.stepaniuk.testhorizon.payload.file.FileResponse;
import com.stepaniuk.testhorizon.types.files.FileEntityType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final S3Service s3Service;

    @PostMapping("/{entityType}/{id}")
    public ResponseEntity<PagedModel<FileResponse>> uploadFiles(
            @PathVariable FileEntityType entityType,
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files) {

        return ResponseEntity.ok(s3Service.uploadFiles(files, entityType, id, UUID.randomUUID().toString()));
    }

    @DeleteMapping("/{entityType}/{id}")
    public ResponseEntity<Void> deleteFiles(
            @PathVariable FileEntityType entityType,
            @PathVariable Long id,
            @RequestParam List<String> fileNames,
            Pageable pageable) {
        s3Service.deleteFiles(entityType, id, fileNames, pageable, UUID.randomUUID().toString());

        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/delete-folder/{entityType}/{id}")
    public ResponseEntity<Void> deleteFolder(
            @PathVariable FileEntityType entityType,
            @PathVariable Long id) {

        s3Service.deleteFolder(entityType, id, UUID.randomUUID().toString());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list/{entityType}/{id}")
    public ResponseEntity<PagedModel<FileResponse>> listFilesByEntityTypeAndId(
            @PathVariable FileEntityType entityType,
            @PathVariable Long id) {

        return ResponseEntity.ok(s3Service.listFiles(entityType, id));
    }

    @GetMapping("/{entityType}/{id}/{fileName}")
    public ResponseEntity<FileResponse> getFileByEntityTypeAndId(
            @PathVariable FileEntityType entityType,
            @PathVariable Long id,
            @PathVariable String fileName) {

        return ResponseEntity.ok(s3Service.getFileByEntityTypeAndId(entityType, id, fileName));
    }
}
