// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.core;

import static com.baidu.acg.pie.Model.AudioFragment;
import static com.baidu.acg.pie.Model.BaseRequest;
import static com.baidu.acg.pie.Model.BaseResponse;
import static com.baidu.acg.pie.Model.InitRequest;
import static com.baidu.acg.pie.Model.TextFragment;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.baidu.acg.pie.DHServiceGrpc;
import com.baidu.acg.pie.digitalhuman.client.DhConsumer;
import com.baidu.acg.pie.digitalhuman.client.DhStream;
import com.baidu.acg.pie.digitalhuman.client.config.ClientConfig;
import com.baidu.acg.pie.digitalhuman.client.exception.DigitalHumanException;
import com.baidu.acg.pie.digitalhuman.client.model.request.AudioRequest;
import com.baidu.acg.pie.digitalhuman.client.model.request.TextRequest;
import com.baidu.acg.pie.digitalhuman.client.model.response.DhResponse;

/**
 * Sender
 *
 * @since 2019-08-30
 */
@Slf4j
@RequiredArgsConstructor
public class GrpcSender {

    private final ClientConfig clientConfig;
    private final DHServiceGrpc.DHServiceStub stub;

    private final InFlights inFlights = new InFlights();
    private final GrpcObserverFactory observerFactory = new GrpcObserverFactory();
    private final SyncStreamHolder<AudioFragment> audioStreamHolder = new SyncStreamHolder<>();
    private final SyncStreamHolder<TextFragment> textStreamHolder = new SyncStreamHolder<>();

    private final AtomicLong sequenceId = new AtomicLong(0);

    @Setter
    @Getter
    private SessionMeta sessionMeta;

    public GrpcSender(ClientConfig clientConfig, ManagedChannel channel) {
        this.clientConfig = clientConfig;
        this.stub = DHServiceGrpc.newStub(channel);
    }

    public DhResponse sendAudioSync(AudioRequest request) {
        return getSyncResponse(send(request));
    }


    public DhResponse sendTextSync(TextRequest request) {
        return getSyncResponse(send(request));
    }

    public CompletableFuture<DhResponse> send(AudioRequest request) {

        try {
            AudioFragment audioRequest = prepareGrpcRequest(request);
            return this.send(DHServiceGrpc.DHServiceStub::sendAudio,
                    audioStreamHolder, audioRequest, audioRequest.getBaseRequest().getSequenceNum());
        } catch (Exception e) {
            log.error("", e);
            throw DigitalHumanException.fail("call sync request failed", e);
        }

    }

    public synchronized CompletableFuture<DhResponse> send(TextRequest request) {
        try {
            TextFragment textRequest = prepareGrpcRequest(request);
            return this.send(DHServiceGrpc.DHServiceStub::sendText,
                    textStreamHolder, textRequest, textRequest.getBaseRequest().getSequenceNum());
        } catch (Exception e) {
            log.error("", e);
            throw DigitalHumanException.fail("call sync request failed", e);
        }
    }


    private DhResponse getSyncResponse(Future<DhResponse> future) {
        try {
            return future.get(clientConfig.getRequestTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("get sync request failed", e);
            throw DigitalHumanException.fail("get sync response failed, ", e);
        }
    }


    private <T> CompletableFuture<DhResponse> send(
            BiFunction<DHServiceGrpc.DHServiceStub, StreamObserver<BaseResponse>, StreamObserver<T>> stubSend,
            SyncStreamHolder<T> syncStreamHolder, T request, final long sequenceNumber) {
        CompletableFuture<DhResponse> future = new CompletableFuture<>();
        StreamObserver<T> stream;
        if (clientConfig.getChannelConfig().isReuseStream()) {
            if (syncStreamHolder.isReady(sessionMeta)) {
                stream = syncStreamHolder.getStreamObserver();
            } else {
                SessionMeta sessionMeta = this.sessionMeta;
                stream = createRequestObserver(sessionMeta, stubSend);
                syncStreamHolder.set(sessionMeta, stream);
            }
        } else {
            stream = createRequestObserver(this.sessionMeta, stubSend);
        }
        inFlights.offer(String.valueOf(sequenceNumber), (response, throwable) -> {
            if (response != null) {
                future.complete(response);
                return;
            }
            if (throwable != null) {
                future.completeExceptionally(throwable);
            }
        });
        stream.onNext(request);
        return future;
    }


    private <T> StreamObserver<T> createRequestObserver(
            SessionMeta sessionMeta,
            BiFunction<DHServiceGrpc.DHServiceStub, StreamObserver<BaseResponse>, StreamObserver<T>> stubSend) {

        DHServiceGrpc.DHServiceStub callStub = MetadataUtils.attachHeaders(stub, prepareMetadata(sessionMeta));

        return stubSend.apply(callStub,
                observerFactory.createResponseObserver(new DhConsumer() {
                    @Override
                    public void onResponse(DhResponse response) {
                        inFlights.ack(String.valueOf(response.getSeqNumber()), response);
                    }

                    @Override
                    public void onError(DigitalHumanException t) {
                        inFlights.onError(t);
                    }

                    @Override
                    public void onCompleted() {
                        inFlights.onCompleted();
                    }
                }));
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
        StreamObserver<F> requestObserver = stubSend.apply(callStub, observerFactory.createResponseObserver(consumer));
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



    private Metadata prepareMetadata(SessionMeta sessionMeta) {
        if (sessionMeta == null || !sessionMeta.isReady()) {
            throw DigitalHumanException.fail("session meta not ready");
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

}
