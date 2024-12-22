package com.stepaniuk.testhorizon.project;

import com.stepaniuk.testhorizon.payload.project.ProjectCreateRequest;
import com.stepaniuk.testhorizon.payload.project.ProjectResponse;
import com.stepaniuk.testhorizon.payload.project.ProjectUpdateRequest;
import com.stepaniuk.testhorizon.project.exception.NoSuchProjectByIdException;
import com.stepaniuk.testhorizon.project.exception.NoSuchProjectStatusByNameException;
import com.stepaniuk.testhorizon.project.status.ProjectStatus;
import com.stepaniuk.testhorizon.project.status.ProjectStatusName;
import com.stepaniuk.testhorizon.project.status.ProjectStatusRepository;
import com.stepaniuk.testhorizon.shared.PageMapper;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final PageMapper pageMapper;
    private final ProjectStatusRepository projectStatusRepository;

    public ProjectResponse createProject(ProjectCreateRequest projectCreateRequest, Long ownerId){
        Project project = new Project();

        project.setOwnerId(ownerId);
        project.setTitle(projectCreateRequest.getTitle());
        project.setDescription(projectCreateRequest.getDescription());
        project.setInstructions(projectCreateRequest.getInstructions());
        project.setGithubUrl(projectCreateRequest.getGithubUrl());
        project.setImageUrls(projectCreateRequest.getImageUrls());
        project.setStatus(
                projectStatusRepository.findByName(ProjectStatusName.ACTIVE)
                        .orElseThrow(() -> new NoSuchProjectStatusByNameException(ProjectStatusName.ACTIVE))
        );

        var savedProject = projectRepository.save(project);

        return projectMapper.toResponse(savedProject);
    }

    public ProjectResponse getProjectById(Long id){
        return projectRepository.findById(id)
                .map(projectMapper::toResponse)
                .orElseThrow(() -> new NoSuchProjectByIdException(id));
    }

    public void deleteProjectById(Long id){
        var project = projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchProjectByIdException(id));

        projectRepository.delete(project);
    }

    public ProjectResponse updateProject(Long id, ProjectUpdateRequest projectUpdateRequest){
        var project = projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchProjectByIdException(id));

        if(projectUpdateRequest.getTitle() != null)
            project.setTitle(projectUpdateRequest.getTitle());

        if(projectUpdateRequest.getDescription() != null)
            project.setDescription(projectUpdateRequest.getDescription());

        if(projectUpdateRequest.getInstructions() != null)
            project.setInstructions(projectUpdateRequest.getInstructions());

        if(projectUpdateRequest.getImageUrls() != null)
            project.setImageUrls(projectUpdateRequest.getImageUrls());

        if(projectUpdateRequest.getStatus() != null)
            project.setStatus(
                    projectStatusRepository.findByName(projectUpdateRequest.getStatus())
                            .orElseThrow(() -> new NoSuchProjectStatusByNameException(projectUpdateRequest.getStatus()))
            );

        var updatedProject = projectRepository.save(project);

        return projectMapper.toResponse(updatedProject);
    }

    public PagedModel<ProjectResponse> getAllProjects(Pageable pageable,
                                                      @Nullable Long ownerId,
                                                      @Nullable String title,
                                                      @Nullable ProjectStatusName statusName){

        Specification<Project> specification = Specification.where(null);

        if (ownerId != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get("ownerId"), ownerId)
            );
        }

        if(title != null){
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
            projects.map(
                projectMapper::toResponse
            ), URI.create("/projects")
        );
    }
}
