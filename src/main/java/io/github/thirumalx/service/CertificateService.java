package io.github.thirumalx.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.thirumalx.dao.anchor.ApplicationAnchorDao;
import io.github.thirumalx.dao.anchor.CertificateAnchorDao;
import io.github.thirumalx.dao.attribute.CertificateIssuedOnAttributeDao;
import io.github.thirumalx.dao.attribute.CertificateLastTimeVerifiedOnAttributeDao;
import io.github.thirumalx.dao.attribute.CertificateNotAfterAttributeDao;
import io.github.thirumalx.dao.attribute.CertificatePathAttributeDao;
import io.github.thirumalx.dao.attribute.CertificateRevokedOnAttributeDao;
import io.github.thirumalx.dao.attribute.CertificateSerialNumberAttributeDao;
import io.github.thirumalx.dao.attribute.CertificateStatusAttributeDao;
import io.github.thirumalx.dao.tie.CertificateClientTieDao;
import io.github.thirumalx.dao.view.CertificateViewDao;
import io.github.thirumalx.dao.view.ClientViewDao;
import io.github.thirumalx.dto.Certificate;
import io.github.thirumalx.dto.Client;
import io.github.thirumalx.dto.PageRequest;
import io.github.thirumalx.dto.PageResponse;
import io.github.thirumalx.exception.ResourceNotFoundException;
import io.github.thirumalx.model.Anchor;
import io.github.thirumalx.model.Attribute;
import io.github.thirumalx.model.Knot;
import java.io.File;
import java.time.Instant;

/**
 * @author Thirumal
 */
@Service
public class CertificateService {

  Logger logger = LoggerFactory.getLogger(CertificateService.class);

  private final ApplicationAnchorDao applicationDao;
  private final CertificateAnchorDao certificateAnchorDao;
  private final CertificateViewDao certificateViewDao;
  private final ClientViewDao clientViewDao;
  // Attributes
  private final CertificateSerialNumberAttributeDao serialNumberAttributeDao;
  private final CertificatePathAttributeDao pathAttributeDao;
  private final CertificateStatusAttributeDao statusAttributeDao;
  private final CertificateIssuedOnAttributeDao issuedOnAttributeDao;
  private final CertificateRevokedOnAttributeDao revokedOnAttributeDao;
  private final CertificateNotAfterAttributeDao notAfterAttributeDao;
  private final CertificateLastTimeVerifiedOnAttributeDao lastTimeVerifiedOnAttributeDao;
  // Tie
  private final CertificateClientTieDao certificateClientTieDao;

  public CertificateService(ApplicationAnchorDao applicationDao, CertificateAnchorDao certificateAnchorDao,
      CertificateViewDao certificateViewDao, ClientViewDao clientViewDao,
      CertificateSerialNumberAttributeDao serialNumberAttributeDao, CertificatePathAttributeDao pathAttributeDao,
      CertificateStatusAttributeDao statusAttributeDao, CertificateIssuedOnAttributeDao issuedOnAttributeDao,
      CertificateRevokedOnAttributeDao revokedOnAttributeDao, CertificateClientTieDao certificateClientTieDao,
      CertificateNotAfterAttributeDao notAfterAttributeDao,
      CertificateLastTimeVerifiedOnAttributeDao lastTimeVerifiedOnAttributeDao) {
    this.applicationDao = applicationDao;
    this.certificateAnchorDao = certificateAnchorDao;
    this.certificateViewDao = certificateViewDao;
    this.clientViewDao = clientViewDao;
    this.serialNumberAttributeDao = serialNumberAttributeDao;
    this.pathAttributeDao = pathAttributeDao;
    this.statusAttributeDao = statusAttributeDao;
    this.issuedOnAttributeDao = issuedOnAttributeDao;
    this.revokedOnAttributeDao = revokedOnAttributeDao;
    this.certificateClientTieDao = certificateClientTieDao;
    this.notAfterAttributeDao = notAfterAttributeDao;
    this.lastTimeVerifiedOnAttributeDao = lastTimeVerifiedOnAttributeDao;
  }

  @Transactional
  public Certificate save(Long applicationId, Long clientId, Certificate certificate) {
    logger.info("Saving certificate: {} for the client {} for the application {}", certificate, clientId,
        applicationId);
    if (!applicationDao.existsById(applicationId)) {
      throw new ResourceNotFoundException("Application with ID " + applicationId + " not found");
    }
    Optional<Client> clientOptional = clientViewDao.findNowById(clientId);
    if (clientOptional.isEmpty()) {
      throw new ResourceNotFoundException("Client with ID " + clientId + " not found");
    }

    // Anchor
    Long certificateId = certificateAnchorDao.insert(Anchor.METADATA_ACTIVE);
    certificate.setId(certificateId);

    // Attributes
    if (certificate.getSerialNumber() != null) {
      serialNumberAttributeDao.insert(certificateId, certificate.getSerialNumber(), Attribute.METADATA_ACTIVE);
    }
    if (certificate.getPath() != null) {
      pathAttributeDao.insert(certificateId, certificate.getPath(), Attribute.METADATA_ACTIVE);
    }
    statusAttributeDao.insert(certificateId, certificate.getStatus(), Instant.now(), Attribute.METADATA_ACTIVE);
    issuedOnAttributeDao.insert(certificateId, certificate.getIssuedOn(), Attribute.METADATA_ACTIVE);
    notAfterAttributeDao.insert(certificateId, certificate.getNotAfter(), Attribute.METADATA_ACTIVE);
    lastTimeVerifiedOnAttributeDao.insert(certificateId, certificate.getLastTimeVerifiedOn(),
        Attribute.METADATA_ACTIVE);

    // Tie
    certificateClientTieDao.insertHistorized(certificateId, clientId, Attribute.METADATA_ACTIVE, Instant.now());

    return getCertificate(certificateId);
  }

  @Transactional
  public Certificate update(Long applicationId, Long clientId, Long id, Certificate certificate) {
    logger.info("Updating certificate: {} with ID: {}", certificate, id);
    Optional<Certificate> existingOptional = certificateViewDao.findNowById(id);
    if (existingOptional.isEmpty()) {
      throw new ResourceNotFoundException("Certificate not found");
    }
    Certificate existing = existingOptional.get();

    if (certificate.getSerialNumber() != null && !certificate.getSerialNumber().equals(existing.getSerialNumber())) {
      serialNumberAttributeDao.insert(id, certificate.getSerialNumber(), Attribute.METADATA_ACTIVE);
    }
    if (certificate.getPath() != null && !certificate.getPath().equals(existing.getPath())) {
      pathAttributeDao.insert(id, certificate.getPath(), Attribute.METADATA_ACTIVE);
    }

    return getCertificate(id);
  }

  public Certificate getCertificate(Long id) {
    logger.debug("Getting certificate with ID: {}", id);
    return certificateViewDao.findNowById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Certificate with ID " + id + " not found"));
  }

  @Transactional
  public boolean delete(Long id) {
    logger.info("Revoking certificate with ID: {}", id);
    statusAttributeDao.insert(id, Knot.DELETED, Instant.now(), Attribute.METADATA_ACTIVE);
    revokedOnAttributeDao.insert(id, Instant.now(), Attribute.METADATA_ACTIVE);
    return true;
  }

  public boolean validatePath(String path) {
    if (path == null || path.isEmpty()) {
      return false;
    }
    String basePath = "C:\\IUShare\\amFzZ2R1NjEyODN0MTI4OXNkYmZ\\Certificate";
    File file = new File(basePath, path);
    logger.debug("Validating certificate path: {}", file.getAbsolutePath());
    return file.exists();
  }

  public PageResponse<Certificate> listCertificates(Long applicationId, Long clientId, PageRequest pageRequest) {
    logger.debug("Listing certificates for application: {} and client: {}", applicationId, clientId);
    List<Certificate> certificates = certificateViewDao.listNowByClient(clientId, pageRequest.page(),
        pageRequest.size());
    long totalElements = certificateViewDao.countNowByClient(clientId);
    int totalPages = (int) Math.ceil((double) totalElements / pageRequest.size());
    return new PageResponse<>(pageRequest.page(), pageRequest.size(), certificates, totalElements, totalPages);
  }

}
