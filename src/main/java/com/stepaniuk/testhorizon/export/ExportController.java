package com.stepaniuk.testhorizon.export;

import com.stepaniuk.testhorizon.types.entity.EntityType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/export", produces = "application/json")
public class ExportController {

    private final EntityExportService exportService;

    @GetMapping("/{entityType}/{id}")
    public ResponseEntity<?> exportEntity(
            @PathVariable EntityType entityType,
            @PathVariable Long id,
            @RequestParam ExportFormat format
    ) {
        String fileName = entityType + "_" + id + "." + format.name().toLowerCase();

        switch (format) {
            case CSV -> {
                String csvData = exportService.exportCsv(entityType, id);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(csvData);
            }
            case XML -> {
                String xmlData = exportService.exportXml(entityType, id);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(xmlData);
            }
            default -> {
                return ResponseEntity.badRequest().body("Unsupported format: " + format);
            }
        }
    }
}
