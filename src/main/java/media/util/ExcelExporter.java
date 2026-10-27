package media.util;

import media.model.Article;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ExcelExporter {

    // ---------- Excel ----------
    public static void exportArticles(List<Article> articles, String filename) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Статьи");

            String[] headers = {"ID", "Название", "Категория", "Статус", "Автор", "Дата"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (Article a : articles) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(a.getId());
                row.createCell(1).setCellValue(a.getTitle());
                row.createCell(2).setCellValue(a.getCategory());
                row.createCell(3).setCellValue(a.getStatus().name());
                row.createCell(4).setCellValue(a.getAuthorName() != null ? a.getAuthorName() : "");
                row.createCell(5).setCellValue(a.getCreatedAt() != null ? a.getCreatedAt().toString() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream out = new FileOutputStream(filename)) {
                wb.write(out);
            }
        }
    }

    // ---------- CSV ----------
    public static void exportArticlesToCsv(List<Article> articles, String filename) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("ID;Название;Категория;Статус;Автор;Дата");
            for (Article a : articles) {
                pw.printf("%d;%s;%s;%s;%s;%s%n",
                    a.getId(),
                    escapeCsv(a.getTitle()),
                    escapeCsv(a.getCategory()),
                    a.getStatus().name(),
                    escapeCsv(a.getAuthorName() != null ? a.getAuthorName() : ""),
                    a.getCreatedAt() != null ? a.getCreatedAt().toString() : ""
                );
            }
        }
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(";") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}