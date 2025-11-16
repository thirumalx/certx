package io.github.thirumalx.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * @author Thirumal
 */
@Data
public class Certificate {

    private String serialNumber;
    private String path;
    private String ownerName;// Equivalent to client name in the database
    private LocalDateTime issuedOn;
    private LocalDateTime revokedOn;
    
    public boolean isRevoked() {
        return revokedOn != null;
    }   

}
