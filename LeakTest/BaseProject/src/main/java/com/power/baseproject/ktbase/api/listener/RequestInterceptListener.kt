package com.power.baseproject.ktbase.api.listener

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * @Describe 网络拦截回掉
 * @Author Jungle68
 * @Date 2016/12/15
 * @Contact 335891510@qq.com
 */
interface RequestInterceptListener {
    /**
     * 网络请求回掉后调用
     *
     * @param httpResult
     * @param chain
     * @param response
     * @return
     */
    fun onHttpResponse(httpResult: String?, chain: Interceptor.Chain?, response: Response?): Response

    /**
     * 网络请求发出去之前调用
     *
     *
     * @param chain
     * @param request
     * @return
     */
    fun onHttpRequestBefore(chain: Interceptor.Chain?, request: Request?): Request?
}