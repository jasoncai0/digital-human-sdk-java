package com.baidu.acg.pie.digitalhuman.client.core;

import io.grpc.stub.StreamObserver;
import lombok.Getter;

/**
 * SyncStreamHolder
 *
 * @since 2019-09-02
 */
public class SyncStreamHolder<T> {


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

}
