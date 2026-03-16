package io.github.thirumalx.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

class CRLServiceTest {

    @Test
    void parseSerialNumberParsesHexWithoutPrefix() {
        String hexSerial = "281f4d16774601e6f6dc";
        BigInteger parsed = CRLService.parseSerialNumber(hexSerial);
        assertThat(parsed).isEqualTo(new BigInteger(hexSerial, 16));
    }

    @Test
    void parseSerialNumberParsesHexWithPrefix() {
        String hexSerial = "0x1A2B3C";
        BigInteger parsed = CRLService.parseSerialNumber(hexSerial);
        assertThat(parsed).isEqualTo(new BigInteger("1A2B3C", 16));
    }

    @Test
    void parseSerialNumberParsesDecimal() {
        String decimalSerial = "123456789";
        BigInteger parsed = CRLService.parseSerialNumber(decimalSerial);
        assertThat(parsed).isEqualTo(new BigInteger(decimalSerial, 10));
    }

    @Test
    void parseSerialNumberRejectsBlank() {
        assertThatThrownBy(() -> CRLService.parseSerialNumber("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Serial number must not be blank");
    }
}
