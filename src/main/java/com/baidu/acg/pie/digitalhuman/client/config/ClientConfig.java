// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.config;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * ClientConfig
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
 * @since 2019-08-29
 */
@Data
@Builder
public class ClientConfig {


    @NonNull
    @Builder.Default
    private final ChannelConfig channelConfig = ChannelConfig.builder().build();
    private String httpHost;
    private int httpPort;
    @NonNull
    private String serverHost;
    @NonNull
    private int serverPort;
    private String appName;
    private String userName;
    @Builder.Default
    private String sessionExpireTime = "60000";
    private String accessKey;
    private String accessSecret;


//    private SessionMeta providedSessionMeta;


}
