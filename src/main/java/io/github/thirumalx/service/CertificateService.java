package io.github.thirumalx.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.thirumalx.cache.CacheNames;
import io.github.thirumalx.dao.anchor.ApplicationAnchorDao;
import io.github.thirumalx.dao.anchor.CertificateAnchorDao;
import io.github.thirumalx.dao.attribute.CertificateIssuedOnAttributeDao;
import io.github.thirumalx.dao.attribute.CertificateLastTimeVerifiedOnAttributeDao;
import io.github.thirumalx.dao.attribute.CertificateNotAfterAttributeDao;
import io.github.thirumalx.dao.attribute.CertificatePasswordAttributeDao;
import io.github.thirumalx.dao.attribute.CertificatePathAttributeDao;
import io.github.thirumalx.dao.attribute.CertificateRevokedOnAttributeDao;
import io.github.thirumalx.dao.attribute.CertificateSerialNumberAttributeDao;
import io.github.thirumalx.dao.attribute.CertificateStatusAttributeDao;
import io.github.thirumalx.dao.tie.ApplicationCertificateTieDao;
import io.github.thirumalx.dao.tie.CertificateClientTieDao;
import io.github.thirumalx.dao.view.ApplicationViewDao;
import io.github.thirumalx.dao.view.CertificateViewDao;
import io.github.thirumalx.dao.view.ClientViewDao;
import io.github.thirumalx.dto.Certificate;
import io.github.thirumalx.dto.CertificateValidityResponse;
import io.github.thirumalx.dto.Client;
import io.github.thirumalx.dto.PageRequest;
import io.github.thirumalx.dto.PageResponse;
import io.github.thirumalx.exception.DuplicateKeyException;
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
  private final ApplicationViewDao applicationViewDao;
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
  private final CertificatePasswordAttributeDao passwordAttributeDao;
  private final PasswordCryptoService passwordCryptoService;
  // Tie
  private final ApplicationCertificateTieDao applicationCertificateTieDao;
  private final CertificateClientTieDao certificateClientTieDao;

  public CertificateService(ApplicationAnchorDao applicationDao,
      ApplicationViewDao applicationViewDao,
      CertificateAnchorDao certificateAnchorDao,
      CertificateViewDao certificateViewDao,
      ClientViewDao clientViewDao,
      CertificateSerialNumberAttributeDao serialNumberAttributeDao, CertificatePathAttributeDao pathAttributeDao,
      CertificateStatusAttributeDao statusAttributeDao, CertificateIssuedOnAttributeDao issuedOnAttributeDao,
      CertificateRevokedOnAttributeDao revokedOnAttributeDao,
      ApplicationCertificateTieDao applicationCertificateTieDao, CertificateClientTieDao certificateClientTieDao,
      CertificateNotAfterAttributeDao notAfterAttributeDao,
      CertificateLastTimeVerifiedOnAttributeDao lastTimeVerifiedOnAttributeDao,
      CertificatePasswordAttributeDao passwordAttributeDao,
      PasswordCryptoService passwordCryptoService) {
    this.applicationDao = applicationDao;
    this.applicationViewDao = applicationViewDao;
    this.certificateAnchorDao = certificateAnchorDao;
    this.certificateViewDao = certificateViewDao;
    this.clientViewDao = clientViewDao;
    this.serialNumberAttributeDao = serialNumberAttributeDao;
    this.pathAttributeDao = pathAttributeDao;
    this.statusAttributeDao = statusAttributeDao;
    this.issuedOnAttributeDao = issuedOnAttributeDao;
    this.revokedOnAttributeDao = revokedOnAttributeDao;
    this.applicationCertificateTieDao = applicationCertificateTieDao;
    this.certificateClientTieDao = certificateClientTieDao;
    this.notAfterAttributeDao = notAfterAttributeDao;
    this.lastTimeVerifiedOnAttributeDao = lastTimeVerifiedOnAttributeDao;
    this.passwordAttributeDao = passwordAttributeDao;
    this.passwordCryptoService = passwordCryptoService;
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

    String normalizedSerial = normalizeText(certificate.getSerialNumber());
    certificate.setSerialNumber(normalizedSerial);
    if (normalizedSerial != null) {
      Optional<Certificate> existing = certificateViewDao.findNowBySerialNumber(normalizedSerial, applicationId,
          clientId);
      if (existing.isPresent()) {
        Certificate found = existing.get();
        throw new DuplicateKeyException(
            "Certificate with serial number " + normalizedSerial
                + " already exists for this application and client (status: " + found.getStatus() + ")");
      }
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
    if ((certificate.getPath().toLowerCase().endsWith(".pfx")
        || certificate.getPath().toLowerCase().endsWith(".p12"))) {
      if (certificate.getPassword() == null) {
        throw new IllegalArgumentException("Password is required for PFX/P12 files");
      }
      String encryptedPassword = passwordCryptoService.encrypt(certificate.getPassword());
      passwordAttributeDao.insert(certificateId, encryptedPassword, Attribute.METADATA_ACTIVE);
    }
    // Tie
    applicationCertificateTieDao.insertWithMetadata(applicationId, certificateId, Attribute.METADATA_ACTIVE);
    certificateClientTieDao.insert(certificateId, clientId, Attribute.METADATA_ACTIVE);

    return getCertificate(certificateId);
  }

  public Certificate getCertificate(Long id) {
    logger.debug("Getting certificate with ID: {}", id);
    return certificateViewDao.findNowById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Certificate with ID " + id + " not found"));
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.CERTIFICATE_VALIDITY, allEntries = true)
  public boolean delete(Long id) {
    logger.info("Revoking certificate with ID: {}", id);
    statusAttributeDao.insert(id, Knot.DELETED, Instant.now(), Attribute.METADATA_ACTIVE);
    return true;
  }

  @Transactional
  @CacheEvict(cacheNames = CacheNames.CERTIFICATE_VALIDITY, allEntries = true)
  public boolean markAsRevoked(Long id, Instant revokedAt) {
    logger.info("Marking certificate {} as REVOKED at {}", id, revokedAt);
    statusAttributeDao.insert(id, Knot.REVOKED, Instant.now(), Attribute.METADATA_ACTIVE);
    revokedOnAttributeDao.insert(id, revokedAt, Attribute.METADATA_ACTIVE);
    lastTimeVerifiedOnAttributeDao.insert(id, Instant.now(), Attribute.METADATA_ACTIVE);
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
    List<Certificate> certificates = certificateViewDao.listNowByApplicationAndClient(applicationId, clientId, status,
        pageRequest.page(),
        pageRequest.size());
    long totalElements = certificateViewDao.countNowByApplicationAndClient(applicationId, clientId, status);
    int totalPages = (int) Math.ceil((double) totalElements / pageRequest.size());
    return new PageResponse<>(pageRequest.page(), pageRequest.size(), certificates, totalElements, totalPages);
  }

  @Cacheable(cacheNames = CacheNames.CERTIFICATE_VALIDITY,
      key = "(#serialNumber == null ? '' : #serialNumber.trim()) + '|' + "
          + "(#applicationUniqueId == null ? '' : #applicationUniqueId.trim()) + '|' + "
          + "(#clientUniqueId == null ? '' : #clientUniqueId.trim())")
  public CertificateValidityResponse checkCertificateValidity(String serialNumber,
      String applicationUniqueId,
      String clientUniqueId) {
    String normalizedSerial = normalizeText(serialNumber);
    if (normalizedSerial == null || normalizedSerial.isEmpty()) {
      throw new IllegalArgumentException("Serial number is required");
    }

    String normalizedApplicationUniqueId = normalizeText(applicationUniqueId);
    Long applicationId = null;
    if (normalizedApplicationUniqueId != null) {
      applicationId = applicationViewDao.findNowByUniqueId(normalizedApplicationUniqueId)
          .map(io.github.thirumalx.dto.Application::getId)
          .orElse(null);
      if (applicationId == null) {
        return new CertificateValidityResponse(normalizedSerial, false, "APPLICATION_NOT_FOUND", null, null);
      }
    }

    String normalizedClientUniqueId = normalizeText(clientUniqueId);
    Long clientId = null;
    if (normalizedClientUniqueId != null) {
      clientId = clientViewDao.findNowByUniqueId(normalizedClientUniqueId)
          .map(Client::getId)
          .orElse(null);
      if (clientId == null) {
        return new CertificateValidityResponse(normalizedSerial, false, "CLIENT_NOT_FOUND", null, null);
      }
    }

    Optional<Certificate> certificate = certificateViewDao.findNowBySerialNumber(normalizedSerial, applicationId,
        clientId);
    if (certificate.isEmpty()) {
      return new CertificateValidityResponse(normalizedSerial, false, "NOT_FOUND", null, null);
    }

    Certificate cert = certificate.get();
    boolean revoked = cert.getRevokedOn() != null;
    if (revoked) {
      return new CertificateValidityResponse(cert.getSerialNumber(), false, "REVOKED", cert.getNotAfter(),
          cert.getRevokedOn());
    }

    if (cert.getNotAfter() == null) {
      return new CertificateValidityResponse(cert.getSerialNumber(), false, "NO_EXPIRY", null, cert.getRevokedOn());
    }

    boolean expired = LocalDateTime.now().isAfter(cert.getNotAfter());
    if (expired) {
      return new CertificateValidityResponse(cert.getSerialNumber(), false, "EXPIRED", cert.getNotAfter(),
          cert.getRevokedOn());
    }

    return new CertificateValidityResponse(cert.getSerialNumber(), true, "VALID", cert.getNotAfter(),
        cert.getRevokedOn());
  }

  private String normalizeText(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

}
