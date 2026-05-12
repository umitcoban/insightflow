package com.umityasincoban.insightflow;

import org.springframework.boot.SpringApplication;

public class TestInsightflowApplication {
	
	public static void main(String[] args) {
		SpringApplication.from(InsightflowApplication::main).with(TestcontainersConfiguration.class).run(args);
	}
	
}
