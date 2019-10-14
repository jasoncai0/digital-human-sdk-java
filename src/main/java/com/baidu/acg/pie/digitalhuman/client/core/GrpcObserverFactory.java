package com.baidu.acg.pie.digitalhuman.client.core;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import com.baidu.acg.pie.Model;
import com.baidu.acg.pie.digitalhuman.client.DhConsumer;
import com.baidu.acg.pie.digitalhuman.client.exception.DigitalHumanException;
import com.baidu.acg.pie.digitalhuman.client.model.response.DhResponse;

/**
 * GrpcResponseObserverFactory
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
 * @since 2019-10-08
 */
@Slf4j
public class GrpcObserverFactory {


    public StreamObserver<Model.BaseResponse> createResponseObserver(DhConsumer dhConsumer) {
        return new StreamObserver<Model.BaseResponse>() {
            @Override
            public void onNext(Model.BaseResponse response) {
                if (response.getErrorCode() != 0) {
                    log.warn("Request accept err {}:{}, seqNumber {}",
                            response.getErrorCode(), response.getErrorMessage(), response.getAck());
                }
                dhConsumer.onResponse(prepareResponse(response));
            }

            @Override
            public void onError(Throwable t) {
                log.error("Encounter error ", t);
                dhConsumer.onError(DigitalHumanException.fail(t));
            }

            @Override
            public void onCompleted() {
                log.warn("On completed");
                dhConsumer.onCompleted();
            }
        };

    }

    private DhResponse prepareResponse(Model.BaseResponse response) {
        return DhResponse.builder().errorCode(response.getErrorCode())
                .errorMessage(response.getErrorMessage())
                .seqNumber(response.getAck()).build();
    }
}
