package com.umityasincoban.insightflow.knowledge.api;

import com.umityasincoban.insightflow.knowledge.application.AssistantAnswer;

import java.util.List;
import java.util.UUID;

public record AssistantAnswerResponse(
		String answer,
		List<Source> sources
) {
	
	public static AssistantAnswerResponse from(AssistantAnswer answer) {
		return new AssistantAnswerResponse(
				answer.answer(),
				answer.sources().stream()
						.map(source -> new Source(source.documentId(), source.chunkId(), source.documentTitle(), source.source(), source.content(), source.score()))
						.toList()
		);
	}
	
	public record Source(
			UUID documentId,
			UUID chunkId,
			String documentTitle,
			String source,
			String content,
			double score
	) {
	}
}

