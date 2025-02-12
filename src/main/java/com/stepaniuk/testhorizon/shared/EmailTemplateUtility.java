package com.stepaniuk.testhorizon.shared;

import com.stepaniuk.testhorizon.shared.exceptions.FailedToLoadEmailTemplateException;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class EmailTemplateUtility {

    private EmailTemplateUtility() {
        throw new IllegalStateException("Utility class");
    }

    public static String loadEmailTemplate(String templateName, Map<String, String> params) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/" + templateName);
            String template = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            for (Map.Entry<String, String> entry : params.entrySet()) {
                template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }

            return template;
        } catch (Exception e) {
            throw new FailedToLoadEmailTemplateException(e);
        }
    }
}
