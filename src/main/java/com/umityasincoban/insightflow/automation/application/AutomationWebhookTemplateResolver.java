package com.umityasincoban.insightflow.automation.application;

import com.umityasincoban.insightflow.outbox.application.OutboxEventMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AutomationWebhookTemplateResolver {
	
	private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_.]+)\\s*}}");
	
	public Object resolve(Object value, OutboxEventMessage eventMessage) {
		if (value instanceof Map<?, ?> map) {
			Map<String, Object> resolved = new LinkedHashMap<>();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				resolved.put(String.valueOf(entry.getKey()), resolve(entry.getValue(), eventMessage));
			}
			return resolved;
		}
		
		if (value instanceof List<?> list) {
			List<Object> resolved = new ArrayList<>(list.size());
			for (Object item : list) {
				resolved.add(resolve(item, eventMessage));
			}
			return resolved;
		}
		
		if (value instanceof String text) {
			return resolveString(text, eventMessage);
		}
		
		return value;
	}
	
	public Map<String, String> resolveHeaders(Map<String, String> headers, OutboxEventMessage eventMessage) {
		Map<String, String> resolved = new LinkedHashMap<>();
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			Object value = resolve(entry.getValue(), eventMessage);
			resolved.put(entry.getKey(), value == null ? "" : value.toString());
		}
		return resolved;
	}
	
	private static Object resolveString(String text, OutboxEventMessage eventMessage) {
		Matcher exactMatcher = PLACEHOLDER.matcher(text);
		if (exactMatcher.matches()) {
			return resolvePath(exactMatcher.group(1), eventMessage);
		}
		
		Matcher matcher = PLACEHOLDER.matcher(text);
		StringBuilder resolved = new StringBuilder();
		while (matcher.find()) {
			Object value = resolvePath(matcher.group(1), eventMessage);
			matcher.appendReplacement(resolved, Matcher.quoteReplacement(value == null ? "" : value.toString()));
		}
		matcher.appendTail(resolved);
		return resolved.toString();
	}
	
	private static Object resolvePath(String path, OutboxEventMessage eventMessage) {
		if (path.startsWith("event.")) {
			return resolveEventPath(path.substring("event.".length()), eventMessage);
		}
		
		if (path.startsWith("payload.")) {
			Map<String, Object> payload = eventMessage.payload() == null ? Map.of() : eventMessage.payload();
			return resolveMapPath(payload, path.substring("payload.".length()));
		}
		
		return null;
	}
	
	private static Object resolveEventPath(String field, OutboxEventMessage eventMessage) {
		return switch (field) {
			case "eventId" -> eventMessage.eventId();
			case "tenantId" -> eventMessage.tenantId();
			case "aggregateType" -> eventMessage.aggregateType();
			case "aggregateId" -> eventMessage.aggregateId();
			case "eventType" -> eventMessage.eventType();
			case "eventVersion" -> eventMessage.eventVersion();
			case "createdAt" -> eventMessage.createdAt();
			default -> null;
		};
	}
	
	private static Object resolveMapPath(Map<String, Object> source, String path) {
		Object current = source;
		for (String segment : path.split("\\.")) {
			if (!(current instanceof Map<?, ?> map) || !map.containsKey(segment)) {
				return null;
			}
			current = map.get(segment);
		}
		return current;
	}
}
