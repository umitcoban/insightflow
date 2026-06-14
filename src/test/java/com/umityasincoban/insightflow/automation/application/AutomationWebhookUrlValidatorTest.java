package com.umityasincoban.insightflow.automation.application;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AutomationWebhookUrlValidatorTest {
	
	private final AutomationWebhookUrlValidator validator = new AutomationWebhookUrlValidator(
			host -> List.of(InetAddress.getByName("93.184.216.34"))
	);
	
	@Test
	void acceptsValidPublicHttpsUrl() {
		assertDoesNotThrow(() -> validator.validateBeforeSend("https://example.com/webhooks/feedback"));
	}
	
	@Test
	void rejectsInvalidScheme() {
		assertThrows(UnsafeAutomationWebhookUrlException.class, () -> validator.validate("ftp://example.com/webhook"));
	}
	
	@Test
	void rejectsLocalhost() {
		assertThrows(UnsafeAutomationWebhookUrlException.class, () -> validator.validate("https://localhost/webhook"));
	}
	
	@Test
	void rejectsLoopbackIp() {
		assertThrows(UnsafeAutomationWebhookUrlException.class, () -> validator.validate("http://127.0.0.1/webhook"));
	}
	
	@Test
	void rejectsPrivateIp() {
		assertThrows(UnsafeAutomationWebhookUrlException.class, () -> validator.validate("http://192.168.1.10/webhook"));
	}
	
	@Test
	void rejectsEmbeddedCredentials() {
		assertThrows(UnsafeAutomationWebhookUrlException.class, () -> validator.validate("https://user:secret@example.com/webhook"));
	}
	
	@Test
	void rejectsMalformedUrl() {
		assertThrows(RuntimeException.class, () -> validator.validate("not a url"));
	}
	
	@Test
	void rejectsHostnameResolvingToLoopback() {
		AutomationWebhookUrlValidator loopbackValidator = new AutomationWebhookUrlValidator(
				host -> List.of(InetAddress.getByName("127.0.0.1"))
		);
		
		assertThrows(UnsafeAutomationWebhookUrlException.class, () -> loopbackValidator.validateBeforeSend("https://example.com/webhook"));
	}
	
	@Test
	void rejectsHostnameResolvingToPrivateIpv4() {
		AutomationWebhookUrlValidator privateValidator = new AutomationWebhookUrlValidator(
				host -> List.of(InetAddress.getByName("10.1.2.3"))
		);
		
		assertThrows(UnsafeAutomationWebhookUrlException.class, () -> privateValidator.validateBeforeSend("https://example.com/webhook"));
	}
	
	@Test
	void rejectsHostnameResolvingToIpv6UniqueLocal() {
		AutomationWebhookUrlValidator privateValidator = new AutomationWebhookUrlValidator(
				host -> List.of(InetAddress.getByName("fc00::1"))
		);
		
		assertThrows(UnsafeAutomationWebhookUrlException.class, () -> privateValidator.validateBeforeSend("https://example.com/webhook"));
	}
	
	@Test
	void rejectsAnyUnsafeAddressAmongMultipleResolvedAddresses() {
		AutomationWebhookUrlValidator mixedValidator = new AutomationWebhookUrlValidator(
				host -> List.of(InetAddress.getByName("93.184.216.34"), InetAddress.getByName("127.0.0.1"))
		);
		
		assertThrows(UnsafeAutomationWebhookUrlException.class, () -> mixedValidator.validateBeforeSend("https://example.com/webhook"));
	}
	
	@Test
	void resolutionFailureProducesExpectedException() {
		AutomationWebhookUrlValidator failingValidator = new AutomationWebhookUrlValidator(
				host -> {
					throw new java.net.UnknownHostException(host);
				}
		);
		
		assertThrows(AutomationWebhookDnsResolutionException.class, () -> failingValidator.validateBeforeSend("https://missing.example/webhook"));
	}
}
