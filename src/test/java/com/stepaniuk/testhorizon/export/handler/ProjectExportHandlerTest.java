package com.stepaniuk.testhorizon.export.handler;

import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.payload.project.ProjectResponse;
import com.stepaniuk.testhorizon.project.ProjectService;
import com.stepaniuk.testhorizon.types.entity.EntityType;
import com.stepaniuk.testhorizon.types.project.ProjectStatusName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectExportHandlerTest {

    @Mock
    private ProjectService projectService;

    private ProjectExportHandler projectExportHandler;

    @BeforeEach
    void setUp() {
        projectExportHandler = new ProjectExportHandler(projectService);
    }

    @Test
    void getEntityTypeShouldReturnPROJECT() {
        // Перевірка, що getEntityType() == EntityType.PROJECT
        assertEquals(EntityType.PROJECT, projectExportHandler.getEntityType());
    }

    @Test
    void findByIdShouldCallProjectServiceAndReturnProjectResponse() {
        // given
        ProjectResponse projectResponse = new ProjectResponse(
                1L, new UserInfo(1L,"firstName","lastName"),"MyTitle", "Desc", "Instr", "https://github.com/example",
                ProjectStatusName.ACTIVE, Instant.now(), Instant.now()
        );
        when(projectService.getProjectById(10L)).thenReturn(projectResponse);

        // when
        ProjectResponse actual = projectExportHandler.findById(10L);

        // then
        assertNotNull(actual);
        assertSame(projectResponse, actual);
        verify(projectService).getProjectById(10L);
    }

    @Test
    void toCsvShouldReturnEmptyStringIfProjectIsNull() {
        // when
        String result = projectExportHandler.toCsv(null);

        // then
        assertEquals("", result);
    }

    @Test
    void toCsvShouldReturnHeaderAndData() {
        // given
        ProjectResponse project = new ProjectResponse(
                1L,  new UserInfo(1L,"firstName","lastName"),"MyTitle", "Desc", "Instr", "https://github.com/example",
                ProjectStatusName.ACTIVE, Instant.now(), Instant.now()
        );

        // when
        String csv = projectExportHandler.toCsv(project);

        // then
        // csv має два рядки: 1) заголовки, 2) дані
        String[] lines = csv.split("\n");
        assertEquals(2, lines.length);

        assertEquals("Title,Description,Instructions,GithubUrl", lines[0]); // заголовок

        // Дані
        String dataLine = lines[1];
        assertTrue(dataLine.contains("MyTitle"));
        assertTrue(dataLine.contains("Desc"));
        assertTrue(dataLine.contains("Instr"));
        assertTrue(dataLine.contains("https://github.com/example"));
    }

    @Test
    void toXmlShouldReturnEmptyProjectWhenNull() {
        // when
        String xml = projectExportHandler.toXml(null);

        // then
        assertEquals("<project></project>", xml);
    }

    @Test
    void toXmlShouldReturnCorrectXml() {
        // given
        ProjectResponse project = new ProjectResponse(
                1L,  new UserInfo(1L,"firstName","lastName"),"XML Title", "Some Desc", "XML Instr", "https://github.com/xml",
                ProjectStatusName.ACTIVE, Instant.now(), Instant.now()
        );

        // when
        String xml = projectExportHandler.toXml(project);

        // then
        assertTrue(xml.contains("<project>"));
        assertTrue(xml.contains("</project>"));
        assertTrue(xml.contains("<title>XML Title</title>"));
        assertTrue(xml.contains("<description>Some Desc</description>"));
        assertTrue(xml.contains("<instructions>XML Instr</instructions>"));
        assertTrue(xml.contains("<githubUrl>https://github.com/xml</githubUrl>"));
    }
}