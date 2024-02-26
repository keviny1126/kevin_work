package com.power.baseproject.widget

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.power.baseproject.R

/**
 * @author LiuChao
 * @describe 提示信息弹框，包括发送失败，发送成功，正在发送
 * @date 2017/2/5
 * @contact email:450127106@qq.com
 */
class LoadingDialog(private val mActivity: Activity?) {
    private val HANDLE_DELAY = 0
    private val DEFAULT_ALPHA = 0.8f
    private var sLoadingDialog: AlertDialog? = null
    private var mAnimationDrawable: AnimationDrawable? = null
    private var layoutView: View? = null
    private var iv_hint_img: ImageView? = null
    private var tv_hint_text: TextView? = null
    private var loading_img: ProgressBar? = null

    /**
     * 显示进行中的状态
     *
     * @param text 进行中的提示消息
     */
    fun showStateIng(text: String) {
        initDialog(R.drawable.frame_loading_center, text, true)
        handleAnimation(true)
    }

    /**
     * 显示进行中的状态
     *
     * @param text     进行中的提示消息
     * @param isCancel 是否可取消对话框
     */
    fun showStateIng(text: String, isCancel: Boolean) {
        initDialog(R.drawable.frame_loading_center, text, isCancel)
        handleAnimation(true)
    }

    /**
     * 进行中的状态变为结束
     */
    fun showStateEnd() {
        handleAnimation(false)
        hideDialog()
    }

    fun onDestroy() {
        if (sLoadingDialog != null) {
            sLoadingDialog!!.dismiss()
        }
    }

    /**
     * 处理动画
     *
     * @param status true 开启动画，false 关闭动画
     */
    private fun handleAnimation(status: Boolean) {
        mAnimationDrawable = iv_hint_img!!.drawable as AnimationDrawable
        requireNotNull(mAnimationDrawable) { "load animation not be null" }
        if (status) {
            if (!mAnimationDrawable!!.isRunning) {
                mAnimationDrawable!!.start()
            }
        } else {
            if (mAnimationDrawable!!.isRunning) {
                mAnimationDrawable!!.stop()
            }
        }
    }

    private fun initDialog(imgRsId: Int, hintContent: String, outsideCancel: Boolean) {
        if (sLoadingDialog == null) {
            layoutView = LayoutInflater.from(mActivity).inflate(R.layout.view_hint_info1, null)
            iv_hint_img = layoutView!!.findViewById<View>(R.id.iv_hint_img) as ImageView
            tv_hint_text = layoutView!!.findViewById<View>(R.id.tv_hint_text) as TextView
            loading_img = layoutView!!.findViewById<View>(R.id.loading_img) as ProgressBar
            //            loading_img.setIndeterminateDrawable(mActivity.getResources().getDrawable(R.drawable.frame_loading_center));
            sLoadingDialog = AlertDialog.Builder(mActivity, R.style.loadingDialogStyle)
                .setCancelable(outsideCancel)
                .create()
            sLoadingDialog?.setCanceledOnTouchOutside(outsideCancel)
            sLoadingDialog?.setOnDismissListener {
                setWindowAlpha(
                    1.0f
                )
            }
        }
        tv_hint_text!!.text = hintContent
        iv_hint_img!!.setImageResource(imgRsId)
        showDialog()
        sLoadingDialog!!.setContentView(layoutView!!) // 必须放在show方法后面
    }

    /**
     * 发送关闭窗口的延迟消息
     */
    private fun sendHideMessage() {
        val message = Message.obtain()
        message.what = HANDLE_DELAY
        mHandler.sendMessageDelayed(message, SUCCESS_ERROR_STATE_TIME.toLong())
    }

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == HANDLE_DELAY && sLoadingDialog != null) {
                hideDialog()
            }
        }
    }

    // Dialog有可能在activity销毁后，调用，这样会发生dialog找不到窗口的错误，所以需要先判断是否有activity
    private fun showDialog() {
        setWindowAlpha(DEFAULT_ALPHA)
        if (mActivity != null && isValidActivity(mActivity)) {
            sLoadingDialog!!.show()
        }
    }

    private fun hideDialog() {
        if (mActivity != null && isValidActivity(mActivity)) {
            sLoadingDialog!!.dismiss()
        }
    }

    /**
     * 判断一个界面是否还存在
     * 使用场景：比如  一个activity被销毁后，它的dialog还要执行某些操作，比如dismiss和show这样是不可以的
     * 因为 dialog是属于activity的
     *
     * @param c
     * @return
     */
    @TargetApi(17)
    private fun isValidActivity(c: Activity?): Boolean {
        if (c == null) {
            return false
        }
        return !(c.isDestroyed || c.isFinishing)
    }

    private fun setWindowAlpha(alpha: Float) {
        val params = mActivity!!.window.attributes
        params.alpha = alpha
        params.verticalMargin = 100f
        mActivity.window.attributes = params
    }

    companion object {
        private const val SUCCESS_ERROR_STATE_TIME = 1500 // 成功或者失败的停留时间
    }
}