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
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.github.thirumalx.dao.view.CertificateViewDao;
import io.github.thirumalx.dto.Certificate;
import io.github.thirumalx.model.Knot;
import io.github.thirumalx.service.CertificateService;
import io.github.thirumalx.service.CRLService;

/**
 * @author Thirumal M
 * Scheduled task that runs every 6 hours to check all ACTIVE certificates for revocation by consulting their CRL distribution points.
 * If a certificate is found to be revoked, it updates its status in the database to REVOKED and records the revocation time.
 * 
 */
@Component
public class CRLCheckScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CRLCheckScheduler.class);

    private final CertificateViewDao certificateViewDao;
    private final CertificateService certificateService;
    private final CRLService crlService;

    public CRLCheckScheduler(CertificateViewDao certificateViewDao,
            CertificateService certificateService,
            CRLService crlService) {
        this.certificateViewDao = certificateViewDao;
        this.certificateService = certificateService;
        this.crlService = crlService;
    }

    /**
     * Runs every 6 hours at 00:00, 06:00, 12:00, 18:00.
     * Checks all ACTIVE certificates for revocation.
     */
    @Scheduled(cron = "0 0 0,6,12,18 * * ?")
    @Transactional
    public void checkAllCertificatesForRevocation() {
        logger.info("Starting scheduled CRL check for all ACTIVE certificates");
        // Fetch all active certificates
        List<Certificate> activeCerts = certificateViewDao.listNow(Knot.ACTIVE, 0, Integer.MAX_VALUE);
        logger.info("Found {} active certificates to check", activeCerts.size());
        int revokedCount = 0;
        for (Certificate cert : activeCerts) {
            if (cert.getPath() == null || cert.getPath().isEmpty()) {
                logger.warn("Certificate {} (ID: {}) has no path, skipping CRL check",
                        cert.getSerialNumber(), cert.getId());
                continue;
            }
            X509Certificate x509Cert;
            if (cert.ispfxCertificate()) {
                x509Cert = loadX509FromPFX(cert, cert.getPassword());
            } else {
                x509Cert = loadX509Certificate(cert);
            }
            if (crlService.isRevoked(x509Cert)) {
                // Mark as revoked in DB
                certificateService.markAsRevoked(cert.getId(), Instant.now());
                revokedCount++;
            }
        }
        logger.info("CRL check completed. {} certificates marked as REVOKED", revokedCount);
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
