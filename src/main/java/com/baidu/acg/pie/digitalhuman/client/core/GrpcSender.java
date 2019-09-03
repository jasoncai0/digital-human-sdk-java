// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.core;

import com.baidu.acg.pie.DHServiceGrpc;
import com.baidu.acg.pie.digitalhuman.client.DhConsumer;
import com.baidu.acg.pie.digitalhuman.client.DhStream;
import com.baidu.acg.pie.digitalhuman.client.config.ClientConfig;
import com.baidu.acg.pie.digitalhuman.client.exception.DigitalHumanException;
import com.baidu.acg.pie.digitalhuman.client.exception.Error;
import com.baidu.acg.pie.digitalhuman.client.model.request.AudioRequest;
import com.baidu.acg.pie.digitalhuman.client.model.request.TextRequest;
import com.baidu.acg.pie.digitalhuman.client.model.response.DhResponse;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static com.baidu.acg.pie.Model.AudioFragment;
import static com.baidu.acg.pie.Model.BaseRequest;
import static com.baidu.acg.pie.Model.BaseResponse;
import static com.baidu.acg.pie.Model.InitRequest;
import static com.baidu.acg.pie.Model.TextFragment;

/**
 * Sender
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
 * @since 2019-08-30
 */
@Slf4j
@RequiredArgsConstructor
public class GrpcSender {

    private final ClientConfig clientConfig;
    private final DHServiceGrpc.DHServiceStub stub;

    private final SyncStreamHolder<AudioFragment> audioStreamHolder = new SyncStreamHolder<>();
    private final SyncStreamHolder<TextFragment> textStreamHolder = new SyncStreamHolder<>();

    private final AtomicLong sequenceId = new AtomicLong(0);

    @Setter
    private SessionMeta sessionMeta;

    public GrpcSender(ClientConfig clientConfig, ManagedChannel channel) {
        this.clientConfig = clientConfig;
        this.stub = DHServiceGrpc.newStub(channel);
    }

    public DhResponse sendAudioSync(AudioRequest request) {
        AudioFragment audioRequest = prepareGrpcRequest(request);
        FutureTask<DhResponse> future = this.sendSync(DHServiceGrpc.DHServiceStub::sendAudio,
                audioStreamHolder, audioRequest, audioRequest.getBaseRequest().getSequenceNum());
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("", e);
            throw DigitalHumanException.fail(e);
        }
    }

    public DhResponse sendTextSync(TextRequest request) {
        TextFragment textRequest = prepareGrpcRequest(request);
        FutureTask<DhResponse> futureTask = this.sendSync(DHServiceGrpc.DHServiceStub::sendText,
                textStreamHolder, textRequest, textRequest.getBaseRequest().getSequenceNum());
        try {
            return futureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("", e);
            throw DigitalHumanException.fail(e);
        }
    }

    public DhStream<AudioRequest> audioStream(DhConsumer consumer) {
        return asyncStream(DHServiceGrpc.DHServiceStub::sendAudio, consumer, this::prepareGrpcRequest);
    }

    public DhStream<TextRequest> textStream(DhConsumer consumer) {
        return asyncStream(DHServiceGrpc.DHServiceStub::sendText, consumer, this::prepareGrpcRequest);
    }

    public <T, F> DhStream<T> asyncStream(
            BiFunction<DHServiceGrpc.DHServiceStub, StreamObserver<BaseResponse>, StreamObserver<F>> stubSend,
            DhConsumer consumer, Function<T, F> requestAdapter) {
        DHServiceGrpc.DHServiceStub callStub = MetadataUtils.attachHeaders(stub, prepareMetadata(this.sessionMeta));
        StreamObserver<F> requestObserver = stubSend.apply(callStub, createResponseObserver(consumer));
        return new DhStream<T>() {
            @Override
            public void send(T request) {
                requestObserver.onNext(requestAdapter.apply(request));
            }

            @Override
            public void close() {
                log.debug("Connection closed {} ");
                requestObserver.onCompleted();
            }

            @Override
            public void onError(Throwable t) {
                log.debug("connection on error {}", t);
                requestObserver.onError(t);
            }
        };
    }


    private <T> FutureTask<DhResponse> sendSync(
            BiFunction<DHServiceGrpc.DHServiceStub, StreamObserver<BaseResponse>, StreamObserver<T>> stubSend,
            SyncStreamHolder<T> syncStreamHolder, T request, final long sequenceNumber) {
        ResponseFutureTask<DhResponse> responseFutureTask = new ResponseFutureTask<>();
        StreamObserver<T> stream;
        if (clientConfig.getChannelConfig().isReuseStream()) {
            syncStreamHolder.setFutureTask(sequenceNumber, responseFutureTask);
            if (syncStreamHolder.isReady(sessionMeta)) {
                stream = syncStreamHolder.getStreamObserver();
            } else {
                SessionMeta sessionMeta = this.sessionMeta;
                stream = createSyncRequestObserver(sessionMeta, stubSend,
                        syncStreamHolder::getFutureTask, () -> syncStreamHolder.getRequestIdHolder().getValue());
                syncStreamHolder.set(sessionMeta, stream);
            }
        } else {
            stream = createSyncRequestObserver(this.sessionMeta, stubSend,
                    () -> responseFutureTask, () -> sequenceNumber);
        }
        stream.onNext(request);
        return responseFutureTask;
    }


    private <T> StreamObserver<T> createSyncRequestObserver(
            SessionMeta sessionMeta,
            BiFunction<DHServiceGrpc.DHServiceStub, StreamObserver<BaseResponse>, StreamObserver<T>> stubSend,
            Supplier<ResponseFutureTask<DhResponse>> futureTaskSupplier,
            Supplier<Long> requestIdSupplier) {

        DHServiceGrpc.DHServiceStub callStub = MetadataUtils.attachHeaders(stub, prepareMetadata(sessionMeta));
        return stubSend.apply(callStub, createResponseObserver(new DhConsumer() {
            @Override
            public void onResponse(DhResponse response) {
                if (response.getSeqNumber() != requestIdSupplier.get()) {
                    log.warn("sequence number invalid , received message {} ", response);
                }
                futureTaskSupplier.get().onResponse(response);
            }

            @Override
            public void onError(DigitalHumanException t) {
                log.error("onError ", t);
                futureTaskSupplier.get().onError(t);
            }

            @Override
            public void onCompleted() {
                futureTaskSupplier.get().run();
            }

        }));
    }


    private StreamObserver<BaseResponse> createResponseObserver(DhConsumer dhConsumer) {
        return new StreamObserver<BaseResponse>() {
            @Override
            public void onNext(BaseResponse response) {
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

    private DhResponse prepareResponse(BaseResponse response) {
        return DhResponse.builder().errorCode(response.getErrorCode())
                .errorMessage(response.getErrorMessage())
                .seqNumber(response.getAck()).build();
    }

    private Metadata prepareMetadata(SessionMeta sessionMeta) {
        if (sessionMeta == null || !sessionMeta.isReady()) {
            throw Error.SESSIONMETA_NOT_READY.asException();
        }
        Metadata metadata = new Metadata();
        InitRequest initRequest = InitRequest.newBuilder()
                .setSessionId(sessionMeta.getSessionId())
                .setSessionToken(sessionMeta.getSessionToken())
                .build();
        metadata.put(Metadata.Key.of("init_request", Metadata.ASCII_STRING_MARSHALLER),
                Base64.getEncoder().encodeToString(initRequest.toByteArray()));
        return metadata;
    }

    private AudioFragment prepareGrpcRequest(AudioRequest audioRequest) {
        if (audioRequest.getSeqNumber() == null) {
            audioRequest.setSeqNumber(sequenceId.incrementAndGet());
        }
        return AudioFragment.newBuilder().setAudioData(ByteString.copyFrom(audioRequest.getAudioData()))
                .setBaseRequest(BaseRequest.newBuilder().setSequenceNum(audioRequest.getSeqNumber())
                        .setSendTimestamp(System.currentTimeMillis()).build())
                .build();
    }

    private TextFragment prepareGrpcRequest(TextRequest textRequest) {
        if (textRequest.getSeqNumber() == null) {
            textRequest.setSeqNumber(sequenceId.incrementAndGet());
        }
        return TextFragment.newBuilder().setContent(textRequest.getText())
                .setBaseRequest(BaseRequest.newBuilder().setSequenceNum(textRequest.getSeqNumber())
                        .setSendTimestamp(System.currentTimeMillis()).build())
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Holder<T> {
        private volatile T value;
    }

}
