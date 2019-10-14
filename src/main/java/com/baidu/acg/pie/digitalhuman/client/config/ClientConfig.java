// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.config;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * ClientConfig
 *
 * @since 2019-08-29
 */
@Data
@Builder
public class ClientConfig {


    @NonNull
    @Builder.Default
    private final ChannelConfig channelConfig = ChannelConfig.builder().build();
    @NonNull
    private String serverHost;
    @NonNull
    private Integer serverPort;

    private String httpHost;

    private Integer httpPort;

    private String appId;

    private String appKey;


    /**
     * -1 means infinite, other negative invalid
     */
    @Builder.Default
    private long acquireTimeoutSeconds = 60 * 60;

//
//    @Deprecated
//    @Builder.Default
//    private String sessionExpireTime = "60000";


    private String accessKey;
    private String accessSecret;

    @Builder.Default
    private long requestTimeoutSeconds = 120;

//    private SessionMeta providedSessionMeta;


}
