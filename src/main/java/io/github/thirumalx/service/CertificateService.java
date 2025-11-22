package io.github.thirumalx.service;

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
     //   Long applicationId = applicationDao.get(certificate);
        
        return certificate;
    }

      public Certificate getCertificateDeail(Certificate certificate) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCertificateDeail'");
    }

}
