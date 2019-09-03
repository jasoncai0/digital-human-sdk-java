// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.core;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import lombok.Data;
import lombok.Getter;

/**
 * ResponseFutureTask
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
 * @since 2019-08-31
 */
public class ResponseFutureTask<T> extends FutureTask<T> {

    @Getter
    ResponseCallable<T> callable;

    public ResponseFutureTask(ResponseCallable<T> callable) {
        super(callable);
        this.callable = callable;
    }

    public ResponseFutureTask() {
        this(new ResponseCallable<>());
    }

    public void onResponse(T response) {
        this.callable.setResponse(response);
        this.run();
    }

    public void onError(Exception e) {
        this.callable.setException(e);
        this.run();
    }

    @Data
    private static class ResponseCallable<T> implements Callable<T> {
        private volatile T response;
        private Exception exception;


        @Override
        public T call() throws Exception {
            if (exception != null) {
                throw exception;
            }
            return response;
        }

    }

}
