package com.umityasincoban.insightflow.shared.error;

import com.umityasincoban.insightflow.feedback.application.FeedbackNotFoundException;
import com.umityasincoban.insightflow.shared.observability.CorrelationId;
import com.umityasincoban.insightflow.shared.tenancy.TenantNotResolvedException;
import com.umityasincoban.insightflow.tenancy.application.TenantAlreadyExistsException;
import com.umityasincoban.insightflow.tenancy.application.TenantInactiveException;
import com.umityasincoban.insightflow.tenancy.application.TenantNotFoundException;
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