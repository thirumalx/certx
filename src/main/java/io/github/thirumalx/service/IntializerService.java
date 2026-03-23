package io.github.thirumalx.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.thirumalx.dao.view.CertificateViewDao;
import io.github.thirumalx.dto.Certificate;
import io.github.thirumalx.dto.Client;
import io.github.thirumalx.dto.InitializerResponse;

/**
 * @author Thirumal M
 * 
 */
@Service
@EnableScheduling
public class IntializerService {

	private static final Logger logger = LoggerFactory.getLogger(IntializerService.class);

	private final CertificateService certificateService;
	private final CertificateViewDao certificateViewDao;
	private final ClientService clientService;

	public IntializerService(CertificateService certificateService, CertificateViewDao certificateViewDao,
			ClientService clientService) {
		this.certificateService = certificateService;
		this.certificateViewDao = certificateViewDao;
		this.clientService = clientService;
	}

	/**
	 * Scans all registered certificate paths for updates every 30 minutes.
	 */
	@Scheduled(cron = "0 0/30 * * * *")
	public void autoUpdateCertificates() {
		logger.info("Starting automated certificate update scan...");
		try {
			List<String> paths = certificateViewDao.findAllUniquePaths();
			for (String path : paths) {
				File file = new File(path);
				if (file.exists() && file.isFile() && isCertificateFile(file.getName())) {
					processCertificateFile(file, null, new ArrayList<>(), new Stats());
				}
			}
			logger.debug("Background scan completed.");
		} catch (Exception e) {
			logger.error("Error during automated certificate update: {}", e.getMessage());
		}
	}

	public InitializerResponse insertNewCertificates(String path, Long applicationId) {
		logger.info("Initializing certificates from path: {}, Application: {}", path, applicationId);
		List<String> logs = new ArrayList<>();
		Stats stats = new Stats();
		File root = new File(path);
		if (!root.exists() || !root.isDirectory()) {
			logger.error("Invalid directory path: {}", path);
			return new InitializerResponse(logs, 0, 0, 0, 0, 0, 0);
		}
		scanDirectory(root, applicationId, logs, stats);
		return new InitializerResponse(
			logs, 
			stats.created + stats.updated + stats.upToDate + stats.error + stats.skipped,
			stats.created,
			stats.updated,
			stats.upToDate,
			stats.error,
			stats.skipped
		);
	}

	private void scanDirectory(File directory, Long applicationId, List<String> logs, Stats stats) {
		File[] files = directory.listFiles();
		if (files == null)
			return;
		for (File file : files) {
			if (file.isDirectory()) {
				scanDirectory(file, applicationId, logs, stats);
			} else if (isCertificateFile(file.getName())) {
				processCertificateFile(file, applicationId, logs, stats);
			}
		}
	}

	private boolean isCertificateFile(String name) {
		String lower = name.toLowerCase();
		return lower.endsWith(".cer") || lower.endsWith(".crt") || lower.endsWith(".pfx") || lower.endsWith(".p12")
				|| lower.endsWith(".der") || lower.endsWith(".pem");
	}

	@Transactional
	public void processCertificateFile(File file, Long applicationId, List<String> logs, Stats stats) {
		String absolutePath = file.getAbsolutePath();
		String filename = file.getName();
		// Assume client name and unique ID are the same as filename (without extension)
		String clientName = filename.contains(".") ? filename.substring(0, filename.lastIndexOf('.')) : filename;

		try {
			// Find existing certificate by path
			Optional<Certificate> existingCert = certificateViewDao.findNowByPath(absolutePath);

			// Extract metadata from file
			Certificate metadata = certificateService.validateCertificate(absolutePath, null);

			if (existingCert.isPresent()) {
				Certificate current = existingCert.get();
				// Check if update is needed (different serial number or expiry)
				boolean needsUpdate = !metadata.getSerialNumber().equals(current.getSerialNumber())
						|| !metadata.getNotAfter().equals(current.getNotAfter());

				if (needsUpdate) {
					logger.info("Updating existing certificate at {}. New Serial: {}", absolutePath,
							metadata.getSerialNumber());
					certificateService.updateCertificateInfo(current.getId(), metadata);
					logs.add("UPDATED: " + absolutePath + " (Serial: " + metadata.getSerialNumber() + ")");
					stats.updated++;
				} else {
					logger.debug("Certificate at {} is already up to date.", absolutePath);
					logs.add("UP-TO-DATE: " + absolutePath);
					stats.upToDate++;
				}
			} else if (applicationId != null) {
				// New certificate registration
				logger.info("Registering new certificate at {} for Client: {} under App: {}", absolutePath, clientName,
						applicationId);

				// 1. Ensure Client exists
				Client clientDto = Client.builder()
						.name(clientName)
						.uniqueId(clientName)
						.applicationId(applicationId)
						.status("ACTIVE")
						.build();
				Client savedClient = clientService.save(clientDto);

				// 2. Register Certificate
				metadata.setPath(absolutePath);
				certificateService.save(applicationId, savedClient.getId(), metadata);

				logs.add("CREATED: " + absolutePath + " (Client: " + clientName + ")");
				stats.created++;
			} else {
				// Background scan might find new files but doesn't know which app they belong to
				logger.debug("Background scan found new file at {} - skipping (no app context)", absolutePath);
				stats.skipped++;
			}
		} catch (Exception e) {
			logger.error("Error processing certificate file {}: {}", absolutePath, e.getMessage());
			logs.add("ERROR: " + absolutePath + " (" + e.getMessage() + ")");
			stats.error++;
		}
	}

	private static class Stats {
		int created = 0;
		int updated = 0;
		int upToDate = 0;
		int error = 0;
		int skipped = 0;
	}

}
