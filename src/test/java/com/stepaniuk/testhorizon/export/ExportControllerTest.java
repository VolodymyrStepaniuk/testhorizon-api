package com.stepaniuk.testhorizon.export;

import com.stepaniuk.testhorizon.export.exceptions.NoSuchHandlerFoundForEntity;
import com.stepaniuk.testhorizon.project.exceptions.NoSuchProjectByIdException;
import com.stepaniuk.testhorizon.security.config.JwtAuthFilter;
import com.stepaniuk.testhorizon.testspecific.ControllerLevelUnitTest;
import com.stepaniuk.testhorizon.types.entity.EntityType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerLevelUnitTest(controllers = ExportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExportControllerTest {
    @MockitoBean
    private EntityExportService entityExportService;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void shouldReturnCsvWhenExportingEntity() throws Exception {
        // given
        when(entityExportService.exportCsv(EntityType.PROJECT, 1L)).thenReturn("some,csv,line");

        mockMvc.perform(get("/export/PROJECT/1")
                        .param("format", "CSV"))
                .andExpect(status().isOk())
                .andExpect(content().string("some,csv,line"));

        verify(entityExportService, times(1)).exportCsv(EntityType.PROJECT, 1L);
    }

    @Test
    void shouldReturnXmlWhenExportingEntity() throws Exception {
        // given
        when(entityExportService.exportXml(EntityType.PROJECT, 2L)).thenReturn("<xml>some data</xml>");

        // when & then
        mockMvc.perform(get("/export/PROJECT/2")
                        .param("format", "XML"))
                .andExpect(status().isOk())
                .andExpect(content().string("<xml>some data</xml>"));

        verify(entityExportService, times(1)).exportXml(EntityType.PROJECT, 2L);
    }

    @Test
    void shouldReturn404WhenNoHandlerFound() throws Exception {
        // given
        doThrow(new NoSuchHandlerFoundForEntity(EntityType.BUG_REPORT))
                .when(entityExportService).exportCsv(EntityType.BUG_REPORT, 10L);

        // when & then
        mockMvc.perform(get("/export/BUG_REPORT/10")
                        .param("format", "CSV"))
                .andExpect(status().isNotFound());

        verify(entityExportService, times(1)).exportCsv(EntityType.BUG_REPORT, 10L);
    }

    @Test
    void shouldReturnBadRequestWhenFormatIsUnsupported() throws Exception {
        // given
        // when & then
        mockMvc.perform(get("/export/PROJECT/1")
                        .param("format", "JSON")) // Format "JSON" не підтримується
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenFormatIsMissing() throws Exception {
        // given

        // when & then
        mockMvc.perform(get("/export/PROJECT/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenEntityNotFound() throws Exception {
        doThrow(new NoSuchProjectByIdException(1L))
                .when(entityExportService).exportCsv(EntityType.PROJECT, 1L);

        mockMvc.perform(get("/export/PROJECT/1").param("format", "CSV"))
                .andExpect(status().isNotFound());

        verify(entityExportService).exportCsv(EntityType.PROJECT, 1L);
    }

}
