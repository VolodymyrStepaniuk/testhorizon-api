package com.stepaniuk.testhorizon.comment;


import com.stepaniuk.testhorizon.event.comment.CommentCreatedEvent;
import com.stepaniuk.testhorizon.event.comment.CommentDeletedEvent;
import com.stepaniuk.testhorizon.event.comment.CommentUpdatedEvent;
import com.stepaniuk.testhorizon.payload.comment.CommentCreateRequest;
import com.stepaniuk.testhorizon.payload.comment.CommentResponse;
import com.stepaniuk.testhorizon.payload.comment.CommentUpdateRequest;
import com.stepaniuk.testhorizon.comment.exceptions.CommentAuthorMismatchException;
import com.stepaniuk.testhorizon.comment.exceptions.NoSuchCommentByIdException;
import com.stepaniuk.testhorizon.payload.comment.user.UserInfo;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.shared.exceptions.AccessToManageEntityDeniedException;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.types.comment.CommentEntityType;
import com.stepaniuk.testhorizon.user.User;
import com.stepaniuk.testhorizon.user.UserRepository;
import com.stepaniuk.testhorizon.types.user.AuthorityName;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchUserByIdException;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final PageMapper pageMapper;
    private final CommentProducer commentProducer;
    private final UserRepository userRepository;


    public CommentResponse createComment(CommentCreateRequest commentCreateRequest, Long authorId, String correlationId) {
        Comment comment = new Comment();

        comment.setAuthorId(authorId);
        comment.setEntityId(commentCreateRequest.getEntityId());
        comment.setEntityType(commentCreateRequest.getEntityType());
        comment.setContent(commentCreateRequest.getContent());

        var savedComment = commentRepository.save(comment);

        commentProducer.send(
                new CommentCreatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        savedComment.getId(), savedComment.getAuthorId(), savedComment.getEntityType(), savedComment.getEntityId()
                )
        );

        return commentMapper.toResponse(savedComment, getAuthorInfo(authorId));
    }


    public CommentResponse updateComment(Long commentId, Long userId, CommentUpdateRequest commentUpdateRequest, String correlationId, AuthInfo authInfo) {
        var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchCommentByIdException(commentId));

        if (hasNoAccessToManageComment(comment.getAuthorId(), authInfo)){
            throw new AccessToManageEntityDeniedException("Comment", "/comments");
        }

        if (!comment.getAuthorId().equals(userId)) {
            throw new CommentAuthorMismatchException(commentId, userId);
        }

        if (commentUpdateRequest.getContent() != null) {
            comment.setContent(commentUpdateRequest.getContent());
        }

        var updatedComment = commentRepository.save(comment);

        commentProducer.send(
                new CommentUpdatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        updatedComment.getId(), updatedComment.getContent()
                )
        );

        return commentMapper.toResponse(updatedComment, getAuthorInfo(userId));
    }

    public void deleteCommentById(Long id, String correlationId, AuthInfo authInfo) {
        var comment = commentRepository.findById(id)
                .orElseThrow(() -> new NoSuchCommentByIdException(id));

        if (hasNoAccessToManageComment(comment.getAuthorId(), authInfo)){
            throw new AccessToManageEntityDeniedException("Comment", "/comments");
        }

        commentRepository.delete(comment);

        commentProducer.send(
                new CommentDeletedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        id
                )
        );
    }

    private UserInfo getAuthorInfo(Long authorId) {
       User user = userRepository.findById(authorId)
                .orElseThrow(() -> new NoSuchUserByIdException(authorId));
        return new UserInfo(user.getFirstName(), user.getLastName());
    }

    public PagedModel<CommentResponse> getAllComments(Pageable pageable,
                                                      @Nullable Long authorId) {

        Specification<Comment> specification = (authorId != null)
                ? (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("authorId"), authorId)
                : null;

        var comments = (specification != null)
                ? commentRepository.findAll(specification, pageable)
                : commentRepository.findAll(pageable);

        var authorInfo = (authorId != null) ? getAuthorInfo(authorId) : null;

        return pageMapper.toResponse(
                comments.map(comment -> commentMapper.toResponse(comment,
                        (authorInfo != null) ? authorInfo : getAuthorInfo(comment.getAuthorId()))),
                URI.create("/comments")
        );
    }


    public PagedModel<CommentResponse> getCommentsByEntity(Pageable pageable, Long entityId, CommentEntityType entityType) {

        var comments = commentRepository.findByEntityTypeAndEntityId(pageable, entityType, entityId);

        if (comments.isEmpty()) {
            return pageMapper.toResponse(Page.empty(pageable), URI.create("/comments"));
        }

        return pageMapper.toResponse(
                comments.map(comment -> commentMapper.toResponse(comment, getAuthorInfo(comment.getAuthorId()))),
                URI.create("/comments")
        );
    }

    private  boolean hasNoAccessToManageComment(Long authorId, AuthInfo authInfo){
        return !(isOwner(authInfo, authorId) || hasAuthority(authInfo, AuthorityName.ADMIN.name()));
    }
}
