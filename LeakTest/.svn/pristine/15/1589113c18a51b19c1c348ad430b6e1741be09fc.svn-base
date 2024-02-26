package com.power.baseproject.utils

import android.os.Handler
import android.os.Message
import java.lang.ref.WeakReference

/**
 * @Description ：Handler弱引用的工具类
 * @Author ：liubo
 * @Date ：2018/5/25
 */
class HandlerUtils private constructor() {
    /**
     * Handler是内部类 内部类存在对外部类的持有
     * 当外部类生命周期结束时如果不静态持有会一直存在,会造成内存泄漏 所以要静态
     */
    class HandlerHolder : Handler {
        private var mListenerWeakReference: WeakReference<OnReceiveMessageListener?>? = null

        constructor() {}

        /**
         * 使用必读：推荐在Activity或者Activity内部持有类中实现该接口，不要使用匿名类，可能会被GC
         *
         * @param listener 收到消息回调接口
         */
        constructor(listener: OnReceiveMessageListener?) {
            mListenerWeakReference = WeakReference(listener)
        }

        override fun handleMessage(msg: Message) {
            if (mListenerWeakReference != null && mListenerWeakReference!!.get() != null) {
                mListenerWeakReference!!.get()!!.handlerMessage(msg)
            }
        }
    }

    /**
     * 收到消息回调接口
     */
    interface OnReceiveMessageListener {
        fun handlerMessage(msg: Message)
    }

    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }
}