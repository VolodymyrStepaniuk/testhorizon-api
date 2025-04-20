package com.stepaniuk.testhorizon.notebook;

import com.stepaniuk.testhorizon.event.notebook.NotebookCreatedEvent;
import com.stepaniuk.testhorizon.event.notebook.NotebookDeletedEvent;
import com.stepaniuk.testhorizon.event.notebook.NotebookUpdatedEvent;
import com.stepaniuk.testhorizon.notebook.exceptions.NoSuchNotebookByIdException;
import com.stepaniuk.testhorizon.payload.notebook.NotebookCreateRequest;
import com.stepaniuk.testhorizon.payload.notebook.NotebookResponse;
import com.stepaniuk.testhorizon.payload.notebook.NotebookUpdateRequest;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.shared.UserInfoService;
import com.stepaniuk.testhorizon.shared.exceptions.AccessToManageEntityDeniedException;
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
public class NotebookService {
    private final NotebookRepository notebookRepository;
    private final NotebookMapper notebookMapper;
    private final PageMapper pageMapper;
    private final UserInfoService userInfoService;
    private final NotebookProducer notebookProducer;

    public NotebookResponse createNotebook(NotebookCreateRequest notebookCreateRequest, Long ownerId, String correlationId) {
        Notebook notebook = new Notebook();

        notebook.setOwnerId(ownerId);
        notebook.setTitle(notebookCreateRequest.getTitle());
        notebook.setDescription(notebookCreateRequest.getDescription());

        var savedNotebook = notebookRepository.save(notebook);
        var ownerInfo = userInfoService.getUserInfo(ownerId);

        notebookProducer.send(
                new NotebookCreatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        savedNotebook.getId(), savedNotebook.getOwnerId()
                )
        );

        return notebookMapper.toResponse(savedNotebook, ownerInfo);
    }

    public NotebookResponse getNotebookById(Long id) {
        return notebookRepository.findById(id)
                .map(notebook -> notebookMapper.toResponse(notebook, userInfoService.getUserInfo(notebook.getOwnerId())))
                .orElseThrow(() -> new NoSuchNotebookByIdException(id));
    }

    public void deleteNotebookById(Long id, String correlationId, AuthInfo authInfo) {
        var notebook = notebookRepository.findById(id)
                .orElseThrow(() -> new NoSuchNotebookByIdException(id));

        if (hasNoAccessToManageNotebook(notebook.getOwnerId(), authInfo)) {
            throw new AccessToManageEntityDeniedException("Notebook", "/notebooks");
        }

        notebookRepository.delete(notebook);

        notebookProducer.send(
                new NotebookDeletedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        id
                )
        );
    }

    public NotebookResponse updateNotebook(Long id, NotebookUpdateRequest notebookUpdateRequest, String correlationId, AuthInfo authInfo) {
        var notebook = notebookRepository.findById(id)
                .orElseThrow(() -> new NoSuchNotebookByIdException(id));

        if (hasNoAccessToManageNotebook(notebook.getOwnerId(), authInfo)) {
            throw new AccessToManageEntityDeniedException("Notebook", "/notebooks");
        }

        var notebookData = new NotebookUpdatedEvent.Data();

        if (notebookUpdateRequest.getTitle() != null) {
            notebook.setTitle(notebookUpdateRequest.getTitle());
            notebookData.setTitle(notebookUpdateRequest.getTitle());
        }

        if (notebookUpdateRequest.getDescription() != null) {
            notebook.setDescription(notebookUpdateRequest.getDescription());
            notebookData.setDescription(notebookUpdateRequest.getDescription());
        }

        var updatedNotebook = notebookRepository.save(notebook);
        var ownerInfo = userInfoService.getUserInfo(updatedNotebook.getOwnerId());

        notebookProducer.send(
                new NotebookUpdatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        updatedNotebook.getId(), notebookData
                )
        );

        return notebookMapper.toResponse(updatedNotebook, ownerInfo);
    }

    public PagedModel<NotebookResponse> getAllNotebooks(Pageable pageable,
                                                      @Nullable Long ownerId,
                                                      @Nullable String title) {

        Specification<Notebook> specification = Specification.where(null);

        if (ownerId != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get("ownerId"), ownerId)
            );
        }

        if (title != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%")
            );
        }

        var notebooks = notebookRepository.findAll(specification, pageable);

        return pageMapper.toResponse(
                notebooks.map(notebook -> notebookMapper.toResponse(notebook, userInfoService.getUserInfo(notebook.getOwnerId()))),
                URI.create("/notebooks")
        );
    }

    private boolean hasNoAccessToManageNotebook(Long ownerId, AuthInfo authInfo) {
        return !(isOwner(authInfo, ownerId) || hasAuthority(authInfo, AuthorityName.ADMIN.name()));
    }
}
