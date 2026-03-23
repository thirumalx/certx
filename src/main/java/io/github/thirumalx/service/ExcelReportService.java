package io.github.thirumalx.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.Builder;

/**
 * Service for generating Excel reports for certificate expiry and revocation.
 * 
 * @author Thirumal M
 */
@Service
public class ExcelReportService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelReportService.class);

    @Builder
    public record CertificateReportItem(
            String category,
            String serialNumber,
            Long clientId,
            String clientName, // OwnerName
            String applicationName,
            String clientEmail,
            String clientPhone,
            LocalDateTime expiryDate,
            LocalDateTime revokedOn) {
    }

    public byte[] generateReport(List<CertificateReportItem> items) throws IOException {
        logger.info("Generating Excel report with {} items", items.size());
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Certificate Report");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            String[] columns = { "Category", "Serial Number", "Client Name", "Application", "Email", "Phone", "Expiry Date", "Revoked On" };
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill Data Rows
            int rowNum = 1;
            for (CertificateReportItem item : items) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.category());
                row.createCell(1).setCellValue(item.serialNumber());
                row.createCell(2).setCellValue(item.clientName());
                row.createCell(3).setCellValue(item.applicationName());
                row.createCell(4).setCellValue(item.clientEmail());
                row.createCell(5).setCellValue(item.clientPhone());
                row.createCell(6).setCellValue(item.expiryDate() == null ? "" : item.expiryDate().toString());
                row.createCell(7).setCellValue(item.revokedOn() == null ? "" : item.revokedOn().toString());
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
