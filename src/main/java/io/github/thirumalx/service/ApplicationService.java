package io.github.thirumalx.service;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.thirumalx.dao.anchor.ApplicationAnchorDao;
import io.github.thirumalx.dao.attribute.ApplicationNameAttributeDao;
import io.github.thirumalx.dto.Application;
import io.github.thirumalx.model.Anchor;
import io.github.thirumalx.model.Attribute;
import io.github.thirumalx.model.anchor.ApplicationAnchor;
import io.github.thirumalx.model.attribute.ApplicationNameAttribute;

/**
 * @author Thirumal
 */
@Service
public class ApplicationService {

    Logger logger = LoggerFactory.getLogger(ApplicationService.class);
    
    private final ApplicationAnchorDao applicationAnchorDao;
    private final ApplicationNameAttributeDao applicationNameAttributeDao;

    public ApplicationService(ApplicationAnchorDao applicationAnchorDao, ApplicationNameAttributeDao applicationNameAttributeDao) {
        this.applicationAnchorDao = applicationAnchorDao;
        this.applicationNameAttributeDao = applicationNameAttributeDao;
    }

    @Transactional
    public Application save(Application application) {
        logger.info("Saving application: {}", application);
        //Create Applicaiton Anchor
        Long applicationId = applicationAnchorDao.insert(Anchor.METADATA_ACTIVE);
        logger.info("Created application anchor with ID: {}", applicationId);
        application.setId(applicationId);
        //Add Name
        Long applicationNameAttributeId = applicationNameAttributeDao.insert(
            applicationId,
            application.getApplicationName(),
            Instant.now(),
            Attribute.METADATA_ACTIVE
        );
        logger.info("Added application name attribute with ID: {}", applicationNameAttributeId);
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
