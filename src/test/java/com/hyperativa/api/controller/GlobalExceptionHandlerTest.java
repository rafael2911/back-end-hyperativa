package com.hyperativa.api.controller;

import com.hyperativa.api.dto.ErrorResponseDTO;
import com.hyperativa.api.exception.ApiException;
import com.hyperativa.api.exception.CardAlreadyExistsException;
import com.hyperativa.api.exception.InvalidTokenException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.naming.AuthenticationException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    // helper dummy method to create MethodParameter
    // This method is intentionally empty; it's used only to obtain a MethodParameter
    public void dummyMethod(@SuppressWarnings("unused") DummyDto dto) {
        // intentionally left blank for reflection-based tests
    }

    static class DummyDto {
        @SuppressWarnings("unused")
        public String field1;
    }

    @Test
    void testHandleValidationException() throws NoSuchMethodException {
        MethodParameter param = new MethodParameter(this.getClass().getMethod("dummyMethod", DummyDto.class), 0);
        DummyDto dto = new DummyDto();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(dto, "dummyDto");
        FieldError fe = new FieldError("dummyDto", "field1", "must not be blank");
        bindingResult.addError(fe);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindingResult);
        when(request.getRequestURI()).thenReturn("/v1/test");

        ResponseEntity<Map<String, Object>> resp = handler.handleValidationException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("BAD_REQUEST", resp.getBody().get("status"));
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) resp.getBody().get("errors");
        assertTrue(errors.containsKey("field1"));
        assertEquals("must not be blank", errors.get("field1"));
    }

    @Test
    void testHandleCardAlreadyExists() {
        CardAlreadyExistsException ex = new CardAlreadyExistsException("Card exists");
        when(request.getRequestURI()).thenReturn("/v1/cards");

        ResponseEntity<ErrorResponseDTO> resp = handler.handleCardAlreadyExists(ex, request);

        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("Card exists", resp.getBody().getMessage());
        assertEquals("CONFLICT", resp.getBody().getStatus());
    }

    @Test
    void testHandleAuthenticationException() {
        AuthenticationException ex = new AuthenticationException("auth failed");
        when(request.getRequestURI()).thenReturn("/v1/auth");

        ResponseEntity<ErrorResponseDTO> resp = handler.handleAuthenticationException(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("UNAUTHORIZED", resp.getBody().getStatus());
        assertTrue(resp.getBody().getMessage().contains("auth failed"));
    }

    @Test
    void testHandleBadCredentials() {
        BadCredentialsException ex = new BadCredentialsException("bad creds");
        when(request.getRequestURI()).thenReturn("/v1/auth/token");

        ResponseEntity<ErrorResponseDTO> resp = handler.handleBadCredentials(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("UNAUTHORIZED", resp.getBody().getStatus());
        assertEquals("Invalid username or password", resp.getBody().getMessage());
    }

    @Test
    void testHandleInvalidToken() {
        InvalidTokenException ex = new InvalidTokenException("token invalid");
        when(request.getRequestURI()).thenReturn("/v1/resource");

        ResponseEntity<ErrorResponseDTO> resp = handler.handleInvalidToken(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("FORBIDDEN", resp.getBody().getStatus());
        assertEquals("token invalid", resp.getBody().getMessage());
    }

    @Test
    void testApiException() {
        ApiException ex = new ApiException("api err", HttpStatus.BAD_REQUEST);

        ResponseEntity<ErrorResponseDTO> resp = handler.apiException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("BAD_REQUEST", resp.getBody().getStatus());
        assertEquals("api err", resp.getBody().getMessage());
    }

    @Test
    void testHandleRuntimeException() {
        RuntimeException ex = new RuntimeException("runtime");
        when(request.getRequestURI()).thenReturn("/v1/runtime");

        ResponseEntity<ErrorResponseDTO> resp = handler.handleRuntimeException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("INTERNAL_SERVER_ERROR", resp.getBody().getStatus());
        assertEquals("runtime", resp.getBody().getMessage());
    }

    @Test
    void testHandleGlobalException() {
        Exception ex = new Exception("boom");
        when(request.getRequestURI()).thenReturn("/v1/global");

        ResponseEntity<ErrorResponseDTO> resp = handler.handleGlobalException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("INTERNAL_SERVER_ERROR", resp.getBody().getStatus());
        assertEquals("An unexpected error occurred", resp.getBody().getMessage());
    }
}
