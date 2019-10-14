package com.baidu.acg.pie.digitalhuman.client.util;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

/**
 * SignUtil
 *
 * @author Cai Zhensheng(caizhensheng@baidu.com)
 * @since 2019-09-22
 */
public class SignUtil {

    public static String sign(String appKey, String appId, String expireTime) {
        HmacUtils hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, appKey);
        return hmac.hmacHex(appId + expireTime);
    }

}
