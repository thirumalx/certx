package io.github.thirumalx.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.thirumalx.dto.Certificate;

public class CertificateControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;  // for JSON conversion
    @Test
    void getCertificateDetailTest() throws Exception {

        Certificate request = Certificate.builder()
                .serialNumber("123321")
                .build();

        mockMvc.perform(post("/certificate/fetch-detail")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serialNumber").value("123321"))
                .andExpect(jsonPath("$.revoked").value(false));
    }
}
