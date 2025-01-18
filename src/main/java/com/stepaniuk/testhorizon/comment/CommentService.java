package com.stepaniuk.testhorizon.comment;

import com.stepaniuk.testhorizon.comment.type.CommentEntityType;
import com.stepaniuk.testhorizon.payload.comment.CommentCreateRequest;
import com.stepaniuk.testhorizon.payload.comment.CommentResponse;
import com.stepaniuk.testhorizon.payload.comment.CommentUpdateRequest;
import com.stepaniuk.testhorizon.payload.comment.exception.CommentAuthorMismatchException;
import com.stepaniuk.testhorizon.payload.comment.exception.NoSuchCommentByIdException;
import com.stepaniuk.testhorizon.payload.comment.user.UserInfo;
import com.stepaniuk.testhorizon.payload.user.UserResponse;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.user.UserService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final PageMapper pageMapper;
    private final UserService userService;


    public CommentResponse createComment(CommentCreateRequest commentCreateRequest, Long authorId) {
        Comment comment = new Comment();

        comment.setAuthorId(authorId);
        comment.setEntityId(commentCreateRequest.getEntityId());
        comment.setEntityType(commentCreateRequest.getEntityType());
        comment.setContent(commentCreateRequest.getContent());

        var savedComment = commentRepository.save(comment);

        return commentMapper.toResponse(savedComment, getAuthorInfo(authorId));
    }


    public CommentResponse updateComment(Long commentId, Long userId, CommentUpdateRequest commentUpdateRequest) {
        var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchCommentByIdException(commentId));

        if (!comment.getAuthorId().equals(userId)) {
            throw new CommentAuthorMismatchException(commentId, userId);
        }

        comment.setContent(commentUpdateRequest.getContent());

        var updatedComment = commentRepository.save(comment);

        return commentMapper.toResponse(updatedComment, getAuthorInfo(userId));
    }

    public void deleteCommentById(Long id) {
        var comment = commentRepository.findById(id)
                .orElseThrow(() -> new NoSuchCommentByIdException(id));

        commentRepository.delete(comment);
    }

    private UserInfo getAuthorInfo(Long authorId) {
        UserResponse author = userService.getUserById(authorId);
        return new UserInfo(author.getFirstName(), author.getLastName());
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
}
