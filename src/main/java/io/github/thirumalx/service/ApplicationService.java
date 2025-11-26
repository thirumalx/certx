package io.github.thirumalx.service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.thirumalx.dao.anchor.ApplicationAnchorDao;
import io.github.thirumalx.dao.attribute.ApplicationNameAttributeDao;
import io.github.thirumalx.dao.attribute.ApplicationUniqueIdAttributeDao;
import io.github.thirumalx.dto.Application;
import io.github.thirumalx.model.Anchor;
import io.github.thirumalx.model.Attribute;
import io.github.thirumalx.model.anchor.ApplicationAnchor;

/**
 * @author Thirumal
 */
@Service
public class ApplicationService {

    Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationAnchorDao applicationAnchorDao;
    private final ApplicationNameAttributeDao applicationNameAttributeDao;
    private final ApplicationUniqueIdAttributeDao applicationUniqueIdAttributeDao;

    public ApplicationService(ApplicationAnchorDao applicationAnchorDao,
            ApplicationNameAttributeDao applicationNameAttributeDao,
            ApplicationUniqueIdAttributeDao applicationUniqueIdAttributeDao) {
        this.applicationAnchorDao = applicationAnchorDao;
        this.applicationNameAttributeDao = applicationNameAttributeDao;
        this.applicationUniqueIdAttributeDao = applicationUniqueIdAttributeDao;
    }

    @Transactional
    public Application save(Application application) {
        logger.info("Saving application: {}", application);
        // Create Applicaiton Anchor
        Long applicationId = applicationAnchorDao.insert(Anchor.METADATA_ACTIVE);
        logger.info("Created application anchor with ID: {}", applicationId);
        application.setId(applicationId);
        // Add Name
        Map<String, Object> applicationNameAttributeId = applicationNameAttributeDao.insert(
                applicationId,
                application.getApplicationName(),
                Instant.now(),
                Attribute.METADATA_ACTIVE);
        logger.info("Added application name attribute with ID: {}",
                applicationNameAttributeId.entrySet().stream().toList());
        // Add UniqueId
        if (application.getUniqueId() != null) {
            Map<String, Object> applicationUniqueIdAttributeId = applicationUniqueIdAttributeDao.insert(
                    applicationId,
                    application.getUniqueId(),
                    Attribute.METADATA_ACTIVE);
            logger.info("Added application uniqueId attribute with ID: {}",
                    applicationUniqueIdAttributeId.entrySet().stream().toList());
        }
        return application;
    }

    public Application getApplication(Long id) {
        logger.info("Fetching application with ID: {}", id);
        Optional<ApplicationAnchor> applicationAnchor = applicationAnchorDao.findById(id);
        if (applicationAnchor.isEmpty()) {
            logger.debug("Application with ID: {} not found", id);
            return null;
        }
        return Application.builder().id(applicationAnchor.get().getId()).build();
    }
}
