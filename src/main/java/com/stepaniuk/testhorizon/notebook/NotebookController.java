package com.stepaniuk.testhorizon.notebook;

import com.stepaniuk.testhorizon.payload.notebook.NotebookCreateRequest;
import com.stepaniuk.testhorizon.payload.notebook.NotebookResponse;
import com.stepaniuk.testhorizon.payload.notebook.NotebookUpdateRequest;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/notebooks", produces = "application/json")
@Validated
public class NotebookController {

    private final NotebookService notebookService;

    @PostMapping
    public ResponseEntity<NotebookResponse> createNotebook(@Valid @RequestBody NotebookCreateRequest notebookCreateRequest, AuthInfo authInfo) {
        return new ResponseEntity<>(
                notebookService.createNotebook(notebookCreateRequest, authInfo.getUserId(), UUID.randomUUID().toString()), HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotebookResponse> getNotebookById(@PathVariable Long id) {
        return ResponseEntity.ok(notebookService.getNotebookById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<NotebookResponse> updateNotebook(@PathVariable Long id, @Valid @RequestBody NotebookUpdateRequest notebookUpdateRequest, AuthInfo authInfo) {
        return ResponseEntity.ok(notebookService.updateNotebook(id, notebookUpdateRequest, UUID.randomUUID().toString(), authInfo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotebookById(@PathVariable Long id, AuthInfo authInfo) {
        notebookService.deleteNotebookById(id, UUID.randomUUID().toString(), authInfo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedModel<NotebookResponse>> getAllNotebooks(Pageable pageable,
                                                                       @RequestParam(required = false) Long ownerId,
                                                                       @RequestParam(required = false) String title) {
        return ResponseEntity.ok(notebookService.getAllNotebooks(pageable,ownerId, title));
    }

}
