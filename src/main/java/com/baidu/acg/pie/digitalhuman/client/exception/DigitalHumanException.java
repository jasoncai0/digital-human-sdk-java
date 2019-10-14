// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.exception;

import lombok.Data;

/**
 * DigitalHumanClientException
 *
 * @since 2019-08-30
 */
@Data
public class DigitalHumanException extends RuntimeException {

    private int errorCode;

    private String errorMessage;


    private DigitalHumanException(int code, String message) {
        super(message);
        this.errorCode = code;
        this.errorMessage = message;
    }

    private DigitalHumanException(int code, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = code;
        this.errorMessage = message;
    }


    public static DigitalHumanException fail(String message) {
        return fail(-1, message);
    }

    public static DigitalHumanException fail(int code, String errorMessage) {
        return new DigitalHumanException(code, errorMessage);
    }

    public static DigitalHumanException fail(int code, String message, Throwable throwable) {
        return new DigitalHumanException(code, message, throwable);
    }

    public static DigitalHumanException fail(Throwable t) {
        return new DigitalHumanException(-1, t.getMessage(), t);
    }

    public static DigitalHumanException fail(String message, Throwable t) {
        return new DigitalHumanException(-1, message, t);
    }

}
