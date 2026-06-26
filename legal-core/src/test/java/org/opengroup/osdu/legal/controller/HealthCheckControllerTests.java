package org.opengroup.osdu.legal.controller;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.legal.controller.HealthCheckController;
import org.springframework.http.HttpStatus;

public class HealthCheckControllerTests {

	private HealthCheckController sut;

	@Before
	public void setup() {
		this.sut = new HealthCheckController();
	}

	@Test
	public void should_returnHttp200_when_checkLiveness() {
		assertEquals(HttpStatus.OK, this.sut.livenessCheck().getStatusCode());
	}

	@Test
	public void should_returnHttp200_when_checkReadiness() {
		assertEquals(HttpStatus.OK, this.sut.readinessCheck().getStatusCode());
	}
}
