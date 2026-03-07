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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

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
    statusAttributeDao.insert(certificateId, Knot.ACTIVE, Instant.now(), Attribute.METADATA_ACTIVE);
    issuedOnAttributeDao.insert(certificateId, certificate.getIssuedOn().toInstant(ZoneOffset.UTC),
        Attribute.METADATA_ACTIVE);
    notAfterAttributeDao.insert(certificateId, certificate.getNotAfter().toInstant(ZoneOffset.UTC),
        Attribute.METADATA_ACTIVE);
    lastTimeVerifiedOnAttributeDao.insert(certificateId, certificate.getLastTimeVerifiedOn().toInstant(ZoneOffset.UTC),
        Attribute.METADATA_ACTIVE);

    // Tie
    certificateClientTieDao.insert(certificateId, clientId, Attribute.METADATA_ACTIVE);

    return getCertificate(certificateId);
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
    return true;
  }

  public Certificate validateCertificate(String path, String password) {
    if (path == null || path.isEmpty()) {
      throw new IllegalArgumentException("Path cannot be empty");
    }
    File file = new File(path);
    logger.debug("Validating certificate path: {}", file.getAbsolutePath());
    if (!file.exists()) {
      throw new ResourceNotFoundException("Certificate file not found at " + file.getAbsolutePath());
    }

    Certificate.CertificateBuilder builder = Certificate.builder()
        .path(path)
        .lastTimeVerifiedOn(LocalDateTime.now())
        .status("ACTIVE");

    try (InputStream is = new FileInputStream(file)) {
      if (path.toLowerCase().endsWith(".pfx") || path.toLowerCase().endsWith(".p12")) {
        logger.debug("Parsing PFX/P12 certificate: {}", path);
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        // Try with provided password
        char[] passwordChars = (password != null) ? password.toCharArray() : "".toCharArray();
        keyStore.load(is, passwordChars);
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
          String alias = aliases.nextElement();
          if (keyStore.isCertificateEntry(alias) || keyStore.isKeyEntry(alias)) {
            java.security.cert.Certificate c = keyStore.getCertificate(alias);
            if (c instanceof X509Certificate cert) {
              builder.serialNumber(cert.getSerialNumber().toString())
                  .issuedOn(LocalDateTime.ofInstant(cert.getNotBefore().toInstant(), ZoneId.systemDefault()))
                  .notAfter(LocalDateTime.ofInstant(cert.getNotAfter().toInstant(), ZoneId.systemDefault()));
              logger.debug("Successfully parsed PFX certificate: {}", path);
              break;
            }
          }
        }
      } else {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(is);
        builder.serialNumber(cert.getSerialNumber().toString())
            .issuedOn(LocalDateTime.ofInstant(cert.getNotBefore().toInstant(), ZoneId.systemDefault()))
            .notAfter(LocalDateTime.ofInstant(cert.getNotAfter().toInstant(), ZoneId.systemDefault()));
        logger.debug("Successfully parsed X.509 certificate: {}", path);
      }
    } catch (Exception e) {
      logger.error("Error parsing certificate: {}. Error: {}", path, e.getMessage());
      // We still return basic info if parsing fails (e.g. for PFX files which need
      // password)
      if (path.toLowerCase().endsWith(".pfx") || path.toLowerCase().endsWith(".p12")) {
        return builder.serialNumber("PASSWORD_PROTECTED_PFX").build();
      }
      throw new RuntimeException("Error parsing certificate: " + e.getMessage());
    }
    return builder.build();
  }

  public PageResponse<Certificate> listCertificates(Long applicationId, Long clientId, String status,
      PageRequest pageRequest) {
    logger.debug("Listing certificates for application: {} and client: {} with status: {}", applicationId, clientId,
        status);
    List<Certificate> certificates = certificateViewDao.listNowByClient(clientId, status, pageRequest.page(),
        pageRequest.size());
    long totalElements = certificateViewDao.countNowByClient(clientId, status);
    int totalPages = (int) Math.ceil((double) totalElements / pageRequest.size());
    return new PageResponse<>(pageRequest.page(), pageRequest.size(), certificates, totalElements, totalPages);
  }

}
