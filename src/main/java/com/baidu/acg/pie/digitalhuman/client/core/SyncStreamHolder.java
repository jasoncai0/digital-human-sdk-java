package com.baidu.acg.pie.digitalhuman.client.core;

import com.baidu.acg.pie.digitalhuman.client.model.response.DhResponse;
import io.grpc.stub.StreamObserver;
import lombok.Getter;

/**
 * SyncStreamHolder
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
 * @since 2019-09-02
 */
public class SyncStreamHolder<T> {

    private final GrpcSender.Holder<ResponseFutureTask<DhResponse>> futureTaskHolder =
            new GrpcSender.Holder<>();
    @Getter
    private final GrpcSender.Holder<Long> requestIdHolder = new GrpcSender.Holder<>();
    private SessionMeta sessionMeta;
    @Getter
    private StreamObserver<T> streamObserver;

    public void set(SessionMeta sessionMeta, StreamObserver<T> streamObserver) {
        this.sessionMeta = sessionMeta;
        this.streamObserver = streamObserver;
    }


    public boolean isReady(SessionMeta sessionMeta) {
        return isSameSession(sessionMeta) && streamObserver != null;
    }

    private boolean isSameSession(SessionMeta sessionMeta) {
        return this.sessionMeta != null && this.sessionMeta.getSessionId().equals(sessionMeta.getSessionId());
    }

    public void setFutureTask(Long requestId, ResponseFutureTask<DhResponse> replace) {
        this.requestIdHolder.setValue(requestId);
        this.futureTaskHolder.setValue(replace);
    }

    public ResponseFutureTask<DhResponse> getFutureTask() {
        return this.futureTaskHolder.getValue();
    }


}
