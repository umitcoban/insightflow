package com.umityasincoban.insightflow.automation.application;

import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

@Component
public class AutomationWebhookUrlValidator {
	
	public URI validate(String url) {
		URI uri = parse(url);
		String scheme = uri.getScheme();
		if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
			throw new UnsafeAutomationWebhookUrlException("Webhook URL scheme must be http or https");
		}
		
		if (uri.getRawUserInfo() != null) {
			throw new UnsafeAutomationWebhookUrlException("Webhook URL must not include credentials");
		}
		
		String host = uri.getHost();
		if (host == null || host.isBlank()) {
			throw new UnsafeAutomationWebhookUrlException("Webhook URL host is required");
		}
		
		validateHost(host);
		return uri;
	}
	
	private static URI parse(String url) {
		if (url == null || url.isBlank()) {
			throw new InvalidAutomationWebhookConfigurationException("Webhook URL is required");
		}
		
		try {
			return new URI(url);
		} catch (URISyntaxException exception) {
			throw new UnsafeAutomationWebhookUrlException("Webhook URL is malformed");
		}
	}
	
	private static void validateHost(String host) {
		String normalized = host.toLowerCase(Locale.ROOT);
		if ("localhost".equals(normalized) || normalized.endsWith(".localhost")) {
			throw new UnsafeAutomationWebhookUrlException("Webhook URL localhost targets are not allowed");
		}
		
		if (isIpLiteral(normalized)) {
			validateIpLiteral(normalized);
		}
	}
	
	private static boolean isIpLiteral(String host) {
		return host.indexOf(':') >= 0 || host.chars().allMatch(character -> Character.isDigit(character) || character == '.');
	}
	
	private static void validateIpLiteral(String host) {
		try {
			InetAddress address = InetAddress.getByName(host);
			if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
					|| isPrivateAddress(address)) {
				throw new UnsafeAutomationWebhookUrlException("Webhook URL private or local targets are not allowed");
			}
		} catch (UnsafeAutomationWebhookUrlException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new UnsafeAutomationWebhookUrlException("Webhook URL host is malformed");
		}
	}
	
	private static boolean isPrivateAddress(InetAddress address) {
		if (address instanceof Inet4Address) {
			byte[] bytes = address.getAddress();
			int first = Byte.toUnsignedInt(bytes[0]);
			int second = Byte.toUnsignedInt(bytes[1]);
			return first == 10 || (first == 172 && second >= 16 && second <= 31) || (first == 192 && second == 168);
		}
		
		if (address instanceof Inet6Address) {
			byte[] bytes = address.getAddress();
			int first = Byte.toUnsignedInt(bytes[0]);
			return (first & 0xfe) == 0xfc;
		}
		
		return false;
	}
}
