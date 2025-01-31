package com.stepaniuk.testhorizon.project;

import com.stepaniuk.testhorizon.payload.project.ProjectCreateRequest;
import com.stepaniuk.testhorizon.payload.project.ProjectResponse;
import com.stepaniuk.testhorizon.payload.project.ProjectUpdateRequest;
import com.stepaniuk.testhorizon.project.status.ProjectStatusName;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/projects", produces = "application/json")
@Validated
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectCreateRequest projectCreateRequest, AuthInfo authInfo) {
        return new ResponseEntity<>(projectService.createProject(projectCreateRequest, authInfo.getUserId(), UUID.randomUUID().toString()), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectUpdateRequest projectUpdateRequest, AuthInfo authInfo) {
        return ResponseEntity.ok(projectService.updateProject(id, projectUpdateRequest, UUID.randomUUID().toString(), authInfo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProjectById(@PathVariable Long id, AuthInfo authInfo) {
        projectService.deleteProjectById(id, UUID.randomUUID().toString(), authInfo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedModel<ProjectResponse>> getAllProjects(Pageable pageable,
                                                                      @Nullable @RequestParam(required = false) Long ownerId,
                                                                      @Nullable @RequestParam(required = false) String title,
                                                                      @Nullable @RequestParam(required = false) ProjectStatusName status) {
        return ResponseEntity.ok(projectService.getAllProjects(pageable, ownerId, title, status));
    }
}
