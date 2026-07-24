package com.umityasincoban.insightflow.shared.error;

import com.umityasincoban.insightflow.automation.application.AutomationExecutionNotFoundException;
import com.umityasincoban.insightflow.automation.application.AutomationRuleDeletionNotAllowedException;
import com.umityasincoban.insightflow.automation.application.AutomationRuleNotFoundException;
import com.umityasincoban.insightflow.feedback.application.FeedbackNotFoundException;
import com.umityasincoban.insightflow.knowledge.application.KnowledgeAssistantUnavailableException;
import com.umityasincoban.insightflow.shared.observability.CorrelationId;
import com.umityasincoban.insightflow.shared.tenancy.TenantAccessDeniedException;
import com.umityasincoban.insightflow.shared.tenancy.TenantNotResolvedException;
import com.umityasincoban.insightflow.tenancy.application.TenantAlreadyExistsException;
import com.umityasincoban.insightflow.tenancy.application.TenantInactiveException;
import com.umityasincoban.insightflow.tenancy.application.TenantNotFoundException;
import com.umityasincoban.insightflow.customer.application.CustomerAlreadyExistsException;
import com.umityasincoban.insightflow.customer.application.CustomerNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(TenantAlreadyExistsException.class)
	public ProblemDetail handleTenantAlreadyExists(
			TenantAlreadyExistsException exception,
			HttpServletRequest request
	) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.CONFLICT,
				exception.getMessage()
		);
		
		problemDetail.setTitle("Tenant already exists");
		problemDetail.setType(URI.create("https://insightflow.dev/problems/tenant-already-exists"));
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("errorCode", "TENANT_ALREADY_EXISTS");
		problemDetail.setProperty("timestamp", OffsetDateTime.now());
		problemDetail.setProperty("correlationId", MDC.get(CorrelationId.MDC_KEY));
		return problemDetail;
	}
	
	@ExceptionHandler(TenantNotFoundException.class)
	public ProblemDetail handleTenantNotFound(
			TenantNotFoundException exception,
			HttpServletRequest request
	) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.NOT_FOUND,
				exception.getMessage()
		);
		
		problemDetail.setTitle("Tenant not found");
		problemDetail.setType(URI.create("https://insightflow.dev/problems/tenant-not-found"));
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("errorCode", "TENANT_NOT_FOUND");
		problemDetail.setProperty("timestamp", OffsetDateTime.now());
		problemDetail.setProperty("correlationId", MDC.get(CorrelationId.MDC_KEY));
		return problemDetail;
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidationError(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {
		List<Map<String, String>> fieldErrors = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(GlobalExceptionHandler::toFieldErrorResponse)
				.toList();
		
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				"Request validation failed"
		);
		
		problemDetail.setTitle("Validation failed");
		problemDetail.setType(URI.create("https://insightflow.dev/problems/validation-error"));
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("errorCode", "VALIDATION_ERROR");
		problemDetail.setProperty("timestamp", OffsetDateTime.now());
		problemDetail.setProperty("errors", fieldErrors);
		problemDetail.setProperty("correlationId", MDC.get(CorrelationId.MDC_KEY));
		return problemDetail;
	}
	
	@ExceptionHandler(Exception.class)
	public ProblemDetail handleUnexpectedError(
			Exception exception,
			HttpServletRequest request
	) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"An unexpected error occurred"
		);
		
		problemDetail.setTitle("Internal server error");
		problemDetail.setType(URI.create("https://insightflow.dev/problems/internal-server-error"));
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("errorCode", "INTERNAL_SERVER_ERROR");
		problemDetail.setProperty("timestamp", OffsetDateTime.now());
		problemDetail.setProperty("correlationId", MDC.get(CorrelationId.MDC_KEY));
		
		return problemDetail;
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ProblemDetail handleIllegalArgument(
			IllegalArgumentException exception,
			HttpServletRequest request
	) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				exception.getMessage()
		);
		
		problemDetail.setTitle("Invalid request");
		problemDetail.setType(URI.create("https://insightflow.dev/problems/invalid-request"));
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("errorCode", "INVALID_REQUEST");
		addCommonProperties(problemDetail);
		
		return problemDetail;
	}
	
	@ExceptionHandler(TenantNotResolvedException.class)
	public ProblemDetail handleTenantNotResolved(
			TenantNotResolvedException exception,
			HttpServletRequest request
	) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				exception.getMessage()
		);
		
		problemDetail.setTitle("Tenant not resolved");
		problemDetail.setType(URI.create("https://insightflow.dev/problems/tenant-not-resolved"));
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("errorCode", "TENANT_NOT_RESOLVED");
		addCommonProperties(problemDetail);
		
		return problemDetail;
	}
	
	@ExceptionHandler(TenantInactiveException.class)
	public ProblemDetail handleTenantInactive(
			TenantInactiveException exception,
			HttpServletRequest request
	) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.FORBIDDEN,
				exception.getMessage()
		);
		
		problemDetail.setTitle("Tenant inactive");
		problemDetail.setType(URI.create("https://insightflow.dev/problems/tenant-inactive"));
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("errorCode", "TENANT_INACTIVE");
		addCommonProperties(problemDetail);
		
		return problemDetail;
	}
	
	@ExceptionHandler(FeedbackNotFoundException.class)
	public ProblemDetail handleFeedbackNotFound(
			FeedbackNotFoundException exception,
			HttpServletRequest request
	) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.NOT_FOUND,
				exception.getMessage()
		);
		
		problemDetail.setTitle("Feedback not found");
		problemDetail.setType(URI.create("https://insightflow.dev/problems/feedback-not-found"));
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("errorCode", "FEEDBACK_NOT_FOUND");
		addCommonProperties(problemDetail);
		
		return problemDetail;
	}
	
	@ExceptionHandler(TenantAccessDeniedException.class)
	public ProblemDetail handleTenantAccessDenied(
			TenantAccessDeniedException exception,
			HttpServletRequest request
	) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.FORBIDDEN,
				"Access is denied"
		);
		
		problemDetail.setTitle("Access denied");
		problemDetail.setType(URI.create("https://insightflow.dev/problems/tenant-access-denied"));
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("errorCode", "TENANT_ACCESS_DENIED");
		addCommonProperties(problemDetail);
		
		return problemDetail;
	}
	
	@ExceptionHandler(AutomationRuleNotFoundException.class)
	public ProblemDetail handleAutomationRuleNotFound(
			AutomationRuleNotFoundException exception,
			HttpServletRequest request
	) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.NOT_FOUND,
				exception.getMessage()
		);
		
		problemDetail.setTitle("Automation rule not found");
		problemDetail.setType(URI.create("https://insightflow.dev/problems/automation-rule-not-found"));
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("errorCode", "AUTOMATION_RULE_NOT_FOUND");
		addCommonProperties(problemDetail);
		
		return problemDetail;
	}
	
	@ExceptionHandler(AutomationExecutionNotFoundException.class)
	public ProblemDetail handleAutomationExecutionNotFound(
			AutomationExecutionNotFoundException exception,
			HttpServletRequest request
	) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.NOT_FOUND,
				exception.getMessage()
		);
		
		problemDetail.setTitle("Automation execution not found");
		problemDetail.setType(URI.create("https://insightflow.dev/problems/automation-execution-not-found"));
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("errorCode", "AUTOMATION_EXECUTION_NOT_FOUND");
		addCommonProperties(problemDetail);
		
		return problemDetail;
	}
	
	@ExceptionHandler(AutomationRuleDeletionNotAllowedException.class)
	public ProblemDetail handleAutomationRuleDeletionNotAllowed(
			AutomationRuleDeletionNotAllowedException exception,
			HttpServletRequest request
	) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.CONFLICT,
				exception.getMessage()
		);
		
		problemDetail.setTitle("Automation rule deletion not allowed");
		problemDetail.setType(URI.create("https://insightflow.dev/problems/automation-rule-deletion-not-allowed"));
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("errorCode", "AUTOMATION_RULE_DELETION_NOT_ALLOWED");
		addCommonProperties(problemDetail);
		
		return problemDetail;
	}
	
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ProblemDetail handleMethodArgumentTypeMismatch(
			MethodArgumentTypeMismatchException exception,
			HttpServletRequest request
	) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				"Request parameter has an invalid value"
		);
		
		problemDetail.setTitle("Invalid request parameter");
		problemDetail.setType(URI.create("https://insightflow.dev/problems/invalid-request-parameter"));
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("errorCode", "INVALID_REQUEST_PARAMETER");
		problemDetail.setProperty("parameter", exception.getName());
		problemDetail.setProperty("value", exception.getValue() == null ? null : exception.getValue().toString());
		problemDetail.setProperty("requiredType", exception.getRequiredType() == null
				? null
				: exception.getRequiredType().getSimpleName());
		addCommonProperties(problemDetail);
		
		return problemDetail;
	}
	
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ProblemDetail handleHttpMessageNotReadable(
			HttpMessageNotReadableException exception,
			HttpServletRequest request
	) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				"Request body is malformed or contains invalid values"
		);
		
		problemDetail.setTitle("Invalid request body");
		problemDetail.setType(URI.create("https://insightflow.dev/problems/invalid-request-body"));
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("errorCode", "INVALID_REQUEST_BODY");
		addCommonProperties(problemDetail);
		
		return problemDetail;
	}
	
	@ExceptionHandler(CustomerAlreadyExistsException.class)
	public ProblemDetail handleCustomerAlreadyExists(
			CustomerAlreadyExistsException exception,
			HttpServletRequest request
	) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.CONFLICT,
				exception.getMessage()
		);
		
		problemDetail.setTitle("Customer already exists");
		problemDetail.setType(URI.create("https://insightflow.dev/problems/customer-already-exists"));
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("errorCode", "CUSTOMER_ALREADY_EXISTS");
		addCommonProperties(problemDetail);
		
		return problemDetail;
	}
	
	@ExceptionHandler(CustomerNotFoundException.class)
	public ProblemDetail handleCustomerNotFound(
			CustomerNotFoundException exception,
			HttpServletRequest request
	) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.NOT_FOUND,
				exception.getMessage()
		);
		
		problemDetail.setTitle("Customer not found");
		problemDetail.setType(URI.create("https://insightflow.dev/problems/customer-not-found"));
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("errorCode", "CUSTOMER_NOT_FOUND");
		addCommonProperties(problemDetail);
		
		return problemDetail;
	}
	
	@ExceptionHandler(KnowledgeAssistantUnavailableException.class)
	public ProblemDetail handleKnowledgeAssistantUnavailable(
			KnowledgeAssistantUnavailableException exception,
			HttpServletRequest request
	) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.SERVICE_UNAVAILABLE,
				exception.getMessage()
		);
		
		problemDetail.setTitle("Knowledge assistant unavailable");
		problemDetail.setType(URI.create("https://insightflow.dev/problems/knowledge-assistant-unavailable"));
		problemDetail.setInstance(URI.create(request.getRequestURI()));
		problemDetail.setProperty("errorCode", "KNOWLEDGE_ASSISTANT_UNAVAILABLE");
		addCommonProperties(problemDetail);
		
		return problemDetail;
	}
	
	private static void addCommonProperties(ProblemDetail problemDetail) {
		problemDetail.setProperty("timestamp", OffsetDateTime.now());
		problemDetail.setProperty("correlationId", MDC.get(CorrelationId.MDC_KEY));
	}
	
	private static Map<String, String> toFieldErrorResponse(FieldError fieldError) {
		return Map.of(
				"field", fieldError.getField(),
				"message", fieldError.getDefaultMessage() == null
						? "Invalid value"
						: fieldError.getDefaultMessage()
		);
	}
}
