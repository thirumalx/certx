package io.github.thirumalx.model.attribute;

import java.time.Instant;

import io.github.thirumalx.model.HistorizedAttribute;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
/**
 * @author Thirumal M
 */
@Data
@Builder
@AllArgsConstructor
public class ApplicationNameAttribute implements HistorizedAttribute<String> {
    
    private Long apNamApId;
    private String apNamApplicationName;
    private Instant apNamChangedAt;
    private  Long metadataApNam;
    
    @Override
    public Long getAnchorId() {
        return apNamApId;
    }

    @Override
    public String getValue() {
        return apNamApplicationName;
    }
    
    @Override
    public Long getMetadataId() {
        return metadataApNam;
    }

    @Override
    public Instant changedAt() {
        return apNamChangedAt;
    }    

}
