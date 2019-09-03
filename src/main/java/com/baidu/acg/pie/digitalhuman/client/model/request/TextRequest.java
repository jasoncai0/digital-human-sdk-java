// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.model.request;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * TextRequest
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
 * @since 2019-08-30
 */
@Data
@Builder
public class TextRequest {

    /**
     * 请求编号，请求编号允许为空，当请求编号为空时，客户端会自动填充自增的Id。
     */
    private Long seqNumber;

    @NonNull
    private String text;

}
