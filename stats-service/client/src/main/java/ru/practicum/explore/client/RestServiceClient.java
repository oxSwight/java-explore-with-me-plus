package ru.practicum.explore.client;

import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

public class RestServiceClient {
    private final RestTemplate httpTemplate;

    public RestServiceClient(RestTemplate restTemplate) {
        this.httpTemplate = restTemplate;
    }

    protected ResponseEntity<Object> fetch(String endpoint, @Nullable Map<String, Object> queryParams) {
        return executeHttpRequest(HttpMethod.GET, endpoint, queryParams, null);
    }

    protected <T> ResponseEntity<Object> submit(String endpoint, T payload) {
        return executeHttpRequest(HttpMethod.POST, endpoint, null, payload);
    }

    private <T> ResponseEntity<Object> executeHttpRequest(
            HttpMethod httpVerb,
            String resourcePath,
            @Nullable Map<String, Object> variables,
            @Nullable T requestPayload
    ) {
        HttpHeaders standardHeaders = generateHeaders();
        HttpEntity<T> request = new HttpEntity<>(requestPayload, standardHeaders);

        try {
            ResponseEntity<Object> serverResponse;
            if (variables != null && !variables.isEmpty()) {
                serverResponse = httpTemplate.exchange(
                        resourcePath,
                        httpVerb,
                        request,
                        Object.class,
                        variables
                );
            } else {
                serverResponse = httpTemplate.exchange(
                        resourcePath,
                        httpVerb,
                        request,
                        Object.class
                );
            }
            return transformResponse(serverResponse);
        } catch (HttpStatusCodeException serverError) {
            return ResponseEntity
                    .status(serverError.getStatusCode())
                    .body(serverError.getResponseBodyAsByteArray());
        }
    }

    private HttpHeaders generateHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private static ResponseEntity<Object> transformResponse(ResponseEntity<Object> originResponse) {
        if (originResponse.getStatusCode().is2xxSuccessful()) {
            return originResponse;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(originResponse.getStatusCode());

        if (originResponse.hasBody()) {
            return responseBuilder.body(originResponse.getBody());
        } else {
            return responseBuilder.build();
        }
    }
}