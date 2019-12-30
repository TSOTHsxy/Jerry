package com.jerry.agent.response;

import com.jerry.agent.valve.MapperValve;

/**
 * 附加信息抽象接口
 * 为{@link State}枚举提供附加信息的途径
 */
interface Additional {

    /**
     * @return 请求行状态码
     */
    String code();

    /**
     * @return 请求行状态码描述
     */
    String reason();

    /**
     * @return 附加说明
     */
    String explain();

    /**
     * @return 错误处理方式
     * 返回{@code false}表示在当前阀门处理错误
     * 返回{@code true}表示为响应对象设置对应的错误状态后递交给下一个阀门处理
     * 该方法主要服务于{@link MapperValve}
     */
    boolean isThrow();
}
