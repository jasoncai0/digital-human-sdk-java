// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.exception;

import lombok.Getter;

/**
 * ErrorCode
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
 * @since 2019-08-30
 */
public enum Error {

    /**
     * everything is ok
     */
    SUCCESS(0, "ok"),

    /**
     * common client side error
     */
    CLIENT_ERROR(400, "common client error"),

    SESSIONMETA_NOT_READY(410, "session meta not ready"),

    EXECUTION(411, "execution"),


    /**
     * common server side error
     */
    SERVER_ERROR(500, "common server error"),

    HTTP_SERVER_ERROR(501, "http server error"),

    SEQUENCE_NUMBER_INVALID(510, "sequence id invalid"),

    /**
     * unknown error
     */
    UNKOWN(600, "unknown error");

    @Getter
    private int errorCode;

    private String errorMessage;

    Error(int code, String errorMessage) {
        this.errorCode = code;
        this.errorMessage = errorMessage;
    }

    public DigitalHumanException asException() {
        return new DigitalHumanException(errorCode, errorMessage);
    }
}
