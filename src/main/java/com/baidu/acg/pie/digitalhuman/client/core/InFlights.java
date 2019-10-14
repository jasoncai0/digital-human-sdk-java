package com.baidu.acg.pie.digitalhuman.client.core;

import io.grpc.netty.shaded.io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import com.baidu.acg.pie.digitalhuman.client.exception.DigitalHumanException;
import com.baidu.acg.pie.digitalhuman.client.model.response.DhResponse;

/**
 * InFlights
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
 * @since 2019-10-08
 */
@Slf4j
public class InFlights {

    private Map<String, BiConsumer<DhResponse, Throwable>> requests = new ConcurrentHashMap<>();


    private ExecutorService executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() * 2, 0, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            new DefaultThreadFactory("inflight-callback-"),
            new ThreadPoolExecutor.AbortPolicy());


    public void ack(String requestId, DhResponse response) {
        BiConsumer<DhResponse, Throwable> callback = requests.remove(requestId);
        if (callback != null) {
            log.debug("message {} , rcv response ", requestId);
            executor.submit(() -> {
                callback.accept(response, null);
            });
        } else {
            log.warn("rcv message {}, with no request match the response ", requestId);
        }
    }

    public void offer(String requestId, BiConsumer<DhResponse, Throwable> callback) {

        if (requests.containsKey(requestId)) {
            log.warn("the request with the same sequence number already sent");
            throw DigitalHumanException.fail("the request with the same sequence number already sent");
        }
        requests.put(String.valueOf(requestId), callback);
    }

    public void onError(Throwable t) {
        HashMap<String, BiConsumer<DhResponse, Throwable>> failed = new HashMap<>(requests);
        requests.clear();
        log.debug("request {} on failed ", failed.keySet(), t);
        failed.forEach((key, value) -> executor.submit(() -> {
            value.accept(null, t);
        }));
    }

    public void onCompleted() {
        HashMap<String, BiConsumer<DhResponse, Throwable>> failed = new HashMap<>(requests);
        requests.clear();
        log.debug("connection completed ,but request {} still waiting response ", failed.keySet());

        failed.forEach((key, value) -> executor.submit(() -> {
            value.accept(null, DigitalHumanException.fail("connection completed without response received"));
        }));
    }


}
