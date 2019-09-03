// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.model.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

/**
 * SessionAcquireInfo
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
 * @since 2019-08-31
 */
@Data
@Builder
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SessionAcquireInfo {

    private String userName;

    private String expireTime;

}
