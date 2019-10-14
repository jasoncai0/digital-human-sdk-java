package com.baidu.acg.pie.digitalhuman.client.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * BaseRequest
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
 * @since 2019-10-08
 */
@Data
@AllArgsConstructor
public class BaseRequest {

    /**
     * 请求编号，请求编号允许为空，当请求编号为空时，客户端会自动填充自增的Id。
     */
    private Long seqNumber;
}
