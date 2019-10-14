// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.model.request;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * AudioRequest
 *
 * @since 2019-08-30
 */
@Data
public class AudioRequest extends BaseRequest {

    /**
     * todo audio stream request
     */
    @NonNull
    byte[] audioData;

    @Builder
    public AudioRequest(Long seqNumber, @NonNull byte[] audioData) {
        super(seqNumber);
        this.audioData = audioData;
    }
}
