package com.umityasincoban.insightflow.search.infrastructure.elasticsearch;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "insightflow.search.elasticsearch")
public class ElasticsearchProperties {
	
	private String baseUrl = "http://localhost:9200";
	private String username;
	private String password;
	private String feedbackIndex = "insightflow-feedbacks";
	private String knowledgeIndex = "insightflow-knowledge-chunks";
	
	public String getBaseUrl() {
		return baseUrl;
	}
	
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getFeedbackIndex() {
		return feedbackIndex;
	}
	
	public void setFeedbackIndex(String feedbackIndex) {
		this.feedbackIndex = feedbackIndex;
	}
	
	public String getKnowledgeIndex() {
		return knowledgeIndex;
	}
	
	public void setKnowledgeIndex(String knowledgeIndex) {
		this.knowledgeIndex = knowledgeIndex;
	}
}

