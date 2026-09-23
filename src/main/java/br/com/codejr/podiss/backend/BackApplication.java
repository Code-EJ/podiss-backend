package br.com.codejr.podiss.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Boot entry point for the podcast API. Schema creation belongs to the external Flyway executor.
 *
 * @author oEnzoRibas
 */
@SpringBootApplication
public class BackApplication {
	public static void main(String[] args) {
		SpringApplication.run(BackApplication.class, args);
	}
}
