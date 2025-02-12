package com.stepaniuk.testhorizon.project;

import com.stepaniuk.testhorizon.event.project.ProjectCreatedEvent;
import com.stepaniuk.testhorizon.event.project.ProjectDeletedEvent;
import com.stepaniuk.testhorizon.event.project.ProjectEvent;
import com.stepaniuk.testhorizon.event.project.ProjectUpdatedEvent;
import com.stepaniuk.testhorizon.payload.project.ProjectCreateRequest;
import com.stepaniuk.testhorizon.payload.project.ProjectUpdateRequest;
import com.stepaniuk.testhorizon.project.exceptions.NoSuchProjectByIdException;
import com.stepaniuk.testhorizon.project.exceptions.NoSuchProjectStatusByNameException;
import com.stepaniuk.testhorizon.project.status.ProjectStatus;
import com.stepaniuk.testhorizon.project.status.ProjectStatusName;
import com.stepaniuk.testhorizon.project.status.ProjectStatusRepository;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.shared.PageMapperImpl;
import com.stepaniuk.testhorizon.shared.exceptions.AccessToManageEntityDeniedException;
import com.stepaniuk.testhorizon.testspecific.ServiceLevelUnitTest;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.stubbing.Answer1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ServiceLevelUnitTest
@ContextConfiguration(classes = {ProjectService.class, ProjectMapperImpl.class, PageMapperImpl.class})
class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

    @MockitoBean
    private ProjectProducer projectProducer;

    @MockitoBean
    private ProjectRepository projectRepository;

    @MockitoBean
    private ProjectStatusRepository projectStatusRepository;


    @Test
    void shouldReturnProjectResponseWhenCreatingProject() {
        // given
        ProjectCreateRequest projectCreateRequest = new ProjectCreateRequest("title", "description",
                "instructions", "githubUrl");

        when(projectRepository.save(any())).thenAnswer(answer(getFakeSave(1L)));
        when(projectStatusRepository.findByName(ProjectStatusName.ACTIVE)).thenReturn(Optional.of(new ProjectStatus(1L, ProjectStatusName.ACTIVE)));
        final var receivedEventWrapper = new ProjectCreatedEvent[1];
        when(
                projectProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (ProjectCreatedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        // when
        var projectResponse = projectService.createProject(projectCreateRequest, 1L, UUID.randomUUID().toString());

        // then
        assertNotNull(projectResponse);
        assertEquals(1L, projectResponse.getOwnerId());
        assertEquals(projectCreateRequest.getTitle(), projectResponse.getTitle());
        assertEquals(projectCreateRequest.getDescription(), projectResponse.getDescription());
        assertEquals(projectCreateRequest.getInstructions(), projectResponse.getInstructions());
        assertEquals(projectCreateRequest.getGithubUrl(), projectResponse.getGithubUrl());
        assertEquals(ProjectStatusName.ACTIVE, projectResponse.getStatus());
        assertTrue(projectResponse.hasLinks());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(1L, receivedEvent.getProjectId());
        assertEquals(projectResponse.getOwnerId(), receivedEvent.getOwnerId());

        verify(projectRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowNoSuchProjectStatusByNameExceptionWhenCreatingProject() {
        // given
        var correlationId = UUID.randomUUID().toString();
        ProjectCreateRequest projectCreateRequest = new ProjectCreateRequest("title", "description",
                "instructions", "githubUrl");

        when(projectStatusRepository.findByName(ProjectStatusName.ACTIVE)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchProjectStatusByNameException.class, () -> projectService.createProject(projectCreateRequest, 1L, correlationId));
    }

    @Test
    void shouldReturnProjectResponseWhenGettingProjectById() {
        // given
        Project project = getNewProjectWithAllFields();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        // when
        var projectResponse = projectService.getProjectById(1L);

        // then
        assertNotNull(projectResponse);
        assertEquals(project.getId(), projectResponse.getId());
        assertEquals(project.getOwnerId(), projectResponse.getOwnerId());
        assertEquals(project.getTitle(), projectResponse.getTitle());
        assertEquals(project.getDescription(), projectResponse.getDescription());
        assertEquals(project.getInstructions(), projectResponse.getInstructions());
        assertEquals(project.getGithubUrl(), projectResponse.getGithubUrl());
        assertEquals(project.getStatus().getName(), projectResponse.getStatus());
        assertEquals(project.getCreatedAt(), projectResponse.getCreatedAt());
        assertEquals(project.getUpdatedAt(), projectResponse.getUpdatedAt());
        assertTrue(projectResponse.hasLinks());
    }

    @Test
    void shouldThrowNoSuchProjectByIdExceptionWhenGettingProjectById() {
        // given
        when(projectRepository.findById(10L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchProjectByIdException.class, () -> projectService.getProjectById(1L));
    }

    @Test
    void shouldUpdateAndReturnProjectResponseWhenChangingProjectTitle() {
        // given
        Project projectToUpdate = getNewProjectWithAllFields();
        var projectUpdateRequest = new ProjectUpdateRequest("newTitle", null, null, null);
        var authInfo = new AuthInfo(1L, List.of());
        when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(projectToUpdate));
        when(projectRepository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
        var receivedEventWrapper = new ProjectUpdatedEvent[1];
        when(
                projectProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (ProjectUpdatedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        // when
        var updatedProjectResponse = projectService.updateProject(1L, projectUpdateRequest, UUID.randomUUID().toString(), authInfo);

        // then
        assertNotNull(updatedProjectResponse);
        assertEquals(projectToUpdate.getId(), updatedProjectResponse.getId());
        assertEquals(projectToUpdate.getOwnerId(), updatedProjectResponse.getOwnerId());
        assertEquals(projectUpdateRequest.getTitle(), updatedProjectResponse.getTitle());
        assertEquals(projectToUpdate.getDescription(), updatedProjectResponse.getDescription());
        assertEquals(projectToUpdate.getInstructions(), updatedProjectResponse.getInstructions());
        assertEquals(projectToUpdate.getGithubUrl(), updatedProjectResponse.getGithubUrl());
        assertEquals(projectToUpdate.getStatus().getName(), updatedProjectResponse.getStatus());
        assertEquals(projectToUpdate.getCreatedAt(), updatedProjectResponse.getCreatedAt());
        assertEquals(projectToUpdate.getUpdatedAt(), updatedProjectResponse.getUpdatedAt());
        assertTrue(updatedProjectResponse.hasLinks());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(updatedProjectResponse.getId(), receivedEvent.getProjectId());
        assertEquals(updatedProjectResponse.getTitle(), receivedEvent.getData().getTitle());
        assertNull(receivedEvent.getData().getDescription());

        verify(projectRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowNoSuchProjectByIdExceptionWhenUpdatingProject() {
        // given
        var authInfo = new AuthInfo(1L, List.of());
        var correlationId = UUID.randomUUID().toString();
        ProjectUpdateRequest projectUpdateRequest = new ProjectUpdateRequest("newTitle", null, null, null);

        when(projectRepository.findById(10L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchProjectByIdException.class, () -> projectService.updateProject(10L, projectUpdateRequest, correlationId, authInfo));
    }

    @Test
    void shouldThrowNoSuchProjectStatusByNameExceptionWhenUpdatingProject() {
        // given
        var authInfo = new AuthInfo(1L, List.of());
        var correlationId = UUID.randomUUID().toString();
        Project projectToUpdate = getNewProjectWithAllFields();
        var projectUpdateRequest = new ProjectUpdateRequest(null, null, ProjectStatusName.INACTIVE, null);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectToUpdate));
        when(projectStatusRepository.findByName(ProjectStatusName.INACTIVE)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchProjectStatusByNameException.class, () -> projectService.updateProject(1L, projectUpdateRequest, correlationId, authInfo));
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenUpdatingProject() {
        // given
        var authInfo = new AuthInfo(2L, List.of());
        var correlationId = UUID.randomUUID().toString();
        Project projectToUpdate = getNewProjectWithAllFields();
        var projectUpdateRequest = new ProjectUpdateRequest("newTitle", null, null, null);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectToUpdate));

        // when && then
        assertThrows(AccessToManageEntityDeniedException.class, () -> projectService.updateProject(1L, projectUpdateRequest, correlationId, authInfo));
    }

    @Test
    void shouldDeleteAndReturnVoidWhenDeletingExistingProject() {
        // given
        Project projectToDelete = getNewProjectWithAllFields();
        var authInfo = new AuthInfo(1L, List.of());
        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectToDelete));
        final var receivedEventWrapper = new ProjectDeletedEvent[1];
        when(
                projectProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (ProjectDeletedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        // when
        projectService.deleteProjectById(1L, UUID.randomUUID().toString(), authInfo);

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(projectToDelete.getId(), receivedEvent.getProjectId());
        // then
        verify(projectRepository, times(1)).delete(projectToDelete);
    }

    @Test
    void shouldThrowNoSuchProjectByIdExceptionWhenDeletingProject() {
        // given
        var authInfo = new AuthInfo(1L, List.of());
        var correlationId = UUID.randomUUID().toString();
        when(projectRepository.findById(10L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchProjectByIdException.class, () -> projectService.deleteProjectById(10L, correlationId, authInfo));
    }

    @Test
    void shouldThrowAccessToManageEntityDeniedExceptionWhenDeletingProject() {
        // given
        var authInfo = new AuthInfo(2L, List.of());
        var correlationId = UUID.randomUUID().toString();
        Project projectToDelete = getNewProjectWithAllFields();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectToDelete));

        // when && then
        assertThrows(AccessToManageEntityDeniedException.class, () -> projectService.deleteProjectById(1L, correlationId, authInfo));
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllProjects() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        var projectToFind = new Project(1L, 1L, "title", "description", "instructions", "githubUrl",
                new ProjectStatus(1L, ProjectStatusName.ACTIVE),
                timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);
        Specification<Project> specification = Specification.where(null);

        when(projectRepository.findAll(specification, pageable)).thenReturn(new PageImpl<>(List.of(projectToFind), pageable, 1));

        var projectPageResponse = projectService.getAllProjects(pageable, null, null, null);
        var projectResponse = projectPageResponse.getContent().iterator().next();

        // then
        assertNotNull(projectPageResponse);
        assertNotNull(projectPageResponse.getMetadata());
        assertEquals(1, projectPageResponse.getMetadata().getTotalElements());
        assertEquals(1, projectPageResponse.getContent().size());

        assertNotNull(projectResponse);
        assertEquals(projectToFind.getId(), projectResponse.getId());
        assertEquals(projectToFind.getOwnerId(), projectResponse.getOwnerId());
        assertEquals(projectToFind.getTitle(), projectResponse.getTitle());
        assertEquals(projectToFind.getDescription(), projectResponse.getDescription());
        assertEquals(projectToFind.getInstructions(), projectResponse.getInstructions());
        assertEquals(projectToFind.getGithubUrl(), projectResponse.getGithubUrl());
        assertEquals(projectToFind.getStatus().getName(), projectResponse.getStatus());
        assertEquals(projectToFind.getCreatedAt(), projectResponse.getCreatedAt());
        assertEquals(projectToFind.getUpdatedAt(), projectResponse.getUpdatedAt());
        assertTrue(projectResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllProjectsByOwnerId() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        Long ownerId = 1L;

        var projectToFind = new Project(1L, ownerId, "title", "description", "instructions", "githubUrl",
                new ProjectStatus(1L, ProjectStatusName.ACTIVE),
                timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);

        when(projectRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(projectToFind), pageable, 1));

        var projectPageResponse = projectService.getAllProjects(pageable, ownerId, null, null);
        var projectResponse = projectPageResponse.getContent().iterator().next();

        // then
        assertNotNull(projectPageResponse);
        assertNotNull(projectPageResponse.getMetadata());
        assertEquals(1, projectPageResponse.getMetadata().getTotalElements());
        assertEquals(1, projectPageResponse.getContent().size());

        assertNotNull(projectResponse);
        assertEquals(projectToFind.getId(), projectResponse.getId());
        assertEquals(ownerId, projectResponse.getOwnerId());
        assertEquals(projectToFind.getTitle(), projectResponse.getTitle());
        assertEquals(projectToFind.getDescription(), projectResponse.getDescription());
        assertEquals(projectToFind.getInstructions(), projectResponse.getInstructions());
        assertEquals(projectToFind.getGithubUrl(), projectResponse.getGithubUrl());
        assertEquals(projectToFind.getStatus().getName(), projectResponse.getStatus());
        assertEquals(projectToFind.getCreatedAt(), projectResponse.getCreatedAt());
        assertEquals(projectToFind.getUpdatedAt(), projectResponse.getUpdatedAt());
        assertTrue(projectResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllProjectsByTitle() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        String title = "title";

        var projectToFind = new Project(1L, 1L, title, "description", "instructions", "githubUrl",
                new ProjectStatus(1L, ProjectStatusName.ACTIVE),
                timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);

        when(projectRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(projectToFind), pageable, 1));

        var projectPageResponse = projectService.getAllProjects(pageable, null, title, null);
        var projectResponse = projectPageResponse.getContent().iterator().next();

        // then
        assertNotNull(projectPageResponse);
        assertNotNull(projectPageResponse.getMetadata());
        assertEquals(1, projectPageResponse.getMetadata().getTotalElements());
        assertEquals(1, projectPageResponse.getContent().size());

        assertNotNull(projectResponse);
        assertEquals(projectToFind.getId(), projectResponse.getId());
        assertEquals(projectToFind.getOwnerId(), projectResponse.getOwnerId());
        assertEquals(title, projectResponse.getTitle());
        assertEquals(projectToFind.getDescription(), projectResponse.getDescription());
        assertEquals(projectToFind.getInstructions(), projectResponse.getInstructions());
        assertEquals(projectToFind.getGithubUrl(), projectResponse.getGithubUrl());
        assertEquals(projectToFind.getStatus().getName(), projectResponse.getStatus());
        assertEquals(projectToFind.getCreatedAt(), projectResponse.getCreatedAt());
        assertEquals(projectToFind.getUpdatedAt(), projectResponse.getUpdatedAt());
        assertTrue(projectResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllProjectsByStatus() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        ProjectStatusName statusName = ProjectStatusName.ACTIVE;

        var projectToFind = new Project(1L, 1L, "title", "description", "instructions", "githubUrl",
                new ProjectStatus(1L, statusName),
                timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);

        when(projectRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(projectToFind), pageable, 1));
        when(projectStatusRepository.findByName(statusName)).thenReturn(Optional.of(projectToFind.getStatus()));

        var projectPageResponse = projectService.getAllProjects(pageable, null, null, statusName);
        var projectResponse = projectPageResponse.getContent().iterator().next();

        // then
        assertNotNull(projectPageResponse);
        assertNotNull(projectPageResponse.getMetadata());
        assertEquals(1, projectPageResponse.getMetadata().getTotalElements());
        assertEquals(1, projectPageResponse.getContent().size());

        assertNotNull(projectResponse);
        assertEquals(projectToFind.getId(), projectResponse.getId());
        assertEquals(projectToFind.getOwnerId(), projectResponse.getOwnerId());
        assertEquals(projectToFind.getTitle(), projectResponse.getTitle());
        assertEquals(projectToFind.getDescription(), projectResponse.getDescription());
        assertEquals(projectToFind.getInstructions(), projectResponse.getInstructions());
        assertEquals(projectToFind.getGithubUrl(), projectResponse.getGithubUrl());
        assertEquals(statusName, projectResponse.getStatus());
        assertEquals(projectToFind.getCreatedAt(), projectResponse.getCreatedAt());
        assertEquals(projectToFind.getUpdatedAt(), projectResponse.getUpdatedAt());
        assertTrue(projectResponse.hasLinks());
    }

    @Test
    void shouldThrowNoSuchProjectStatusByNameExceptionWhenGettingAllProjectsByStatus() {
        // given
        ProjectStatusName statusName = ProjectStatusName.ACTIVE;
        Pageable pageable = PageRequest.of(0, 2);

        when(projectStatusRepository.findByName(statusName)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchProjectStatusByNameException.class, () -> projectService.getAllProjects(pageable, null, null, statusName));
    }

    private Answer1<Project, Project> getFakeSave(long id) {
        return project -> {
            project.setId(id);
            return project;
        };
    }

    private Answer1<CompletableFuture<SendResult<String, ProjectEvent>>, ProjectEvent> getFakeSendResult() {
        return event -> CompletableFuture.completedFuture(
                new SendResult<>(new ProducerRecord<>("projects", event),
                        new RecordMetadata(new TopicPartition("projects", 0), 0L, 0, 0L, 0, 0)));
    }

    private Project getNewProjectWithAllFields() {
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        ProjectStatus status = new ProjectStatus(1L, ProjectStatusName.ACTIVE);

        return new Project(1L, 1L, "title", "description", "instructions", "githubUrl",
                status, timeOfCreation, timeOfModification);
    }
}
