package com.jovine360.talenthub;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Full application context integration test.
 * Requires Docker to be running (MySQL via Testcontainers) and is disabled in CI
 * environments without Docker. Individual module slices use mocked beans instead.
 */
@Disabled("Requires Docker — enable locally with 'docker compose -f docker-compose.dev.yml up -d'")
@SpringBootTest
@ActiveProfiles("test")
class TalenthubApplicationTests {

	@Test
	void contextLoads() {
	}

}
