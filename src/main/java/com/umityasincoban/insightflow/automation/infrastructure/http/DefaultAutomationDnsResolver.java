package com.umityasincoban.insightflow.automation.infrastructure.http;

import com.umityasincoban.insightflow.automation.application.AutomationDnsResolver;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

@Component
public class DefaultAutomationDnsResolver implements AutomationDnsResolver {
	
	@Override
	public List<InetAddress> resolve(String host) throws UnknownHostException {
		return Arrays.asList(InetAddress.getAllByName(host));
	}
}
