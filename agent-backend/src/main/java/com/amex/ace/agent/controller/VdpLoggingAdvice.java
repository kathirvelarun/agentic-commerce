package com.amex.ace.agent.controller;

import com.amex.ace.agent.service.vdp.VdpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;
import java.util.Map;

/**
 * Appends VDP request/response logs to every JSON response body.
 *
 * Replaces Python main.py add_vdp_logging_middleware:
 *   data['logs'] = logs  (injected into response JSON before returning)
 */
@RestControllerAdvice
public class VdpLoggingAdvice implements ResponseBodyAdvice<Object> {

    @Autowired(required = false)
    private VdpClient vdpClient;

    @Override
    public boolean supports(MethodParameter returnType,
                             Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (vdpClient == null) return body;
        List<Map<String, Object>> logs = vdpClient.getLogs();
        if (logs.isEmpty()) return body;

        if (body instanceof Map) {
            ((Map<String, Object>) body).put("logs", logs);
        }
        return body;
    }
}
