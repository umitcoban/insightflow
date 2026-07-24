package com.umityasincoban.insightflow.search.application;

import com.umityasincoban.insightflow.shared.api.PageResponse;
import com.umityasincoban.insightflow.tenancy.domain.TenantId;

public interface FeedbackSearchPort {
	
	void index(FeedbackSearchDocument document);
	
	PageResponse<FeedbackSearchDocument> search(TenantId tenantId, FeedbackSearchQuery query);
}

