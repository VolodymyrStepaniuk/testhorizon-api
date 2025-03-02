package com.stepaniuk.testhorizon.export.handler;

import com.stepaniuk.testhorizon.payload.project.ProjectResponse;
import com.stepaniuk.testhorizon.project.ProjectService;
import com.stepaniuk.testhorizon.types.entity.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.stepaniuk.testhorizon.export.ExportUtils.safeForCsv;
import static com.stepaniuk.testhorizon.export.ExportUtils.safeForXml;
import static com.stepaniuk.testhorizon.types.entity.EntityType.PROJECT;

@Component
public class ProjectExportHandler implements EntityExportHandler<ProjectResponse> {

    private final ProjectService projectService;

    @Autowired
    public ProjectExportHandler(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public EntityType getEntityType() {
        return PROJECT;
    }

    @Override
    public ProjectResponse findById(Long id) {
        return projectService.getProjectById(id);
    }

    @Override
    public String toCsv(ProjectResponse project) {
        if (project == null) return "";

        String header = "Title,Description,Instructions,GithubUrl";

        String data = String.join(",",
                safeForCsv(project.getTitle()),
                safeForCsv(project.getDescription()),
                safeForCsv(project.getInstructions()),
                safeForCsv(project.getGithubUrl())
        );

        return header + "\n" + data;
    }

    @Override
    public String toXml(ProjectResponse project) {
        if (project == null) {
            return "<project></project>";
        }

        return """
                <project>
                    <title>%s</title>
                    <description>%s</description>
                    <instructions>%s</instructions>
                    <githubUrl>%s</githubUrl>
                </project>
                """.formatted(
                safeForXml(project.getTitle()),
                safeForXml(project.getDescription()),
                safeForXml(project.getInstructions()),
                safeForXml(project.getGithubUrl())
        );
    }
}


