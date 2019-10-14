// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.core.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;

import com.baidu.acg.pie.digitalhuman.client.config.ClientConfig;
import com.baidu.acg.pie.digitalhuman.client.exception.DigitalHumanException;
import com.baidu.acg.pie.digitalhuman.client.model.SessionResult;
import com.baidu.acg.pie.digitalhuman.client.model.request.SessionAcquireInfo;
import com.baidu.acg.pie.digitalhuman.client.util.SignUtil;

/**
 * HttpSessionClient
 *
 * @since 2019-08-31
 */
@Slf4j
public class SessionHttpClient {

    private final ClientConfig clientConfig;

    private SessionService httpService;

    public SessionHttpClient(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        String httpHost = clientConfig.getHttpHost();
        int httpPort = clientConfig.getHttpPort();
        // use https?
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(String.join(":", "http", "//" + httpHost, "" + httpPort))
                .client(new OkHttpClient())
                .addConverterFactory(JacksonConverterFactory.create(new ObjectMapper()))
                .build();
        this.httpService = retrofit.create(SessionService.class);
    }

    public SessionResult acquire() {
        ZonedDateTime expireTime;
        if (StringUtils.isEmpty(clientConfig.getAppId())) {
            throw DigitalHumanException.fail("app id not present");
        }

        if (StringUtils.isEmpty(clientConfig.getAppKey())) {
            throw DigitalHumanException.fail("app key not present");
        }

        if (clientConfig.getAcquireTimeoutSeconds() < 0) {
            if (clientConfig.getAcquireTimeoutSeconds() == -1) {
                expireTime = ZonedDateTime.now().plusYears(100);
            } else {
                throw DigitalHumanException.fail("invalid expire time");
            }
        } else {
            expireTime = ZonedDateTime.now().plusSeconds(clientConfig.getAcquireTimeoutSeconds());
        }
        String expireTimeText = expireTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return doHttpCall(() -> this.httpService.acquire(
                clientConfig.getAppId(),
                SessionAcquireInfo.builder()
                        .signature(SignUtil.sign(clientConfig.getAppKey(), clientConfig.getAppId(), expireTimeText))
                        .expireTime(expireTimeText).build())
                .execute());
    }

    public SessionResult query(String sessionId) {
        return doHttpCall(() -> this.httpService.query(clientConfig.getAppId(), sessionId).execute());
    }

    public void close(String sessionId) {
        doHttpCall(() -> this.httpService.close(clientConfig.getAppId(), sessionId).execute());
    }

    private <T> T doHttpCall(Callable<Response<T>> callable) {
        try {
            Response<T> response = callable.call();
            if (!response.isSuccessful()) {
                try (ResponseBody errorBody = response.errorBody()) {
                    String errorMessage = errorBody == null ? "unknown error message" : errorBody.string();
                    throw DigitalHumanException.fail(response.code(), errorMessage);
                }
            }
            return response.body();
        } catch (DigitalHumanException e) {
            log.error("Fail to call http service", e);
            throw e;
        } catch (Exception e) {
            log.error("Fail to call http service ", e);
            throw DigitalHumanException.fail("call http service failed", e);
        }
    }

}
