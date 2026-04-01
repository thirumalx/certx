package io.github.thirumalx.scheduler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.github.thirumalx.dao.view.CertificateViewDao;
import io.github.thirumalx.dto.CRLCheckRunResponse;
import io.github.thirumalx.dto.Certificate;
import io.github.thirumalx.model.Knot;
import io.github.thirumalx.service.CertificateService;
import io.github.thirumalx.service.CRLService;
import io.github.thirumalx.service.NotificationService;
import io.github.thirumalx.service.PasswordCryptoService;

/**
 * @author Thirumal M
 * Scheduled task that runs every 6 hours to check all ACTIVE certificates for revocation by consulting their CRL
 * distribution points. If a certificate is found to be revoked, it updates its status in the database to REVOKED
 * and records the revocation time.
 * <p>
 * Schedule: 00:00, 06:00, 12:00, 18:00 (server time).
 * Manual trigger: REST API <code>/scheduler/crl-check/run</code> invokes the same logic and returns a summary.
 * </p>
 */
@Component
public class CRLCheckScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CRLCheckScheduler.class);

    private final CertificateViewDao certificateViewDao;
    private final CertificateService certificateService;
    private final CRLService crlService;
    private final NotificationService notificationService;
    private final PasswordCryptoService passwordCryptoService;

    public CRLCheckScheduler(CertificateViewDao certificateViewDao,
            CertificateService certificateService,
            CRLService crlService,
            NotificationService notificationService,
            PasswordCryptoService passwordCryptoService) {
        this.certificateViewDao = certificateViewDao;
        this.certificateService = certificateService;
        this.crlService = crlService;
        this.notificationService = notificationService;
        this.passwordCryptoService = passwordCryptoService;
    }

    /**
     * Runs every 6 hours at 00:00, 06:00, 12:00, 18:00 (server time).
     * Checks all ACTIVE certificates for revocation.
     */
    @Scheduled(cron = "0 0 0,6,12,18 * * ?")
    @Transactional
    public void checkAllCertificatesForRevocation() {
        runCrlCheckInternal();
    }

    /**
     * Manually runs the CRL check and returns a summary for UI/API usage.
     */
    @Transactional
    public CRLCheckRunResponse runCrlCheckNow() {
        return runCrlCheckInternal();
    }

    private CRLCheckRunResponse runCrlCheckInternal() {
        Instant startedAt = Instant.now();
        logger.info("Starting CRL check for all ACTIVE certificates");
        List<Certificate> activeCerts = certificateViewDao.listNow(Knot.ACTIVE, 0, Integer.MAX_VALUE);
        logger.info("Found {} active certificates to check", activeCerts.size());
        
        List<CRLCheckRunResponse.CertificateLog> logs = new ArrayList<>();
        int revokedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;
        int processedCount = 0;

        for (Certificate cert : activeCerts) {
            if (cert.getPath() == null || cert.getPath().isBlank()) {
                skippedCount++;
                logs.add(new CRLCheckRunResponse.CertificateLog(cert.getSerialNumber(), "SKIPPED", "No path provided"));
                continue;
            }
            try {
                X509Certificate x509Cert;
                if (cert.ispfxCertificate()) {
                    String decryptedPassword = passwordCryptoService.decrypt(cert.getPassword());
                    x509Cert = loadX509FromPFX(cert, decryptedPassword);
                } else {
                    x509Cert = loadX509Certificate(cert);
                }
                
                if (x509Cert == null) {
                    failedCount++;
                    logs.add(new CRLCheckRunResponse.CertificateLog(cert.getSerialNumber(), "FAILED", "Could not load certificate from path"));
                    continue;
                }
                
                processedCount++;
                if (crlService.isRevoked(x509Cert)) {
                    certificateService.markAsRevoked(cert.getId(), Instant.now());
                    revokedCount++;
                    logs.add(new CRLCheckRunResponse.CertificateLog(cert.getSerialNumber(), "REVOKED", "Certificate found in CRL"));
                    notificationService.sendRevocationNotification(cert.getId());
                } else {
                    logs.add(new CRLCheckRunResponse.CertificateLog(cert.getSerialNumber(), "SUCCESS", "Certificate is valid"));
                }
            } catch (Exception e) {
                failedCount++;
                logs.add(new CRLCheckRunResponse.CertificateLog(cert.getSerialNumber(), "FAILED", e.getMessage()));
                logger.warn("CRL check failed for certificate {} (ID: {}): {}",
                        cert.getSerialNumber(), cert.getId(), e.getMessage());
            }
        }
        Instant finishedAt = Instant.now();
        return new CRLCheckRunResponse(startedAt, finishedAt, activeCerts.size(),
                processedCount, revokedCount, skippedCount, failedCount, logs);
    }

    public X509Certificate loadX509Certificate(Certificate cert) {
        logger.debug("Loading X509Certificate from path: {}", cert.getPath());
        CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            logger.error("Failed to create CertificateFactory: {}", e.getMessage());
            throw new RuntimeException("Failed to create certificate factory", e);
        }
        try (InputStream in = new FileInputStream(cert.getPath())) {
            return (X509Certificate) cf.generateCertificate(in);
        } catch (Exception e) {
            logger.error("Failed to load X509Certificate from path {}: {}", cert.getPath(), e.getMessage());
            throw new RuntimeException("Failed to load certificate", e);
        }
    }

    public X509Certificate loadX509FromPFX(Certificate cert, String password) {
        logger.debug("Loading X509Certificate from PFX: {}", cert.getPath());
        try  {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream(cert.getPath())) {
                keystore.load(fis, password.toCharArray());
            }

            Enumeration<String> aliases = keystore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                java.security.cert.Certificate certificate = keystore.getCertificate(alias);
                if (certificate instanceof X509Certificate x509Certificate) {
                    return x509Certificate;
                }
            }
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            logger.error("Failed to load X509Certificate from PFX: {}", e.getMessage());
        }
        return null;
    }
}
