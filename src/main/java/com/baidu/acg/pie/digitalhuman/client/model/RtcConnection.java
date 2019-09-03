// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RtcConnection
 * Rtc连接信息用于连接BRTC(百度云webRtc)，拉取数字人的视频流。
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
 * @since 2019-08-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class RtcConnection {
    private String rtcServerUrl;
    private String appId;
    private String roomName;
    private String clientId;
    private String clientToken;
    private String feedId;
}
