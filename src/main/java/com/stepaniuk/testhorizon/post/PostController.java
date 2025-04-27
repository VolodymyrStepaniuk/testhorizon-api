package com.stepaniuk.testhorizon.post;

import com.stepaniuk.testhorizon.payload.post.PostCreateRequest;
import com.stepaniuk.testhorizon.payload.post.PostResponse;
import com.stepaniuk.testhorizon.payload.post.PostUpdateRequest;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.types.post.PostCategoryName;
import jakarta.annotation.Nullable;
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
@RequestMapping(path = "/posts", produces = "application/json")
@Validated
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostCreateRequest postCreateRequest, AuthInfo authInfo) {
        return new ResponseEntity<>(postService.createPost(postCreateRequest, authInfo.getUserId(), UUID.randomUUID().toString()), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id, @Valid @RequestBody PostUpdateRequest postUpdateRequest, AuthInfo authInfo) {
        return ResponseEntity.ok(postService.updatePost(id, postUpdateRequest, UUID.randomUUID().toString(), authInfo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePostById(@PathVariable Long id, AuthInfo authInfo) {
        postService.deletePostById(id, UUID.randomUUID().toString(), authInfo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedModel<PostResponse>> getAllPosts(Pageable pageable,
                                                                @Nullable @RequestParam(required = false) Long ownerId,
                                                                @Nullable @RequestParam(required = false) String title,
                                                                @Nullable @RequestParam(required = false) PostCategoryName category) {

        return ResponseEntity.ok(postService.getAllPosts(pageable, ownerId, title, category));
    }

}
