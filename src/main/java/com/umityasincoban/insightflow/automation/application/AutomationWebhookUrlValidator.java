package com.umityasincoban.insightflow.automation.application;

import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Locale;

@Component
public class AutomationWebhookUrlValidator {
	
	private final AutomationDnsResolver dnsResolver;
	
	public AutomationWebhookUrlValidator(AutomationDnsResolver dnsResolver) {
		this.dnsResolver = dnsResolver;
	}
	
	public URI validate(String url) {
		return validate(url, false);
	}
	
	public URI validateBeforeSend(String url) {
		return validate(url, true);
	}
	
	private URI validate(String url, boolean resolveDns) {
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
		
		validateHost(host, resolveDns);
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
	
	private void validateHost(String host, boolean resolveDns) {
		String normalized = host.toLowerCase(Locale.ROOT);
		if ("localhost".equals(normalized) || normalized.endsWith(".localhost")) {
			throw new UnsafeAutomationWebhookUrlException("Webhook URL localhost targets are not allowed");
		}
		
		if (isIpLiteral(normalized)) {
			validateIpLiteral(normalized);
			return;
		}
		
		if (resolveDns) {
			try {
				for (InetAddress address : dnsResolver.resolve(host)) {
					validateAddress(address);
				}
			} catch (UnknownHostException exception) {
				throw new AutomationWebhookDnsResolutionException("Webhook URL host could not be resolved", exception);
			}
		}
	}
	
	private static boolean isIpLiteral(String host) {
		return host.indexOf(':') >= 0 || host.chars().allMatch(character -> Character.isDigit(character) || character == '.');
	}
	
	private static void validateIpLiteral(String host) {
		try {
			InetAddress address = InetAddress.getByName(host);
			validateAddress(address);
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
			return first == 10
					|| (first == 172 && second >= 16 && second <= 31)
					|| (first == 192 && second == 168)
					|| (first == 100 && second >= 64 && second <= 127)
					|| (first == 192 && second == 0)
					|| (first == 192 && second == 0 && Byte.toUnsignedInt(bytes[2]) == 2)
					|| (first == 198 && (second == 18 || second == 19))
					|| (first == 198 && second == 51 && Byte.toUnsignedInt(bytes[2]) == 100)
					|| (first == 203 && second == 0 && Byte.toUnsignedInt(bytes[2]) == 113);
		}
		
		if (address instanceof Inet6Address) {
			byte[] bytes = address.getAddress();
			int first = Byte.toUnsignedInt(bytes[0]);
			return (first & 0xfe) == 0xfc || (first == 0xfe && (Byte.toUnsignedInt(bytes[1]) & 0xc0) == 0x80)
					|| isPrivateMappedIpv4(bytes);
		}
		
		return false;
	}
	
	private static void validateAddress(InetAddress address) {
		if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
				|| address.isSiteLocalAddress() || address.isMulticastAddress() || isPrivateAddress(address)) {
			throw new UnsafeAutomationWebhookUrlException("Webhook URL private or local targets are not allowed");
		}
	}
	
	private static boolean isPrivateMappedIpv4(byte[] bytes) {
		if (bytes.length != 16) {
			return false;
		}
		for (int index = 0; index < 10; index++) {
			if (bytes[index] != 0) {
				return false;
			}
		}
		if (bytes[10] != (byte) 0xff || bytes[11] != (byte) 0xff) {
			return false;
		}
		
		try {
			return isPrivateAddress(InetAddress.getByAddress(new byte[] {bytes[12], bytes[13], bytes[14], bytes[15]}));
		} catch (Exception exception) {
			return true;
		}
	}
}
