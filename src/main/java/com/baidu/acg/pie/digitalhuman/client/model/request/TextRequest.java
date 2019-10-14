// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.model.request;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * TextRequest
 *
 * @since 2019-08-30
 */
@Data
public class TextRequest extends BaseRequest {

    @NonNull
    private String text;

    @Builder
    public TextRequest(Long seqNumber, @NonNull String text) {
        super(seqNumber);
        this.text = text;
    }

}
