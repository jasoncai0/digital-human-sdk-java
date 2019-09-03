// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client;

import com.baidu.acg.pie.digitalhuman.client.config.ClientConfig;
import com.baidu.acg.pie.digitalhuman.client.core.GrpcSender;
import com.baidu.acg.pie.digitalhuman.client.core.SessionMeta;
import com.baidu.acg.pie.digitalhuman.client.core.http.SessionHttpClient;
import com.baidu.acg.pie.digitalhuman.client.exception.DigitalHumanException;
import com.baidu.acg.pie.digitalhuman.client.model.SessionResult;
import com.baidu.acg.pie.digitalhuman.client.model.request.AudioRequest;
import com.baidu.acg.pie.digitalhuman.client.model.request.TextRequest;
import com.baidu.acg.pie.digitalhuman.client.model.response.DhResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * DHClient
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
 * @since 2019-08-29
 */
@Slf4j
@RequiredArgsConstructor
public class DhClientImpl implements DhClient {

    private final ClientConfig clientConfig;
    private final GrpcSender sender;
    private final ManagedChannel channel;
    /**
     * lazy init http client
     */
    private volatile SessionHttpClient sessionHttpClient;

    public DhClientImpl(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.channel = ManagedChannelBuilder
                .forAddress(clientConfig.getServerHost(), clientConfig.getServerPort())
                .keepAliveTime(clientConfig.getChannelConfig().getKeepAliveTime(),
                        clientConfig.getChannelConfig().getTimeUnit())
                .keepAliveTimeout(clientConfig.getChannelConfig().getKeepAliveTimeout(),
                        clientConfig.getChannelConfig().getTimeUnit())
                .usePlaintext()
                .build();
        this.sender = new GrpcSender(clientConfig, channel);
    }


    @Override
    public SessionResult acquire() throws DigitalHumanException {
        SessionResult result = getSessionHttpClient().acquire();
        /* 自动注册申请得到的session资源 */
        this.register(SessionMeta.fromSessionResult(result));
        return result;
    }

    @Override
    public SessionResult query(String sessionId) throws DigitalHumanException {
        return getSessionHttpClient().query(sessionId);
    }

    @Override
    public void release(String sessionId) throws DigitalHumanException {
        getSessionHttpClient().close(sessionId);
    }

    @Override
    public void register(SessionMeta sessionMeta) throws DigitalHumanException {
        this.sender.setSessionMeta(sessionMeta);
    }

    @Override
    public DhResponse sendSync(AudioRequest request) throws DigitalHumanException {
        return this.sender.sendAudioSync(request);
    }

    @Override
    public DhResponse sendSync(TextRequest request) throws DigitalHumanException {
        return this.sender.sendTextSync(request);
    }

    @Override
    public DhStream<AudioRequest> audioStream(DhConsumer consumer) throws DigitalHumanException {
        return this.sender.audioStream(consumer);
    }

    @Override
    public DhStream<TextRequest> textStream(DhConsumer consumer) throws DigitalHumanException {
        return this.sender.textStream(consumer);
    }

    @Override
    public void shutdown() throws DigitalHumanException {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("shutdown failed: ", e);
            throw DigitalHumanException.fail(e);
        }
    }


    private SessionHttpClient getSessionHttpClient() {
        if (sessionHttpClient == null) {
            synchronized (this) {
                if (sessionHttpClient == null) {
                    sessionHttpClient = new SessionHttpClient(clientConfig);
                }
            }
        }
        return sessionHttpClient;
    }
}
