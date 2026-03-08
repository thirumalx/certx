package io.github.thirumalx.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Thirumal M
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    private Long id;
    private LocalDateTime sentAt;
    private int remainderCount;
    private Long certificateId;
    private Long clientId;

}
