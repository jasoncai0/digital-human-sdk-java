// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.core;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import lombok.extern.slf4j.Slf4j;

/**
 * ClientInterceptor
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
 * @since 2019-08-30
 */
@Slf4j
public class ClientInterceptor implements io.grpc.ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions, Channel next) {
        log.info("accept");

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            protected ClientCall<ReqT, RespT> delegate() {
                return super.delegate();
            }

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
//                log.info("accept header {}", headers);
                super.start(new SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onHeaders(Metadata headers) {
                        System.out.println("on header " + headers);
                        log.info("on header {}", headers);
                        super.onHeaders(headers);
                    }
                }, headers);
            }
        };
    }
}
