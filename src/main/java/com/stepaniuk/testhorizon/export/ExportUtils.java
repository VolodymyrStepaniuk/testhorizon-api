package com.stepaniuk.testhorizon.export;

public final class ExportUtils {

    private ExportUtils() {
    }

    public static String safeForCsv(String input) {
        if (input == null) return "";
        return input
                .replace(",", ";")
                .replace("\"", "\"\"");
    }

    public static String safeForXml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}

