package com.lovely4k.backend.common.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;

import java.util.Map;

public record HttpLogResponse(
    String ipAddress,
    String header,
    String uri,
    String requestBody,
    String responseBody,
    Map<String, String[]> parameter,
    String httpMethod,
    int status
) {

    @SneakyThrows
    public static HttpLogResponse of(HttpServletRequest request, HttpServletResponse response, ObjectMapper objectMapper, Object proceed) {
        String header = request.getHeader("User-Agent");

        String uri = request.getRequestURI();
        String httpMethod = request.getMethod();
        RequestWrapper requestWrapper = new RequestWrapper(request);
        String requestBody = requestWrapper.getBody(objectMapper);
        Map<String, String[]> parameter = request.getParameterMap();
        int status = response.getStatus();
        String responseBody = proceed != null ? proceed.toString() : null;

        return new HttpLogResponse(getIpAddress(request), header, uri, requestBody, responseBody, parameter, httpMethod, status);
    }

    private static String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For"); // 실제 클라이언트 IP
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr(); // 프록시를 거치지 않았다면 이 IP를 사용
        }
        return ipAddress;
    }

    public HttpLogResponse removeBody() {
        return new HttpLogResponse(ipAddress, header, uri, "", "", parameter, httpMethod, status);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ipAddress=").append(ipAddress)
            .append(", header=").append(header)
            .append(", uri=").append(uri)
            .append(", requestBody=").append(requestBody)
            .append(", responseBody=").append(responseBody)
            .append(", httpMethod=").append(httpMethod)
            .append(", status=").append(status);

        sb.append(", parameter=");
        parameter.forEach((key, values) -> {
            sb.append(key).append(":[");
            sb.append(String.join(", ", values));
            sb.append("] ");
        });

        return sb.toString();
    }

}