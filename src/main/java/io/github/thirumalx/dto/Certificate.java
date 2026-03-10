package io.github.thirumalx.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Thirumal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {

    private Long id;
    @NotNull
    private String serialNumber;
    @NotNull
    private String path; // Certificate path
    private String ownerName;// Equivalent to client name in the database
    private LocalDateTime issuedOn;
    private LocalDateTime revokedOn;
    private LocalDateTime notAfter;
    private LocalDateTime lastTimeVerifiedOn;
    @NotNull
    private String status;
    private String password;

    public boolean isRevoked() {
        return revokedOn != null;
    }

    /**
     * Checks if the certificate is valid
     * based on the condition, that it is not revoked and not expired
     * 
     * @return true if the certificate is valid
     */
    public boolean isCertificateValid() {
        return !isRevoked() && notAfter != null && LocalDateTime.now().isBefore(notAfter);
    }

    public String getFileName() {
        if (path != null && !path.isEmpty()) {
            return path.substring(path.lastIndexOf("\\") + 1);
        }
        return null;
    }

    /**
     * Checks if the certificate file is a PFX/P12 file based on its extension
     */
    public boolean ispfxCertificate() {
        String fileName = getFileName();
        return fileName != null && (fileName.toLowerCase().endsWith(".pfx") || fileName.toLowerCase().endsWith(".p12"));
    }
}
