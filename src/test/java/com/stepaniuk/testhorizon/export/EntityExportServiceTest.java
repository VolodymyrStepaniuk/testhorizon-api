package com.stepaniuk.testhorizon.export;

import com.stepaniuk.testhorizon.export.exceptions.NoSuchHandlerFoundForEntity;
import com.stepaniuk.testhorizon.export.handler.EntityExportHandler;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.payload.project.ProjectResponse;
import com.stepaniuk.testhorizon.testspecific.ServiceLevelUnitTest;
import com.stepaniuk.testhorizon.types.entity.EntityType;
import com.stepaniuk.testhorizon.types.project.ProjectStatusName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ServiceLevelUnitTest
class EntityExportServiceTest {

    private EntityExportService entityExportService;

    @MockitoBean
    private EntityExportHandler<ProjectResponse> projectExportHandler;

    @BeforeEach
    void setUp() {
        when(projectExportHandler.getEntityType()).thenReturn(EntityType.PROJECT);
        entityExportService = new EntityExportService(List.of(projectExportHandler));
    }

    @Test
    void shouldReturnCsvWhenExportingEntity() {
        // given
        var timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        var timeOfModification = Instant.now().plus(Duration.ofHours(20));

        var projectResponse = new ProjectResponse(
                2L,
                new UserInfo(1L, "firstName", "lastName"),
                "title",
                "description",
                "instructions",
                "https://github.com",
                ProjectStatusName.ACTIVE,
                timeOfCreation,
                timeOfModification
        );

        when(projectExportHandler.getEntityType()).thenReturn(EntityType.PROJECT);
        when(projectExportHandler.findById(1L)).thenReturn(projectResponse);
        when(projectExportHandler.toCsv(projectResponse)).thenReturn("some,csv,line");

        // when
        String csvResult = entityExportService.exportCsv(EntityType.PROJECT, 1L);

        // then
        assertNotNull(csvResult);
        assertEquals("some,csv,line", csvResult);

        verify(projectExportHandler, times(1)).findById(1L);
        verify(projectExportHandler, times(1)).toCsv(projectResponse);
    }

    @Test
    void shouldReturnXmlWhenExportingEntity() {
        // given
        var timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        var timeOfModification = Instant.now().plus(Duration.ofHours(20));

        var projectResponse = new ProjectResponse(
                2L,
                new UserInfo(1L, "firstName", "lastName"),
                "title",
                "description",
                "instructions",
                "https://github.com",
                ProjectStatusName.ACTIVE,
                timeOfCreation,
                timeOfModification
        );

        when(projectExportHandler.getEntityType()).thenReturn(EntityType.PROJECT);
        when(projectExportHandler.findById(2L)).thenReturn(projectResponse);
        when(projectExportHandler.toXml(projectResponse)).thenReturn("<xml>data</xml>");

        // when
        String xmlResult = entityExportService.exportXml(EntityType.PROJECT, 2L);

        // then
        assertNotNull(xmlResult);
        assertEquals("<xml>data</xml>", xmlResult);

        verify(projectExportHandler, times(1)).findById(2L);
        verify(projectExportHandler, times(1)).toXml(projectResponse);
    }

    @Test
    void shouldThrowNoSuchHandlerFoundForEntityWhenExportingToCsv() {
        // given
        when(projectExportHandler.getEntityType()).thenReturn(EntityType.PROJECT);

        // when & then
        assertThrows(NoSuchHandlerFoundForEntity.class,
                () -> entityExportService.exportCsv(EntityType.BUG_REPORT, 1L));
    }

    @Test
    void shouldThrowNoSuchHandlerFoundForEntityWhenExportingToXml() {
        // given
        when(projectExportHandler.getEntityType()).thenReturn(EntityType.PROJECT);

        // when & then
        assertThrows(NoSuchHandlerFoundForEntity.class,
                () -> entityExportService.exportXml(EntityType.TEST, 10L));
    }

    @Test
    void shouldHandleNullEntityWhenExportingToCsv() {
        // given
        when(projectExportHandler.getEntityType()).thenReturn(EntityType.PROJECT);

        when(projectExportHandler.findById(100L)).thenReturn(null);

        when(projectExportHandler.toCsv(null)).thenReturn("");

        // when
        String csvResult = entityExportService.exportCsv(EntityType.PROJECT, 100L);

        // then
        assertNotNull(csvResult);
        assertEquals("", csvResult);

        verify(projectExportHandler).findById(100L);
        verify(projectExportHandler).toCsv(null);
    }

    @Test
    void shouldHandleNullEntityWhenExportingToXml() {
        // given
        when(projectExportHandler.getEntityType()).thenReturn(EntityType.PROJECT);

        when(projectExportHandler.findById(100L)).thenReturn(null);

        when(projectExportHandler.toXml(null)).thenReturn("");

        // when
        String xmlResult = entityExportService.exportXml(EntityType.PROJECT, 100L);

        // then
        assertNotNull(xmlResult);
        assertEquals("", xmlResult);

        verify(projectExportHandler).findById(100L);
        verify(projectExportHandler).toXml(null);
    }
}
