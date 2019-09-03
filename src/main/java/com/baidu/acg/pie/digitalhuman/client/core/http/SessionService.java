// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.core.http;

import com.baidu.acg.pie.digitalhuman.client.model.SessionResult;
import com.baidu.acg.pie.digitalhuman.client.model.request.SessionAcquireInfo;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * HttpService
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
 * @since 2019-08-31
 */
public interface SessionService {

    @POST("/api/digitalhuman/v1/app/{app}/session")
    Call<SessionResult> acquire(@Path("app") String app, @Body SessionAcquireInfo sessionAcquireInfo);


    @GET("/api/digitalhuman/v1/app/{app}/session/{sessionId}")
    Call<SessionResult> query(@Path("app") String app, @Path("sessionId") String sessionId);


    @DELETE("/api/digialhuman/v1/app/{app}/session/{sessionId}")
    Call<Void> close(@Path("app") String app, @Path("sessionId") String sessionId);

}
