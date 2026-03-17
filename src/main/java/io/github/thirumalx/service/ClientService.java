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
import io.github.thirumalx.dao.anchor.UserAnchorDao;
import io.github.thirumalx.dao.attribute.ClientEmailAttributeDao;
import io.github.thirumalx.dao.attribute.ClientMobileNumberAttributeDao;
import io.github.thirumalx.dao.attribute.ClientNameAttributeDao;
import io.github.thirumalx.dao.attribute.ClientStatusAttributeDao;
import io.github.thirumalx.dao.attribute.ClientUniqueIdAttributeDao;
import io.github.thirumalx.dao.attribute.UserEmailAttributeDao;
import io.github.thirumalx.dao.attribute.UserIdAttributeDao;
import io.github.thirumalx.dao.attribute.UserMobileNumberAttributeDao;
import io.github.thirumalx.dao.attribute.UserNameAttributeDao;
import io.github.thirumalx.dao.tie.ApplicationClientTieDao;
import io.github.thirumalx.dao.tie.UserClientAssignedTieDao;
import io.github.thirumalx.dao.view.ClientViewDao;
import io.github.thirumalx.dto.Client;
import io.github.thirumalx.dto.PageRequest;
import io.github.thirumalx.dto.PageResponse;
import io.github.thirumalx.dto.User;
import io.github.thirumalx.dto.UserAssignmentRequest;
import io.github.thirumalx.exception.DuplicateKeyException;
import io.github.thirumalx.exception.ResourceNotFoundException;
import io.github.thirumalx.model.Anchor;
import io.github.thirumalx.model.Attribute;
import io.github.thirumalx.model.Knot;
import jakarta.validation.Valid;
import io.github.thirumalx.dao.view.UserViewDao;
import java.util.HashMap;
import java.util.Map;

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
    private final ClientUniqueIdAttributeDao clientUniqueIdAttributeDao;
    private final UserIdAttributeDao userIdAttributeDao;
    private final UserNameAttributeDao userNameAttributeDao;
    private final UserMobileNumberAttributeDao userMobileNumberAttributeDao;
    private final UserEmailAttributeDao userEmailAttributeDao;
    // Views
    private final ClientViewDao clientViewDao;
    private final UserViewDao userViewDao;
    // Anchors
    private final UserAnchorDao userAnchorDao;
    // Ties
    private final ApplicationClientTieDao applicationClientTieDao;
    private final UserClientAssignedTieDao userClientAssignedTieDao;
    private final MailService mailService;

    public ClientService(ClientAnchorDao clientAnchorDao,
            ClientNameAttributeDao clientNameAttributeDao,
            ClientEmailAttributeDao clientEmailAttributeDao,
            ClientMobileNumberAttributeDao clientMobileNumberAttributeDao,
            ClientStatusAttributeDao clientStatusAttributeDao,
            ClientUniqueIdAttributeDao clientUniqueIdAttributeDao,
            ClientViewDao clientViewDao,
            ApplicationAnchorDao applicationAnchorDao, ApplicationClientTieDao applicationClientTieDao,
            UserViewDao userViewDao, UserClientAssignedTieDao userClientAssignedTieDao, MailService mailService,
            UserAnchorDao userAnchorDao, UserIdAttributeDao userIdAttributeDao,
            UserNameAttributeDao userNameAttributeDao, UserMobileNumberAttributeDao userMobileNumberAttributeDao,
            UserEmailAttributeDao userEmailAttributeDao) {
        this.clientAnchorDao = clientAnchorDao;
        this.clientNameAttributeDao = clientNameAttributeDao;
        this.clientEmailAttributeDao = clientEmailAttributeDao;
        this.clientMobileNumberAttributeDao = clientMobileNumberAttributeDao;
        this.clientStatusAttributeDao = clientStatusAttributeDao;
        this.clientUniqueIdAttributeDao = clientUniqueIdAttributeDao;
        this.userIdAttributeDao = userIdAttributeDao;
        this.userNameAttributeDao = userNameAttributeDao;
        this.userMobileNumberAttributeDao = userMobileNumberAttributeDao;
        this.userEmailAttributeDao = userEmailAttributeDao;
        this.applicationClientTieDao = applicationClientTieDao;
        this.clientViewDao = clientViewDao;
        this.applicationAnchorDao = applicationAnchorDao;
        this.userViewDao = userViewDao;
        this.userClientAssignedTieDao = userClientAssignedTieDao;
        this.mailService = mailService;
        this.userAnchorDao = userAnchorDao;
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

        String uniqueId = normalizeText(client.getUniqueId());
        if (uniqueId != null && uniqueId.length() > 15) {
            throw new IllegalArgumentException("Client uniqueId must be at most 15 characters");
        }
        client.setUniqueId(uniqueId);

        if (uniqueId != null) {
            Optional<Client> existingByUniqueId = clientViewDao.findNowByUniqueId(uniqueId);
            if (existingByUniqueId.isPresent()) {
                Client existing = existingByUniqueId.get();
                if (!applicationClientTieDao.existsAssignment(client.getApplicationId(), existing.getId())) {
                    applicationClientTieDao.insertHistorized(client.getApplicationId(), existing.getId(),
                            Attribute.METADATA_ACTIVE, Instant.now());
                }
                return getClient(client.getApplicationId(), existing.getId());
            }
        }

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

        if (normalizeText(client.getName()) == null) {
            logger.debug("Client name is required to create a client");
            throw new IllegalArgumentException("Client name is required to create a client");
        }

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

        // Add UniqueId (if provided)
        if (uniqueId != null) {
            clientUniqueIdAttributeDao.insert(clientId, uniqueId, Attribute.METADATA_ACTIVE);
        }
        // Create Tie between Client and Application
        applicationClientTieDao.insertHistorized(client.getApplicationId(), clientId, Attribute.METADATA_ACTIVE,
                Instant.now());

        logger.info("Client created successfully with ID: {} for application: {}", clientId, client.getApplicationId());
        return getClient(client.getApplicationId(), clientId);
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
        String uniqueId = normalizeText(client.getUniqueId());
        if (uniqueId != null && uniqueId.length() > 15) {
            throw new IllegalArgumentException("Client uniqueId must be at most 15 characters");
        }
        client.setUniqueId(uniqueId);
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
        // Update UniqueId
        if (uniqueId != null && !uniqueId.equals(existingClient.getUniqueId())) {
            Optional<Client> existingByUniqueId = clientViewDao.findNowByUniqueId(uniqueId);
            if (existingByUniqueId.isPresent() && !existingByUniqueId.get().getId().equals(id)) {
                throw new DuplicateKeyException("Client uniqueId must be unique");
            }
            clientUniqueIdAttributeDao.deleteByApplicationId(id);
            clientUniqueIdAttributeDao.insert(id, uniqueId, Attribute.METADATA_ACTIVE);
        }
        // Update Status
        if (client.getStatus() != null && !client.getStatus().equals(existingClient.getStatus())) {
            clientStatusAttributeDao.insert(id, client.getStatus(), Instant.now(), Attribute.METADATA_ACTIVE);
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

    public Optional<Client> findByUniqueId(String uniqueId) {
        String normalized = normalizeText(uniqueId);
        if (normalized == null) {
            return Optional.empty();
        }
        return clientViewDao.findNowByUniqueId(normalized);
    }

    public PageResponse<Client> listClient(Long applicationId, PageRequest pageRequest, String statusString) {
        logger.debug("Listing clients for the application {} with status {} from page {} with size {}", applicationId,
                statusString,
                pageRequest.page(), pageRequest.size());
        Long status = Knot.getIdFromDescription(statusString);
        List<Client> clients = clientViewDao.listNow(applicationId, status, pageRequest.page(),
                pageRequest.size());
        long totalElements = clientViewDao.countNow(applicationId, status);
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

    public java.util.List<User> listAssignedUsers(Long clientId) {
        return userViewDao.listAssignedToClient(clientId);
    }

    @Transactional
    public User assignUser(Long clientId, UserAssignmentRequest request) {
        String normalized = normalizeEmail(request.getEmail());
        if (normalized == null) {
            throw new IllegalArgumentException("Assigned user email is required");
        }
        User user = userViewDao.findNowByEmail(normalized)
                .orElseGet(() -> createUser(request));
        if (!userClientAssignedTieDao.existsAssignment(user.getId(), clientId)) {
            userClientAssignedTieDao.insertHistorized(user.getId(), clientId, Attribute.METADATA_ACTIVE,
                    Instant.now());
            notifyAssignedUser(user, clientId);
        }
        return user;
    }

    @Transactional
    public User updateAssignedUser(Long clientId, Long userId, UserAssignmentRequest request) {
        removeAssignedUser(clientId, userId);
        return assignUser(clientId, request);
    }

    @Transactional
    public boolean removeAssignedUser(Long clientId, Long userId) {
        return userClientAssignedTieDao.deleteAssignment(userId, clientId);
    }

    private void notifyAssignedUser(User user, Long clientId) {
        if (user.getEmail() == null) {
            return;
        }
        Client client = clientViewDao.findNowById(clientId).orElse(null);
        Map<String, Object> model = new HashMap<>();
        model.put("userName", user.getName());
        model.put("clientName", client != null ? client.getName() : "");
        model.put("clientEmail", client != null ? client.getEmail() : "");
        try {
            mailService.sendEmail(user.getEmail(),
                    "Client Assignment - " + (client != null ? client.getName() : "Client"),
                    "client-assignment-notification.ftl", model);
        } catch (Exception e) {
            logger.error("Failed to send assignment email to user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private User createUser(UserAssignmentRequest request) {
        String name = normalizeText(request.getName());
        String email = normalizeEmail(request.getEmail());
        String mobileNumber = normalizeText(request.getMobileNumber());
        if (name == null || email == null || mobileNumber == null) {
            throw new IllegalArgumentException("Assigned user with email " + email
                    + " not found. Provide name and mobile number to create.");
        }
        Long userId = userAnchorDao.insert(Anchor.METADATA_ACTIVE);
        Instant now = Instant.now();
        String userIdentifier = normalizeText(request.getUserId());
        if (userIdentifier == null) {
            userIdentifier = email;
        }
        if (userIdentifier.length() > 75) {
            userIdentifier = userIdentifier.substring(0, 75);
        }
        userIdAttributeDao.insert(userId, userIdentifier, now, Attribute.METADATA_ACTIVE);
        userNameAttributeDao.insert(userId, name, now, Attribute.METADATA_ACTIVE);
        userMobileNumberAttributeDao.insert(userId, mobileNumber, now, Attribute.METADATA_ACTIVE);
        userEmailAttributeDao.insert(userId, email, now, Attribute.METADATA_ACTIVE);
        return userViewDao.findNowById(userId)
                .orElseThrow(() -> new IllegalStateException("Failed to create user"));
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
