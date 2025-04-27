package com.stepaniuk.testhorizon.post;

import com.stepaniuk.testhorizon.event.post.PostCreatedEvent;
import com.stepaniuk.testhorizon.event.post.PostDeletedEvent;
import com.stepaniuk.testhorizon.event.post.PostUpdatedEvent;
import com.stepaniuk.testhorizon.payload.post.PostCreateRequest;
import com.stepaniuk.testhorizon.payload.post.PostResponse;
import com.stepaniuk.testhorizon.payload.post.PostUpdateRequest;
import com.stepaniuk.testhorizon.post.category.PostCategory;
import com.stepaniuk.testhorizon.post.category.PostCategoryRepository;
import com.stepaniuk.testhorizon.post.exceptions.NoSuchPostByIdException;
import com.stepaniuk.testhorizon.post.exceptions.NoSuchPostCategoryByNameException;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.shared.UserInfoService;
import com.stepaniuk.testhorizon.shared.exceptions.AccessToManageEntityDeniedException;
import com.stepaniuk.testhorizon.types.post.PostCategoryName;
import com.stepaniuk.testhorizon.types.user.AuthorityName;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import static com.stepaniuk.testhorizon.security.SecurityUtils.hasAuthority;
import static com.stepaniuk.testhorizon.security.SecurityUtils.isOwner;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final PostProducer postProducer;
    private final PostMapper postMapper;
    private final PageMapper pageMapper;
    private final UserInfoService userInfoService;

    public PostResponse createPost(PostCreateRequest request, Long ownerId, String correlationId) {
        Post post = new Post();

        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setContent(request.getContent());
        post.setOwnerId(ownerId);
        post.setCategory(
                postCategoryRepository.findByName(request.getCategory())
                        .orElseThrow(() -> new NoSuchPostCategoryByNameException(request.getCategory()))
        );

        var savedPost = postRepository.save(post);
        var ownerInfo = userInfoService.getUserInfo(ownerId);

        postProducer.send(
                new PostCreatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        savedPost.getId(), savedPost.getOwnerId()
                )
        );

        return postMapper.toResponse(savedPost, ownerInfo);
    }

    public PostResponse getPostById(Long postId) {
        return postMapper.toResponse(
                postRepository.findById(postId)
                        .orElseThrow(() -> new NoSuchPostByIdException(postId)),
                userInfoService.getUserInfo(postId)
        );
    }

    public void deletePostById(Long id, String correlationId, AuthInfo authInfo) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NoSuchPostByIdException(id));

        if (hasNoAccessToManagePost(post.getOwnerId(), authInfo)) {
            throw new AccessToManageEntityDeniedException("Post", "/posts");
        }

        postRepository.deleteById(id);

        postProducer.send(
                new PostDeletedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        id
                )
        );
    }

    public PostResponse updatePost(Long id, PostUpdateRequest request, String correlationId, AuthInfo authInfo) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NoSuchPostByIdException(id));

        if (hasNoAccessToManagePost(post.getOwnerId(), authInfo)) {
            throw new AccessToManageEntityDeniedException("Post", "/posts");
        }

        var postData = new PostUpdatedEvent.Data();

        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
            postData.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            post.setDescription(request.getDescription());
            postData.setDescription(request.getDescription());
        }

        if (request.getContent() != null) {
            post.setContent(request.getContent());
            postData.setContent(request.getContent());
        }

        if (request.getCategory() != null) {
            post.setCategory(
                    postCategoryRepository.findByName(request.getCategory())
                            .orElseThrow(() -> new NoSuchPostCategoryByNameException(request.getCategory()))
            );
            postData.setCategory(request.getCategory());
        }

        var updatedPost = postRepository.save(post);
        var ownerInfo = userInfoService.getUserInfo(post.getOwnerId());

        postProducer.send(
                new PostUpdatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        updatedPost.getId(), postData
                )
        );

        return postMapper.toResponse(updatedPost, ownerInfo);
    }

    public PagedModel<PostResponse> getAllPosts(Pageable pageable,
                                                @Nullable Long ownerId,
                                                @Nullable String title,
                                                @Nullable PostCategoryName categoryName) {

        Specification<Post> specification = Specification.where(null);

        if (ownerId != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("ownerId"), ownerId)
            );
        }

        if (title != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%")
            );
        }

        if (categoryName != null) {
            PostCategory category = postCategoryRepository.findByName(categoryName)
                    .orElseThrow(() -> new NoSuchPostCategoryByNameException(categoryName));

            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get("category"), category)
            );
        }

        var postsPage = postRepository.findAll(specification, pageable);

        return pageMapper.toResponse(
                postsPage.map(post -> postMapper.toResponse(post, userInfoService.getUserInfo(post.getOwnerId()))),
                URI.create("/posts")
        );
    }

    private boolean hasNoAccessToManagePost(Long ownerId, AuthInfo authInfo) {
        return !(isOwner(authInfo, ownerId) || hasAuthority(authInfo, AuthorityName.ADMIN.name()));
    }
}
