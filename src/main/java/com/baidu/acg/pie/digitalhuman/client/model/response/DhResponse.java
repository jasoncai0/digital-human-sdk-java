// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.model.response;

import lombok.Builder;
import lombok.Data;

/**
 * Response
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
 * @since 2019-08-30
 */
@Data
@Builder
public class DhResponse {
    private long seqNumber;
    private int errorCode;
    private String errorMessage;
}
