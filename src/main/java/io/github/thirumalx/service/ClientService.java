package io.github.thirumalx.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.thirumalx.dao.anchor.ApplicationAnchorDao;
import io.github.thirumalx.dao.anchor.ClientAnchorDao;
import io.github.thirumalx.dao.attribute.ClientEmailAttributeDao;
import io.github.thirumalx.dao.attribute.ClientMobileNumberAttributeDao;
import io.github.thirumalx.dao.attribute.ClientNameAttributeDao;
import io.github.thirumalx.dao.attribute.ClientStatusAttributeDao;
import io.github.thirumalx.dao.tie.ApplicationClientTieDao;
import io.github.thirumalx.dao.view.ClientViewDao;
import io.github.thirumalx.dto.Client;
import io.github.thirumalx.dto.PageRequest;
import io.github.thirumalx.dto.PageResponse;
import io.github.thirumalx.exception.ResourceNotFoundException;
import io.github.thirumalx.model.Anchor;
import io.github.thirumalx.model.Attribute;
import io.github.thirumalx.model.Knot;
import jakarta.validation.Valid;

/**
 * @author Thirumal
 */
@Service
public class ClientService {

    Logger logger = LoggerFactory.getLogger(ClientService.class);

    // Anchors
    private final ClientAnchorDao clientAnchorDao;
    private final ApplicationAnchorDao applicationAnchorDao;
    // Attributes
    private final ClientNameAttributeDao clientNameAttributeDao;
    private final ClientEmailAttributeDao clientEmailAttributeDao;
    private final ClientMobileNumberAttributeDao clientMobileNumberAttributeDao;
    private final ClientStatusAttributeDao clientStatusAttributeDao;
    // Views
    private final ClientViewDao clientViewDao;
    // Ties
    private final ApplicationClientTieDao applicationClientTieDao;

    public ClientService(ClientAnchorDao clientAnchorDao,
            ClientNameAttributeDao clientNameAttributeDao,
            ClientEmailAttributeDao clientEmailAttributeDao,
            ClientMobileNumberAttributeDao clientMobileNumberAttributeDao,
            ClientStatusAttributeDao clientStatusAttributeDao,
            ClientViewDao clientViewDao,
            ApplicationAnchorDao applicationAnchorDao, ApplicationClientTieDao applicationClientTieDao) {
        this.clientAnchorDao = clientAnchorDao;
        this.clientNameAttributeDao = clientNameAttributeDao;
        this.clientEmailAttributeDao = clientEmailAttributeDao;
        this.clientMobileNumberAttributeDao = clientMobileNumberAttributeDao;
        this.clientStatusAttributeDao = clientStatusAttributeDao;
        this.applicationClientTieDao = applicationClientTieDao;
        this.clientViewDao = clientViewDao;
        this.applicationAnchorDao = applicationAnchorDao;
    }

    @Transactional
    public Client save(Client client) {
        logger.info("Saving client: {} for the application {}", client, client.getApplicationId());

        // Validate Application ID
        if (client.getApplicationId() == null) {
            logger.debug("Application ID is required to create a client");
            throw new IllegalArgumentException("Application ID is required to create a client");
        }

        // Check if application exists
        if (!applicationAnchorDao.existsById(client.getApplicationId())) {
            logger.error("Application with ID: {} does not exist", client.getApplicationId());
            throw new ResourceNotFoundException(
                    "Application with ID: " + client.getApplicationId() + " does not exist");
        }
        logger.debug("Application with ID: {} exists", client.getApplicationId());

        // Check for client duplication (name/email within the same application)
        List<Client> existingClients = clientViewDao.listNow(client.getApplicationId(), Knot.ACTIVE, 0,
                Integer.MAX_VALUE);
        boolean isDuplicate = existingClients.stream()
                .anyMatch(c -> (client.getName() != null && client.getName().equals(c.getName())) ||
                        (client.getEmail() != null && client.getEmail().equals(c.getEmail())));

        if (isDuplicate) {
            logger.error("Client with name: {} or email: {} already exists for application: {}",
                    client.getName(), client.getEmail(), client.getApplicationId());
            throw new IllegalArgumentException("Client with name or email already exists for this application");
        }
        logger.debug("No duplicate client found for application: {}", client.getApplicationId());

        // Create Client Anchor
        Long clientId = clientAnchorDao.insert(Anchor.METADATA_ACTIVE);
        logger.info("Created client anchor with ID: {}", clientId);
        client.setId(clientId);

        // Add Name
        clientNameAttributeDao.insert(clientId, client.getName(), Attribute.METADATA_ACTIVE);

        // Add Email
        if (client.getEmail() != null) {
            clientEmailAttributeDao.insert(clientId, client.getEmail(), Instant.now(), Attribute.METADATA_ACTIVE);
        }

        // Add Mobile Number
        if (client.getMobileNumber() != null) {
            clientMobileNumberAttributeDao.insert(clientId, client.getMobileNumber(), Instant.now(),
                    Attribute.METADATA_ACTIVE);
        }

        // Add Status (Active)
        clientStatusAttributeDao.insert(clientId, Knot.ACTIVE, Instant.now(), Attribute.METADATA_ACTIVE);
        // Create Tie between Client and Application
        applicationClientTieDao.insertHistorized(client.getApplicationId(), clientId, Attribute.METADATA_ACTIVE,
                Instant.now());
        logger.info("Client created successfully with ID: {} for application: {}", clientId, client.getApplicationId());
        return client;
    }

    @Transactional
    public Client update(Long applicationId, Long id, @Valid Client client) {
        logger.debug("Initiated Updating client {} with details {}", id, client);
        Client existingClient = getClient(applicationId, id);
        if (existingClient == null) {
            logger.debug("Client with ID: {} not found for update", id);
            throw new ResourceNotFoundException("Client not found for update");
        }
        client.setId(id);
        if (client.equals(existingClient)) {
            logger.debug("No changes detected for client with ID: {}", id);
            throw new IllegalArgumentException("No changes detected to update");
        }
        // Update Name
        if (client.getName() != null && !client.getName().equals(existingClient.getName())) {
            clientNameAttributeDao.deleteByApplicationId(id);
            clientNameAttributeDao.insert(id, client.getName(), Attribute.METADATA_ACTIVE);
        }
        // Update Email
        if (client.getEmail() != null && !client.getEmail().equals(existingClient.getEmail())) {
            clientEmailAttributeDao.insert(id, client.getEmail(), Instant.now(), Attribute.METADATA_ACTIVE);
        }
        // Update Mobile Number
        if (client.getMobileNumber() != null && !client.getMobileNumber().equals(existingClient.getMobileNumber())) {
            clientMobileNumberAttributeDao.insert(id, client.getMobileNumber(), Instant.now(),
                    Attribute.METADATA_ACTIVE);
        }
        return getClient(applicationId, id);
    }

    public Client getClient(Long applicationId, Long id) {
        logger.info("Fetching client with ID: {} for application: {}", id, applicationId);
        Optional<Client> clientOptional = clientViewDao.findNowById(id);
        if (clientOptional.isEmpty()) {
            logger.debug("Client with ID: {} not found", id);
            return null;
        }
        return clientOptional.get();
    }

    public PageResponse<Client> listClient(Long applicationId, PageRequest pageRequest) {
        logger.debug("Listing clients for the application {} from page {} with size {}", applicationId,
                pageRequest.page(), pageRequest.size());
        List<Client> clients = clientViewDao.listNow(applicationId, Knot.ACTIVE, pageRequest.page(),
                pageRequest.size());
        long totalElements = clientViewDao.countNow(applicationId, Knot.ACTIVE);
        int totalPages = (int) Math.ceil((double) totalElements / pageRequest.size());
        return new PageResponse<>(pageRequest.page(), pageRequest.size(), clients, totalElements, totalPages);
    }

    public boolean deleteClient(Long applicationId, Long id) {
        logger.info("Deleting client with ID: {} for application: {}", id, applicationId);
        Client existingClient = getClient(applicationId, id);
        if (existingClient == null) {
            logger.debug("Client with ID: {} not found for deletion", id);
            throw new ResourceNotFoundException("Client not found for deletion");
        }
        clientStatusAttributeDao.insert(
                id,
                Knot.DELETED,
                Instant.now(),
                Attribute.METADATA_ACTIVE);
        return true;
    }

}
