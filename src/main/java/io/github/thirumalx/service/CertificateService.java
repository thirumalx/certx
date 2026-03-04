package io.github.thirumalx.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.thirumalx.dao.anchor.ApplicationAnchorDao;
import io.github.thirumalx.dto.Certificate;

/**
 * @author Thirumal
 */
@Service
public class CertificateService {

  Logger logger = LoggerFactory.getLogger(CertificateService.class);

  private final ApplicationAnchorDao applicationDao;

  public CertificateService(ApplicationAnchorDao applicationDao) {
    this.applicationDao = applicationDao;
  }

  public Certificate save(Certificate certificate) {
    logger.info("Saving certificate: {}", certificate);
    // Long applicationId = applicationDao.get(certificate);

    return certificate;
  }

  public Certificate getCertificateDeail(Certificate certificate) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getCertificateDeail'");
  }

  public List<Certificate> listCertificates(Long applicationId, Long clientId) {
    logger.debug("Listing certificates for application: {} and client: {}", applicationId, clientId);
    List<Certificate> certificates = new ArrayList<>();
    
    return certificates;
    
  }

}
