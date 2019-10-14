// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.sample;

import io.grpc.internal.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import com.baidu.acg.pie.digitalhuman.client.DhClient;
import com.baidu.acg.pie.digitalhuman.client.DhClientImpl;
import com.baidu.acg.pie.digitalhuman.client.DhConsumer;
import com.baidu.acg.pie.digitalhuman.client.DhStream;
import com.baidu.acg.pie.digitalhuman.client.config.ClientConfig;
import com.baidu.acg.pie.digitalhuman.client.core.SessionMeta;
import com.baidu.acg.pie.digitalhuman.client.exception.DigitalHumanException;
import com.baidu.acg.pie.digitalhuman.client.model.SessionResult;
import com.baidu.acg.pie.digitalhuman.client.model.request.AudioRequest;
import com.baidu.acg.pie.digitalhuman.client.model.request.TextRequest;
import com.baidu.acg.pie.digitalhuman.client.model.response.DhResponse;

/**
 * SimpleSendSample
 *
 * @since 2019-08-31
 */
public class SimpleSendSample {

    public static DhClient createClient() {
        return new DhClientImpl(
                ClientConfig.builder()
                        // user and app credential
                        .appId("dummy")
                        .appKey("dummy")
                        // server for grpc
                        .serverHost("1.2.3.4").serverPort(8090)
                        // server for http request
                        .httpHost("1.2.3.4").httpPort(8080)
                        .build());
    }


    /**
     * 创建客户端，并申请会话连接，后续使用申请得到的会话信息进行grpc请求，发送文本内容，驱动数字人
     * 临时地，你可以查看
     * {@see https://redwinner.github.io/brtc/brtc.html?appid=75c664d50ae5432581fcfe2c9c3011d5&listener_only=1}
     * 来观看rtc房间内容，以查看是否正确的
     */
    public static void simpleAcquireAndSendTextSync() {
        DhClient client = createClient();
        // 申请会话信息，后续使用申请得到的会话信息进行进一步的grpc请求
        SessionResult acquire = client.acquire();
        System.out.println("acquire success" + acquire);
        try {
            for (int i = 1; i <= 10; i++) {
                DhResponse result =
                        client.sendSync(buildTextRequest(i));
                System.out.println(result);
            }

        } finally {
            //对于申请得到的会话，并不会在连接断连时释放，只有在client主动的release，才会将申请得到的会话资源释放。
            System.out.println("release session");
            client.shutdown();
            client.release();
        }

    }


    /**
     * 使用现有的会话认证信息，进行grpc请求，发送对应的文本内容，驱动数字人
     */
    private static void sendTextWithExistedMetaSync() {
        DhClient client = createClient();

        // 使用前面已经申请的会话信息，进行grpc连接请求
        client.register(SessionMeta.builder().sessionId("dummy")
                .sessionToken("dummy").build());
        for (int i = 1; i <= 10; i++) {
            DhResponse result =
                    client.sendSync(buildTextRequest(i));
            System.out.println(result);
        }
        client.shutdown();
        client.release();
    }


    /**
     * 申请会话，创建文本数据流，使用文本数据流异步发送文本请求，请求的返回结果在consumer回调函数中进行处理。
     */
    public static void sendTextByStream() {
        DhClient client = createClient();
        // acquire session credential
        client.acquire();
        try {
            CountDownLatch latch = new CountDownLatch(10);
            DhStream<TextRequest> stream = client.textStream(new DhConsumer() {
                @Override
                public void onResponse(DhResponse response) {
                    System.out.println("rcv response " + response.getSeqNumber() + " msg" + response);
                    latch.countDown();
                }

                @Override
                public void onError(DigitalHumanException t) {
                    System.out.println("err occur ," + t.getErrorMessage());
                }

                @Override
                public void onCompleted() {
                    System.out.println("connection complete by remote");
                }
            });
            // 发送并等待服务端的响应
            for (int i = 1; i <= 10; i++) {
                System.out.println("send msg: " + i);
                stream.send(buildTextRequest(i));
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            client.shutdown();
            client.release();
        }
    }


    public static void sendAudioSync() {
        DhClient client = createClient();
        System.out.println(client.acquire());
        try {
            for (int i = 1; i <= 10; i++) {
                DhResponse result = client.sendSync(buildAudioRequest());
                System.out.println("send  " + i + " and rcv " + result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("release");
            client.release();
        }
    }


    public static void sendAudioByStream() {
        DhClient client = createClient();
        client.acquire();
        try {
            CountDownLatch latch = new CountDownLatch(10);
            DhStream<AudioRequest> stream = client.audioStream(new DhConsumer() {
                @Override
                public void onResponse(DhResponse response) {
                    System.out.println("rcv audio response " + response.getSeqNumber());
                    latch.countDown();
                }

                @Override
                public void onError(DigitalHumanException e) {
                    e.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    System.out.println("connection close by remote ");
                }
            });
            try {
                for (int i = 1; i <= 10; i++) {
                    stream.send(buildAudioRequest());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            client.release();
        }
    }


    public static void sendAudioPCM() {
        DhClient client = createClient();
        SessionResult result = client.acquire();
        System.out.println(result);
        try {
            CountDownLatch latch = new CountDownLatch(10);
            DhStream<AudioRequest> stream = client.audioStream(new DhConsumer() {
                @Override
                public void onResponse(DhResponse response) {
                    System.out.println("rcv audio response " + response.getSeqNumber());
                    latch.countDown();
                }

                @Override
                public void onError(DigitalHumanException e) {
                    e.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    System.out.println("connection close by remote ");
                }
            });
            try {
                for (int i = 1; i <= 10; i++) {
                    stream.send(buildPCMAudioRequest());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            client.release();
        }
    }

    private static AudioRequest buildAudioRequest() throws IOException {
        InputStream audio = SimpleSendSample.class.getClassLoader().getResourceAsStream("test.wav");
        return AudioRequest.builder().audioData(IoUtils.toByteArray(audio)).build();
    }

    private static AudioRequest buildPCMAudioRequest() throws IOException {
        InputStream audio = SimpleSendSample.class.getClassLoader().getResourceAsStream("test.wav");

        byte[] audioBytes = IoUtils.toByteArray(audio);

        byte[] bytesWithoutHeader = new byte[audioBytes.length - 44];


        System.arraycopy(audioBytes, 44, bytesWithoutHeader, 0, audioBytes.length - 44);
        return AudioRequest.builder().audioData(bytesWithoutHeader).build();

    }


    private static TextRequest buildTextRequest(int i) {
        return TextRequest.builder().text("你好,我是第" + i + "条").build();
    }


    public static void main(String[] args) {
//        simpleAcquireAndSendTextSync();
//        sendTextWithExistedMetaSync();
//        sendTextByStream();
//        sendAudioSync();
//        sendAudioByStream();
//
        sendAudioPCM();

    }
}
