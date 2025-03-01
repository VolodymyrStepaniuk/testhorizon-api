package com.stepaniuk.testhorizon.project;

import com.stepaniuk.testhorizon.event.project.ProjectCreatedEvent;
import com.stepaniuk.testhorizon.event.project.ProjectDeletedEvent;
import com.stepaniuk.testhorizon.event.project.ProjectUpdatedEvent;
import com.stepaniuk.testhorizon.payload.project.ProjectCreateRequest;
import com.stepaniuk.testhorizon.payload.project.ProjectResponse;
import com.stepaniuk.testhorizon.payload.project.ProjectUpdateRequest;
import com.stepaniuk.testhorizon.project.exceptions.NoSuchProjectByIdException;
import com.stepaniuk.testhorizon.project.exceptions.NoSuchProjectStatusByNameException;
import com.stepaniuk.testhorizon.project.status.ProjectStatus;
import com.stepaniuk.testhorizon.project.status.ProjectStatusRepository;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.shared.UserInfoService;
import com.stepaniuk.testhorizon.shared.exceptions.AccessToManageEntityDeniedException;
import com.stepaniuk.testhorizon.types.project.ProjectStatusName;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final PageMapper pageMapper;
    private final ProjectStatusRepository projectStatusRepository;
    private final ProjectProducer projectProducer;
    private final UserInfoService userInfoService;

    public ProjectResponse createProject(ProjectCreateRequest projectCreateRequest, Long ownerId, String correlationId) {
        Project project = new Project();

        project.setOwnerId(ownerId);
        project.setTitle(projectCreateRequest.getTitle());
        project.setDescription(projectCreateRequest.getDescription());
        project.setInstructions(projectCreateRequest.getInstructions());
        project.setGithubUrl(projectCreateRequest.getGithubUrl());
        project.setStatus(
                projectStatusRepository.findByName(ProjectStatusName.ACTIVE)
                        .orElseThrow(() -> new NoSuchProjectStatusByNameException(ProjectStatusName.ACTIVE))
        );

        var savedProject = projectRepository.save(project);
        var ownerInfo = userInfoService.getUserInfo(ownerId);

        projectProducer.send(
                new ProjectCreatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        savedProject.getId(), savedProject.getOwnerId()
                )
        );

        return projectMapper.toResponse(savedProject, ownerInfo);
    }

    public ProjectResponse getProjectById(Long id) {
        return projectRepository.findById(id)
                .map(project -> projectMapper.toResponse(project, userInfoService.getUserInfo(project.getOwnerId())))
                .orElseThrow(() -> new NoSuchProjectByIdException(id));
    }

    public void deleteProjectById(Long id, String correlationId, AuthInfo authInfo) {
        var project = projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchProjectByIdException(id));

        if (hasNoAccessToManageProject(project.getOwnerId(), authInfo)) {
            throw new AccessToManageEntityDeniedException("Project", "/projects");
        }

        projectRepository.delete(project);

        projectProducer.send(
                new ProjectDeletedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        id
                )
        );
    }

    public ProjectResponse updateProject(Long id, ProjectUpdateRequest projectUpdateRequest, String correlationId, AuthInfo authInfo) {
        var project = projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchProjectByIdException(id));

        if (hasNoAccessToManageProject(project.getOwnerId(), authInfo)) {
            throw new AccessToManageEntityDeniedException("Project", "/projects");
        }

        var projectData = new ProjectUpdatedEvent.Data();

        if (projectUpdateRequest.getTitle() != null) {
            project.setTitle(projectUpdateRequest.getTitle());
            projectData.setTitle(projectUpdateRequest.getTitle());
        }

        if (projectUpdateRequest.getDescription() != null) {
            project.setDescription(projectUpdateRequest.getDescription());
            projectData.setDescription(projectUpdateRequest.getDescription());
        }

        if (projectUpdateRequest.getInstructions() != null) {
            project.setInstructions(projectUpdateRequest.getInstructions());
            projectData.setInstructions(projectUpdateRequest.getInstructions());
        }

        if (projectUpdateRequest.getStatus() != null) {
            project.setStatus(
                    projectStatusRepository.findByName(projectUpdateRequest.getStatus())
                            .orElseThrow(() -> new NoSuchProjectStatusByNameException(projectUpdateRequest.getStatus()))
            );

            projectData.setStatus(projectUpdateRequest.getStatus());
        }

        var updatedProject = projectRepository.save(project);
        var ownerInfo = userInfoService.getUserInfo(updatedProject.getOwnerId());

        projectProducer.send(
                new ProjectUpdatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        updatedProject.getId(), projectData
                )
        );

        return projectMapper.toResponse(updatedProject, ownerInfo);
    }

    public PagedModel<ProjectResponse> getAllProjects(Pageable pageable,
                                                      @Nullable Long ownerId,
                                                      @Nullable String title,
                                                      @Nullable ProjectStatusName statusName) {

        Specification<Project> specification = Specification.where(null);

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

        if (statusName != null) {
            ProjectStatus status = projectStatusRepository.findByName(statusName)
                    .orElseThrow(() -> new NoSuchProjectStatusByNameException(statusName));

            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get("status"), status)
            );
        }

        var projects = projectRepository.findAll(specification, pageable);

        return pageMapper.toResponse(
                projects.map(project -> projectMapper.toResponse(project, userInfoService.getUserInfo(project.getOwnerId()))),
                URI.create("/projects")
        );
    }

    private boolean hasNoAccessToManageProject(Long ownerId, AuthInfo authInfo) {
        return !(isOwner(authInfo, ownerId) || hasAuthority(authInfo, AuthorityName.ADMIN.name()));
    }
}
