// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client;

/**
 * Stream
 *
 * @since 2019-08-31
 */
public interface DhStream<T> {

    /**
     * 该异步发送接口非线程安全
     *
     * @param request
     */
    void send(T request);

    void close();

    void onError(Throwable t);

}
