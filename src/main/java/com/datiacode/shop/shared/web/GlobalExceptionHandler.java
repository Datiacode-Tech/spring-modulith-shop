package com.datiacode.shop.shared.web;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Application-wide fallback for framework exceptions (validation, malformed JSON, unsupported media type...),
 * rendered as RFC 9457 {@code ProblemDetail}. Module-specific exceptions are mapped by each module's own advice.
 */
@RestControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
}
