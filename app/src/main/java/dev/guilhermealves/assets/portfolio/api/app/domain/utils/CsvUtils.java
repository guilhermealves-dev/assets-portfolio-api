package dev.guilhermealves.assets.portfolio.api.app.domain.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CsvUtils {

    /**
     * Escapa um valor para o padrão RFC 4180 CSV.
     */
    public static String escapeCsv(Object value) {
        if (value == null) {
            return "";
        }
        String str = value.toString();
        if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }

    /**
     * Faz o parsing de uma única linha CSV respeitando aspas e aspas duplas escapadas.
     */
    public static List<String> parseLine(String line) {
        if (line == null) {
            return Collections.emptyList();
        }

        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"');
                    i++; // Pula o segundo caractere de aspas escapadas
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens;
    }

    /**
     * Lê um fluxo CSV completo e retorna uma lista de registros mapeados pelo nome da coluna em minúsculo.
     */
    public static List<Map<String, String>> parseCsv(InputStream inputStream) throws Exception {
        List<Map<String, String>> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                return records;
            }

            // Remove possível BOM UTF-8 no início do arquivo
            if (headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }

            List<String> headers = parseLine(headerLine);
            List<String> normalizedHeaders = new ArrayList<>();
            for (String h : headers) {
                normalizedHeaders.add(normalizeKey(h));
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> values = parseLine(line);
                Map<String, String> row = new LinkedHashMap<>();

                for (int i = 0; i < normalizedHeaders.size(); i++) {
                    String colName = normalizedHeaders.get(i);
                    String val = (i < values.size()) ? values.get(i) : "";
                    row.put(colName, val);
                }
                records.add(row);
            }
        }

        return records;
    }

    private static String normalizeKey(String key) {
        if (key == null) return "";
        return key.trim()
                .toLowerCase()
                .replace("_", "")
                .replace("-", "")
                .replace(" ", "")
                .replace("ç", "c")
                .replace("ã", "a")
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u");
    }
}
