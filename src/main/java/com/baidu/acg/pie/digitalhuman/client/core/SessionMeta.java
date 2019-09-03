// Copyright (C) 2019 Baidu Inc. All rights reserved.
package com.baidu.acg.pie.digitalhuman.client.core;

import com.baidu.acg.pie.digitalhuman.client.DhClient;
import com.baidu.acg.pie.digitalhuman.client.model.SessionResult;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * SessionMeta
 * 包含会话的认证信息，如果用户已经拥有了sessionId和sessionToken，且该会话没有关闭释放，那么可以直接进行grpc的连接。
 * 如果用户没有session信息，那么请使用{@link DhClient#acquire()}，进行会话资源的申请。
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
 * @since 2019-08-31
 */
@Data
@Builder
public class SessionMeta {

    private String sessionId;

    private String sessionToken;

    public static SessionMeta fromSessionResult(SessionResult result) {
        return SessionMeta.builder().sessionId(result.getSessionId())
                .sessionToken(result.getSessionToken()).build();
    }

    public boolean isReady() {
        return !StringUtils.isEmpty(sessionId) && !StringUtils.isEmpty(sessionToken);
    }
}
