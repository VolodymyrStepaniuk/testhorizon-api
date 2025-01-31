package com.stepaniuk.testhorizon.comment;

import com.stepaniuk.testhorizon.comment.type.CommentEntityType;
import com.stepaniuk.testhorizon.payload.comment.CommentCreateRequest;
import com.stepaniuk.testhorizon.payload.comment.CommentResponse;
import com.stepaniuk.testhorizon.payload.comment.CommentUpdateRequest;
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
@RequestMapping(path = "/comments", produces = "application/json")
@Validated
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentCreateRequest commentCreateRequest, AuthInfo authInfo) {
        return new ResponseEntity<>(commentService.createComment(commentCreateRequest, authInfo.getUserId(), UUID.randomUUID().toString()), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable Long id, @Valid @RequestBody CommentUpdateRequest commentUpdateRequest, AuthInfo authInfo) {
        return ResponseEntity.ok(commentService.updateComment(id, authInfo.getUserId(), commentUpdateRequest, UUID.randomUUID().toString(), authInfo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommentById(@PathVariable Long id, AuthInfo authInfo) {
        commentService.deleteCommentById(id, UUID.randomUUID().toString(), authInfo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<PagedModel<CommentResponse>> getAllComments(Pageable pageable,
                                                                      @RequestParam(required = false) Long authorId){
        return ResponseEntity.ok(commentService.getAllComments(pageable, authorId));
    }

    @GetMapping
    public ResponseEntity<PagedModel<CommentResponse>> getCommentsByEntity(Pageable pageable,
                                                                          @RequestParam Long entityId,
                                                                          @RequestParam CommentEntityType entityType) {
        return ResponseEntity.ok(commentService.getCommentsByEntity(pageable, entityId, entityType));
    }
}
