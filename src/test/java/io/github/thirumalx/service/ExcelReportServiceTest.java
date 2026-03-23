package io.github.thirumalx.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.thirumalx.service.ExcelReportService.CertificateReportItem;

class ExcelReportServiceTest {

    @Test
    void testGenerateReport() throws IOException {
        ExcelReportService service = new ExcelReportService();
        List<CertificateReportItem> items = List.of(
                CertificateReportItem.builder()
                        .category("Expired Today")
                        .certificateId(12L)
                        .clientId(2L)
                        .expiryDate(LocalDateTime.now())
                        .build(),
                CertificateReportItem.builder()
                        .category("Revoked Today")
                        .certificateId(6L)
                        .clientId(2L)
                        .revokedOn(LocalDateTime.now())
                        .build());

        byte[] report = service.generateReport(items);

        assertNotNull(report);
        assertTrue(report.length > 0);
        // The first few bytes of a .xlsx file (ZIP format) are 50 4B 03 04
        assertEquals((byte) 0x50, report[0]);
        assertEquals((byte) 0x4B, report[1]);
    }

    private void assertEquals(byte expected, byte actual) {
        if (expected != actual) {
            throw new AssertionError("Expected " + expected + " but was " + actual);
        }
    }
}
