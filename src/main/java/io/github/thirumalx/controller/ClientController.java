package io.github.thirumalx.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.github.thirumalx.dto.Client;
import io.github.thirumalx.dto.PageRequest;
import io.github.thirumalx.dto.PageResponse;
import io.github.thirumalx.dto.User;
import io.github.thirumalx.dto.UserAssignmentRequest;
import io.github.thirumalx.exception.ResourceNotFoundException;
import io.github.thirumalx.service.ClientService;
import jakarta.validation.Valid;

/**
 * @author Thirumal
 */
@RestController
@RequestMapping("/application/{applicationId}/client")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping()
    public ResponseEntity<Client> saveClient(@PathVariable Long applicationId, @Valid @RequestBody Client client) {
        client.setApplicationId(applicationId);
        Client saved = clientService.save(client);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable Long applicationId, @PathVariable Long id,
            @Valid @RequestBody Client client) {
        Client updatedClient = clientService.update(applicationId, id, client); 
        return ResponseEntity.ok(updatedClient);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getClient(@PathVariable Long applicationId, @PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClient(applicationId, id));
    }

    @GetMapping()
    public ResponseEntity<PageResponse<Client>> listClient(@PathVariable Long applicationId, @Valid PageRequest pageRequest) {
        return ResponseEntity.ok(clientService.listClient(applicationId, pageRequest));
    }

    @DeleteMapping("/{id}")
    public boolean deleteClient(@PathVariable Long applicationId, @PathVariable Long id) {
        return clientService.deleteClient(applicationId, id);
    }

    @GetMapping("/{id}/assignees")
    public ResponseEntity<List<User>> listAssignees(@PathVariable Long applicationId, @PathVariable Long id) {
        if (clientService.getClient(applicationId, id) == null) {
            throw new ResourceNotFoundException("Client not found");
        }
        return ResponseEntity.ok(clientService.listAssignedUsers(id));
    }

    @PostMapping("/{id}/assignees")
    public ResponseEntity<User> assignUser(@PathVariable Long applicationId, @PathVariable Long id,
            @Valid @RequestBody UserAssignmentRequest request) {
        if (clientService.getClient(applicationId, id) == null) {
            throw new ResourceNotFoundException("Client not found");
        }
        return ResponseEntity.ok(clientService.assignUser(id, request));
    }

    @PutMapping("/{id}/assignees/{userId}")
    public ResponseEntity<User> updateAssignee(@PathVariable Long applicationId, @PathVariable Long id,
            @PathVariable Long userId, @Valid @RequestBody UserAssignmentRequest request) {
        if (clientService.getClient(applicationId, id) == null) {
            throw new ResourceNotFoundException("Client not found");
        }
        return ResponseEntity.ok(clientService.updateAssignedUser(id, userId, request));
    }

    @DeleteMapping("/{id}/assignees/{userId}")
    public boolean deleteAssignee(@PathVariable Long applicationId, @PathVariable Long id,
            @PathVariable Long userId) {
        if (clientService.getClient(applicationId, id) == null) {
            throw new ResourceNotFoundException("Client not found");
        }
        return clientService.removeAssignedUser(id, userId);
    }
}
