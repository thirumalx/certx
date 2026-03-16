package io.github.thirumalx.service;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.math.BigInteger;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.util.InternalException;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Thirumal M
 */
@Service
public class CRLService {

    private static final Logger logger = LoggerFactory.getLogger(CRLService.class);
    private final HttpClient httpClient;

    public CRLService() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Checks if the given certificate is revoked by consulting its CRL Distribution
     * Points.
     * 
     * @param cert The certificate to check.
     * @return true if revoked, false otherwise.
     */
    public boolean isRevoked(X509Certificate cert) {
        logger.debug("Checking revocation for certificate: {}", cert.getSerialNumber());
        List<String> crlUrls = getCRLDistributionPoints(cert);
        if (crlUrls == null || crlUrls.isEmpty()) {
            logger.warn("No CRL distribution points found for certificate: {}", cert.getSerialNumber());
            return false;
        }
        for (String url : crlUrls) {
            try {
                if (checkCRL(cert, url)) {
                    return true;
                }
            } catch (Exception e) {
                logger.warn("Failed to check CRL at {}: {}", url, e.getMessage());
                throw new InternalException("Failed to check CRL at " + url + ": " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * Parses a certificate serial number from a string that may be decimal or hexadecimal.
     * Hex format is detected by a 0x prefix or the presence of A-F characters.
     */
    static BigInteger parseSerialNumber(String serial) {
        if (serial == null || serial.isBlank()) {
            throw new IllegalArgumentException("Serial number must not be blank");
        }
        String trimmed = serial.trim();
        boolean isHex = trimmed.startsWith("0x") || trimmed.matches("(?i).*[a-f].*");
        if (isHex) {
            String hex = trimmed.replaceFirst("(?i)^0x", "");
            return new BigInteger(hex, 16);
        }
        return new BigInteger(trimmed, 10);
    }

    private boolean checkCRL(X509Certificate cert, String url) throws Exception {
        logger.debug("Fetching CRL from {}", url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP error " + response.statusCode());
        }

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509CRL crl = (X509CRL) cf.generateCRL(response.body());

        X509CRLEntry entry = crl.getRevokedCertificate(cert.getSerialNumber());
        // To simulate testing with a known revoked certificate, you can use a hardcoded serial number:
        //X509CRLEntry entry = crl.getRevokedCertificate(parseSerialNumber("166f3dfbb5"));
        if (entry != null) {
            logger.info("Certificate {} is REVOKED in CRL at {}", cert.getSerialNumber(), url);
            return true;
        }
        return false;
    }

    /**
     * Extracts CRL Distribution Points (URLs) from an X509 certificate.
     */
    public List<String> getCRLDistributionPoints(X509Certificate cert) {
        logger.debug("Extracting CRL distribution points for certificate: {}", cert.getSerialNumber());
        List<String> crlUrls = new ArrayList<>();
        byte[] crldpExtensionValue = cert.getExtensionValue(Extension.cRLDistributionPoints.getId());
        if (crldpExtensionValue == null) {
            return crlUrls;
        }

        try {
            ASN1InputStream asn1In = new ASN1InputStream(crldpExtensionValue);
            DEROctetString octetString = (DEROctetString) asn1In.readObject();
            asn1In.close();

            ASN1InputStream asn1InOctets = new ASN1InputStream(octetString.getOctets());
            CRLDistPoint distPoint = CRLDistPoint.getInstance(asn1InOctets.readObject());
            asn1InOctets.close();

            for (DistributionPoint dp : distPoint.getDistributionPoints()) {
                DistributionPointName dpn = dp.getDistributionPoint();
                if (dpn != null && dpn.getType() == DistributionPointName.FULL_NAME) {
                    GeneralNames gns = GeneralNames.getInstance(dpn.getName());
                    for (GeneralName gn : gns.getNames()) {
                        if (gn.getTagNo() == GeneralName.uniformResourceIdentifier) {
                            String url = ((ASN1IA5String) gn.getName()).getString();
                            crlUrls.add(url);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error extracting CRL distribution points: {}", e.getMessage());
        }
        logger.debug("CRL Urls extracted: {}", crlUrls);
        return crlUrls;
    }
}
