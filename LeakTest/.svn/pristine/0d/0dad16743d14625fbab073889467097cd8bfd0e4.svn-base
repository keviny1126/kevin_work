package com.power.baseproject.ktbase.api

import com.power.baseproject.ktbase.api.listener.RequestInterceptListener
import com.power.baseproject.utils.ZipHelper
import com.power.baseproject.utils.log.LogUtil
import okhttp3.*
import okio.Buffer
import okio.BufferedSource
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.charset.Charset

/**
 * @Describe
 * @Author Jungle68
 * @Date 2016/12/15
 * @Contact 335891510@qq.com
 */
class RequestIntercept(listener: RequestInterceptListener?) : Interceptor {
    private val mListener: RequestInterceptListener? = listener

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()
        if (mListener != null) //在请求服务器之前可以拿到request,做一些操作比如给request添加header,如果不做操作则返回参数中的request
        {
            request = mListener.onHttpRequestBefore(chain, request)!!
        }
        val requestbuffer = Buffer()
        if (request.body != null) {
            request.body!!.writeTo(requestbuffer)
        } else {
            LogUtil.d(TAG, "request.body() == null")
        }
        //打印url信息
        var logUrl = request.url.toString() + ""
        val method = request.method
        logUrl = URLDecoder.decode(logUrl, "utf-8")
        try {
            chain.connection()?.let {
                LogUtil.d(
                    TAG,
                    "Sending $method Request %s on %n formdata --->  %s%n Connection ---> %s%n Headers ---> %s",
                    logUrl,
                    if (request.body != null) parseParams(request.body, requestbuffer) else "null",
                    it,
                    request.headers
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val t1 = System.nanoTime()
        val originalResponse: Response = chain.proceed(request)
        val t2 = System.nanoTime()
        //打印响应时间
        LogUtil.d(
            TAG,
            "Received response code %d in %.1fms%n%s",
            originalResponse.code,
            (t2 - t1) / 1e6,
            originalResponse.headers
        )

        //读取服务器返回的结果
        val responseBody: ResponseBody? = originalResponse.body
        val source: BufferedSource = responseBody!!.source()
        source.request(Long.MAX_VALUE) // Buffer the entire body.
        val buffer: Buffer = source.buffer()
        //获取content的压缩类型
        val encoding = originalResponse
            .headers["Content-Encoding"]
        val clone = buffer.clone()
        val bodyString: String? = praseBodyString(responseBody, encoding, clone)
        // 打印返回的json结果
        LogUtil.json(TAG, "打印json，請求的URL::: $logUrl")
        LogUtil.json(TAG, "打印json，返回的数据 bodyString == $bodyString")
        if (USE_ERROR_LOG) {
            // 服務器出錯時候打印
            try {
                LogUtil.d(TAG, bodyString+"")
            } catch (ignored: Exception) {
            }
        }
        return mListener?.onHttpResponse(bodyString, chain, originalResponse) ?: originalResponse
    }

    /**
     * 解析返回体数据内容
     *
     * @param responseBody 返回体
     * @param encoding     编码
     * @param clone        数据
     * @return
     */
    fun praseBodyString(responseBody: ResponseBody, encoding: String?, clone: Buffer): String? {
        val bodyString: String? //解析response content
        if (encoding != null && encoding.equals("gzip", ignoreCase = true)) { //content使用gzip压缩
            bodyString = ZipHelper.decompressForGzip(clone.readByteArray()) //解压
        } else if (encoding != null && encoding.equals(
                "zlib",
                ignoreCase = true
            )
        ) { //content使用zlib压缩
            bodyString = ZipHelper.decompressToStringForZlib(clone.readByteArray()) //解压
        } else { //content没有被压缩
            var charset = Charset.forName("UTF-8")
            val contentType = responseBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(charset)
            }
            bodyString = clone.readString(charset!!)
        }
        return bodyString
    }
    companion object {
        private const val TAG = "RequestIntercept"
        private const val USE_ERROR_LOG = false

        @Throws(UnsupportedEncodingException::class)
        fun parseParams(body: RequestBody?, requestbuffer: Buffer): String {
            var canPrint = false
            if (body!!.contentType() != null) {
                val isMultipart: Boolean = body.contentType().toString().contains("multipart")
                val isImage =
                    body.contentType().toString().contains("image/jpeg") || body.contentType()
                        .toString().contains("image/png")
                val isVideo: Boolean = body.contentType().toString().contains("video/mp4")
                canPrint = !isImage && !isMultipart && !isVideo
            }
            if (canPrint && requestbuffer.size < 1000) {
                var data = requestbuffer.readUtf8()
                try {
                    data = data.replace("%(?![0-9a-fA-F]{2})".toRegex(), "%25")
                    data = data.replace("\\+".toRegex(), "%2B")
                    data = URLDecoder.decode(data, "utf-8")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return data
            }
            return "can not print"
        }
    }

}