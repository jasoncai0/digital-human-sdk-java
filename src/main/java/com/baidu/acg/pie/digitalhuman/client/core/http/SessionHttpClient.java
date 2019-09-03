// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.core.http;

import com.baidu.acg.pie.digitalhuman.client.config.ClientConfig;
import com.baidu.acg.pie.digitalhuman.client.exception.DigitalHumanException;
import com.baidu.acg.pie.digitalhuman.client.exception.Error;
import com.baidu.acg.pie.digitalhuman.client.model.SessionResult;
import com.baidu.acg.pie.digitalhuman.client.model.request.SessionAcquireInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * HttpSessionClient
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
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
        return doHttpCall(() -> this.httpService.acquire(
                clientConfig.getAppName(), SessionAcquireInfo.builder().userName(clientConfig.getUserName())
                        .expireTime(clientConfig.getSessionExpireTime()).build())
                .execute());
    }

    public SessionResult query(String sessionId) {
        return doHttpCall(() -> this.httpService.query(clientConfig.getAppName(), sessionId).execute());
    }


    public void close(String sessionId) {
        doHttpCall(() -> this.httpService.close(clientConfig.getAppName(), sessionId).execute());
    }


    private <T> T doHttpCall(Callable<Response<T>> callable) {
        try {
            Response<T> response = callable.call();
            if (!response.isSuccessful()) {
                try (ResponseBody errorBody = response.errorBody()) {
                    String errorMessage = errorBody == null ? "unknown error message" : errorBody.string();
                    throw new DigitalHumanException(response.code(), errorMessage);
                }
            }
            return response.body();
        } catch (Exception e) {
            log.error("Fail to http  session service ", e);
            throw Error.HTTP_SERVER_ERROR.asException();
        }
    }

}
