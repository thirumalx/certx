package io.github.thirumalx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CertxApplication {

	public static void main(String[] args) {
		SpringApplication.run(CertxApplication.class, args);
	}

}
