// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client;

import com.baidu.acg.pie.digitalhuman.client.core.SessionMeta;
import com.baidu.acg.pie.digitalhuman.client.exception.DigitalHumanException;
import com.baidu.acg.pie.digitalhuman.client.model.SessionResult;
import com.baidu.acg.pie.digitalhuman.client.model.request.AudioRequest;
import com.baidu.acg.pie.digitalhuman.client.model.request.TextRequest;
import com.baidu.acg.pie.digitalhuman.client.model.response.DhResponse;

/**
 * DigitalHumanClient
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
 * @since 2019-08-30
 */
public interface DhClient {

    /**
     * 申请会话资源，用于后续的驱动数字人的grpc连接认证和连接BRTC拉取视频流。
     * 使用sdk申请得到会话资源后，会自动调用{@link #register(SessionMeta)}，将申请得到的会话信息进行注册， 用户后续的grpc请求。
     * 当然用户也可以主动调用register
     *
     * @return 会话资源信息，其中sessionId和sessionToken，用于grpc认证；rtcConnection用于连接BRTC认证
     * @throws DigitalHumanException
     */
    SessionResult acquire() throws DigitalHumanException;

    /**
     * 查询申请得到的会话内容
     *
     * @param sessionId 会话ID
     * @return 同 {@link #acquire()}
     * @throws DigitalHumanException
     */
    SessionResult query(String sessionId) throws DigitalHumanException;

    /**
     * 关闭和释放会话资源
     *
     * @param sessionId 会话ID
     * @throws DigitalHumanException
     */
    void release(String sessionId) throws DigitalHumanException;

    /**
     * 注册会话认证信息，用于后续的grpc请求。
     *
     * @param sessionMeta 包含会话的认证信息，如果用户已经拥有了sessionId和sessionToken，且该会话没有关闭释放，那么可以直接进行grpc的连接。
     *                    如果用户没有session信息，那么请使用{@link #acquire()}，进行会话资源的申请
     * @throws DigitalHumanException
     */
    void register(SessionMeta sessionMeta) throws DigitalHumanException;

    /**
     * 发送音频请求，并同步等待响应结果。
     *
     * @param request 音频信息
     * @return 数字人ACK信息
     */
    DhResponse sendSync(AudioRequest request) throws DigitalHumanException;

    /**
     * 发送文本请求，并同步等待响应结果，该请求会等待服务端将请求完成的处理完后才会接收到服务端返回的确认消息。
     *
     * @param request 文本信息
     * @return 数字人ACK消息
     */
    DhResponse sendSync(TextRequest request) throws DigitalHumanException;

    /**
     * 流式发送音频请求，异步发送，不会阻塞线程，通过consumer处理返回结果
     * 注意该流式接口
     *
     * @param consumer 返回结果回调方法，异步处理返回结果
     * @return 请求流，具体使用方式见 {@link DhStream}
     */
    DhStream<AudioRequest> audioStream(DhConsumer consumer) throws DigitalHumanException;

    /**
     * 流式发送文本请求，异步发送，不会阻塞线程，通过consumer回调处理返回
     *
     * @param consumer 回调处理返回结果
     * @return 请求流，具体使用方式见 {@link DhStream}
     */
    DhStream<TextRequest> textStream(DhConsumer consumer) throws DigitalHumanException;

    /**
     * 释放会话资源，并断开Grpc的连接
     *
     * @throws DigitalHumanException
     */
    void shutdown() throws DigitalHumanException;

}
