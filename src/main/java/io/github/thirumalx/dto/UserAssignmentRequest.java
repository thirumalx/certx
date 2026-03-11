package io.github.thirumalx.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Thirumal
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAssignmentRequest {
    private String email;
    private String name;
    private String mobileNumber;
    private String userId;
}
