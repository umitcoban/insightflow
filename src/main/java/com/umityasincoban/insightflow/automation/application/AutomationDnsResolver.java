package com.umityasincoban.insightflow.automation.application;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public interface AutomationDnsResolver {
	
	List<InetAddress> resolve(String host) throws UnknownHostException;
}
