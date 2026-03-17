package io.github.thirumalx.benchmark;

import io.github.thirumalx.dao.anchor.*;
import io.github.thirumalx.dao.view.*;
import io.github.thirumalx.dao.attribute.*;
import io.github.thirumalx.dao.tie.*;
import io.github.thirumalx.dto.Application;
import io.github.thirumalx.dto.Certificate;
import io.github.thirumalx.dto.CertificateValidityResponse;
import io.github.thirumalx.dto.Client;
import io.github.thirumalx.service.CertificateService;
import io.github.thirumalx.service.PasswordCryptoService;
import org.mockito.Mockito;
import org.openjdk.jmh.annotations.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 5, time = 1)
public class CertificateValidityBenchmark {

    private CertificateService certificateService;
    private final String serialNumber = "123456";
    private final String appUniqueId = "APP-001";
    private final String clientUniqueId = "CLIENT-001";

    @Setup
    public void setup() {
        // Build Mocks
        ApplicationViewDao applicationViewDao = mock(ApplicationViewDao.class);
        ClientViewDao clientViewDao = mock(ClientViewDao.class);
        CertificateViewDao certificateViewDao = mock(CertificateViewDao.class);

        // Required by Constructor but not used for checkCertificateValidity
        ApplicationAnchorDao applicationDao = mock(ApplicationAnchorDao.class);
        CertificateAnchorDao certificateAnchorDao = mock(CertificateAnchorDao.class);
        CertificateSerialNumberAttributeDao serialNumberAttributeDao = mock(CertificateSerialNumberAttributeDao.class);
        CertificatePathAttributeDao pathAttributeDao = mock(CertificatePathAttributeDao.class);
        CertificateStatusAttributeDao statusAttributeDao = mock(CertificateStatusAttributeDao.class);
        CertificateIssuedOnAttributeDao issuedOnAttributeDao = mock(CertificateIssuedOnAttributeDao.class);
        CertificateRevokedOnAttributeDao revokedOnAttributeDao = mock(CertificateRevokedOnAttributeDao.class);
        ApplicationCertificateTieDao applicationCertificateTieDao = mock(ApplicationCertificateTieDao.class);
        CertificateClientTieDao certificateClientTieDao = mock(CertificateClientTieDao.class);
        CertificateNotAfterAttributeDao notAfterAttributeDao = mock(CertificateNotAfterAttributeDao.class);
        CertificateLastTimeVerifiedOnAttributeDao lastTimeVerifiedOnAttributeDao = mock(CertificateLastTimeVerifiedOnAttributeDao.class);
        CertificatePasswordAttributeDao passwordAttributeDao = mock(CertificatePasswordAttributeDao.class);
        PasswordCryptoService passwordCryptoService = mock(PasswordCryptoService.class);

        // Pre-fill with Mocks
        certificateService = new CertificateService(
                applicationDao, applicationViewDao, certificateAnchorDao, certificateViewDao, clientViewDao,
                serialNumberAttributeDao, pathAttributeDao, statusAttributeDao, issuedOnAttributeDao,
                revokedOnAttributeDao, applicationCertificateTieDao, certificateClientTieDao,
                notAfterAttributeDao, lastTimeVerifiedOnAttributeDao, passwordAttributeDao,
                passwordCryptoService
        );

        // Mock Behaviors for checkCertificateValidity
        when(applicationViewDao.findNowByUniqueId(appUniqueId))
                .thenReturn(Optional.of(Application.builder().id(1L).build()));
        
        when(clientViewDao.findNowByUniqueId(clientUniqueId))
                .thenReturn(Optional.of(Client.builder().id(1L).build()));

        Certificate mockCert = Certificate.builder()
                .serialNumber(serialNumber)
                .notAfter(LocalDateTime.now().plusYears(1))
                .status("ACTIVE")
                .build();
        
        when(certificateViewDao.findNowBySerialNumber(anyString(), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(Optional.of(mockCert));
    }

    @Benchmark
    public CertificateValidityResponse benchmarkCheckValidity() {
        return certificateService.checkCertificateValidity(serialNumber, appUniqueId, clientUniqueId);
    }
}
