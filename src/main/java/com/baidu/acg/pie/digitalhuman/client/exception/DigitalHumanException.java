// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.exception;

import lombok.Data;

/**
 * DigitalHumanClientException
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
 * @since 2019-08-30
 */
@Data
public class DigitalHumanException extends RuntimeException {

    private int error;

    private String errorMessage;


    public DigitalHumanException(int code, String message) {
        super(message);
        this.error = code;
        this.errorMessage = message;
    }

    public static DigitalHumanException fail(int code, String errorMessage) {
        return new DigitalHumanException(code, errorMessage);
    }

    public static DigitalHumanException fail(Throwable t) {
        return new DigitalHumanException(Error.EXECUTION.getErrorCode(), t.getMessage());
    }


}
