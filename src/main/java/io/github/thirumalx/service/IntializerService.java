package io.github.thirumalx.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Thirumal M
 * 
 */
@Service
public class IntializerService {

	private static final Logger logger = LoggerFactory.getLogger(IntializerService.class);

	public List<String> insertNewCertificates(String path) {
		logger.info("Inserting new certificates... from the path {}", path);
		return null;
	}

}
