package io.github.thirumalx.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.thirumalx.dao.anchor.ApplicationAnchorDao;
import io.github.thirumalx.dao.view.ApplicationViewDao;
import io.github.thirumalx.dao.attribute.ApplicationNameAttributeDao;
import io.github.thirumalx.dao.attribute.ApplicationUniqueIdAttributeDao;
import io.github.thirumalx.dto.Application;
import io.github.thirumalx.dto.PageRequest;
import io.github.thirumalx.dto.PageResponse;
import io.github.thirumalx.model.Anchor;
import io.github.thirumalx.model.Attribute;
import io.github.thirumalx.model.anchor.ApplicationAnchor;
import jakarta.validation.Valid;

/**
 * @author Thirumal
 */
@Service
public class ApplicationService {

    Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationAnchorDao applicationAnchorDao;
    private final ApplicationNameAttributeDao applicationNameAttributeDao;
    private final ApplicationUniqueIdAttributeDao applicationUniqueIdAttributeDao;
    private final ApplicationViewDao applicationViewDao;

    public ApplicationService(ApplicationAnchorDao applicationAnchorDao,
            ApplicationNameAttributeDao applicationNameAttributeDao,
            ApplicationUniqueIdAttributeDao applicationUniqueIdAttributeDao,
            ApplicationViewDao applicationViewDao) {
        this.applicationAnchorDao = applicationAnchorDao;
        this.applicationNameAttributeDao = applicationNameAttributeDao;
        this.applicationUniqueIdAttributeDao = applicationUniqueIdAttributeDao;
        this.applicationViewDao = applicationViewDao;
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
            try {
                Map<String, Object> applicationUniqueIdAttributeId = applicationUniqueIdAttributeDao.insert(
                        applicationId,
                        application.getUniqueId(),
                        Attribute.METADATA_ACTIVE);
                logger.info("Added application uniqueId attribute with ID: {}",
                        applicationUniqueIdAttributeId.entrySet().stream().toList());
            } catch (DuplicateKeyException duplicateKeyException) {
                throw new io.github.thirumalx.exception.DuplicateKeyException("Application ID must be unique...");
            }

        }
        return application;
    }

    @Transactional
    public Application update(Long id, @Valid Application application) {
        logger.debug("Initiated Updating application {} with details {}", id, application);

        return getApplication(id);
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

    public PageResponse<Application> listApplication(PageRequest pageRequest) {
        logger.debug("Listing applications for page {} with size {}", pageRequest.page(), pageRequest.size());
        List<Application> applications = applicationViewDao.listNow(pageRequest.page(), pageRequest.size());
        long totalElements = applicationViewDao.countNow();
        int totalPages = (int) Math.ceil((double) totalElements / pageRequest.size());
        return new PageResponse<>(pageRequest.page(), pageRequest.size(), applications, totalElements, totalPages);
    }

}
