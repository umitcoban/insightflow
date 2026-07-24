package com.umityasincoban.insightflow.knowledge.api;

import com.umityasincoban.insightflow.knowledge.application.KnowledgeApplicationService;
import com.umityasincoban.insightflow.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/knowledge/documents")
public class KnowledgeDocumentController {
	
	private final KnowledgeApplicationService knowledgeApplicationService;
	
	public KnowledgeDocumentController(KnowledgeApplicationService knowledgeApplicationService) {
		this.knowledgeApplicationService = knowledgeApplicationService;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public KnowledgeDocumentResponse create(@Valid @RequestBody CreateKnowledgeDocumentRequest request) {
		return KnowledgeDocumentResponse.from(knowledgeApplicationService.createDocument(
				request.title(),
				request.source(),
				request.content(),
				request.metadata()
		));
	}
	
	@GetMapping
	public PageResponse<KnowledgeDocumentResponse> list(
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size
	) {
		PageResponse<com.umityasincoban.insightflow.knowledge.domain.KnowledgeDocument> documents = knowledgeApplicationService.listDocuments(page, size);
		return new PageResponse<>(
				documents.content().stream().map(KnowledgeDocumentResponse::from).toList(),
				documents.page(),
				documents.size(),
				documents.totalElements(),
				documents.totalPages(),
				documents.first(),
				documents.last()
		);
	}
	
	@DeleteMapping("/{documentId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID documentId) {
		knowledgeApplicationService.deleteDocument(documentId);
	}
}

