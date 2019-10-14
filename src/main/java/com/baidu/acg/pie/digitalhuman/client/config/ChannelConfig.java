// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.config;

import lombok.Builder;
import lombok.Data;

import java.util.concurrent.TimeUnit;

/**
 * ChannelConfig
 *
 * @since 2019-08-31
 */
@Data
@Builder
public class ChannelConfig {

    /**
     * 注意，此处心跳时间必须大于30MIN，如果小于这个时间，服务器会检查并关闭该连接
     */
    @Builder.Default
    private long keepAliveTime = 3;

    @Builder.Default
    private long keepAliveTimeout = 40;

    @Builder.Default
    private TimeUnit timeUnit = TimeUnit.MINUTES;

    /**
     * 是否在同步请求时，复用长连接
     */
    @Builder.Default
    private boolean reuseStream = true;

    /**
     * -1 mean infinite, won't timeout forever
     */
    @Builder.Default
    private long syncRequestTimeout = -1;

    @Builder.Default
    private int syncMaxInflight = 10000;

}
