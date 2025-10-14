package ru.kharevich.postservice.config;

import feign.Logger;
import feign.Request;
import feign.Response;
import feign.Util;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class FeignFullLogger extends Logger {

    @Override
    protected void log(String configKey, String format, Object... args) {
        log.info(String.format(methodTag(configKey) + format, args));
    }

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        if (logLevel.ordinal() >= Level.FULL.ordinal()) {
            super.logRequest(configKey, logLevel, request);

            // Логируем тело запроса
            if (request.body() != null && request.body().length > 0) {
                try {
                    String bodyText = new String(request.body(), StandardCharsets.UTF_8);
                    log.info("{} Request body: {}", methodTag(configKey), bodyText);
                } catch (Exception e) {
                    log.info("{} Request body: [binary data, {} bytes]", methodTag(configKey), request.body().length);
                }
            }

            // Логируем все заголовки
            log.info("{} Request headers: {}", methodTag(configKey), request.headers());
        }
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime)
            throws IOException {
        Response loggedResponse = super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime);

        if (logLevel.ordinal() >= Level.FULL.ordinal() && loggedResponse.body() != null) {
            byte[] bodyData = Util.toByteArray(loggedResponse.body().asInputStream());
            String bodyText = new String(bodyData, StandardCharsets.UTF_8);
            log.info("{} Response body: {}", methodTag(configKey), bodyText);

            return Response.builder()
                    .body(bodyData)
                    .headers(loggedResponse.headers())
                    .reason(loggedResponse.reason())
                    .status(loggedResponse.status())
                    .request(loggedResponse.request())
                    .build();
        }

        return loggedResponse;
    }
}
