package io.github.thirumalx.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.thirumalx.dto.Certificate;
import io.github.thirumalx.service.CertificateService;

@WebMvcTest(CertificateController.class)
public class CertificateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CertificateService certificateService;

    @Test
    void saveCertificateTest() throws Exception {
        Certificate certificate = Certificate.builder()
                .serialNumber("123321")
                .path("test.crt")
                .ownerName("Test Client")
                .build();

        // Mocking can be added here if needed, but for now we test the endpoint exists
        // and handles JSON

        mockMvc.perform(post("/application/1/client/1/certificate/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(certificate)))
                .andExpect(status().isCreated());
    }
}
