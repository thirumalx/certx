package io.github.thirumalx.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * @author Thirumal
 */
@Data
@Builder
public class Certificate {

    private Long id;
    @NotNull
    private String serialNumber;
    @NotNull
    private String path;
    private String ownerName;// Equivalent to client name in the database
    @NotNull
    private Instant issuedOn;
    private Instant revokedOn;
    @NotNull
    private Instant notAfter;
    private Instant lastTimeVerifiedOn;
    @NotNull
    private String status;

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
        return !isRevoked() && Instant.now().isBefore(notAfter);
    }

}
