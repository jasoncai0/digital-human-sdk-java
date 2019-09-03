// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client;

import com.baidu.acg.pie.digitalhuman.client.exception.DigitalHumanException;
import com.baidu.acg.pie.digitalhuman.client.model.response.DhResponse;

/**
 * DhConsumer
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
 * @since 2019-08-31
 */
public interface DhConsumer {

    /**
     * 用户接收到服务端返回的回调函数 。
     *
     * @param response
     */
    void onResponse(DhResponse response);

    /**
     * 用户处理异常的回调函数
     *
     * @param e
     */
    void onError(DigitalHumanException e);

    /**
     * 用户连接关闭的回调函数。
     */
    void onCompleted();

}
