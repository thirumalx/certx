package io.github.thirumalx.model.anchor;

import io.github.thirumalx.model.Anchor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Thirumal M
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class NotificationAnchor implements Anchor {

    private Long id;
    private Long metadata;

}
