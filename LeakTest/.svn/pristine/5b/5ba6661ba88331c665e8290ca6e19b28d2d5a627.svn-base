package com.power.baseproject.widget

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.power.baseproject.R
import com.power.baseproject.utils.log.LogUtil
import kotlinx.coroutines.*

class ResistanceProgressArcView @JvmOverloads constructor(
    context: Context?, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), CoroutineScope by CoroutineScope(
    Dispatchers.Main
) {
    private val mStartAngle = 225 // 起始角度
    private val mSweepAngle = 90 // 绘制角度
    private var mCenterX = 0f
    private var mCenterY // 圆心坐标
            = 0f
    private var mProgressWidth // 进度圆弧宽度
            = 0
    private var mCalibrationWidth // 刻度圆弧宽度
            = 0
    private var scaleToArcLength // 刻度顶部相对边缘的长度
            = 0
    private val mSection = 180 // 值域（mMax-mMin）等分份数
    private val mPortion = 20 // 一个mSection等分份数
    private var mPaint: Paint? = null
    private var mRectFProgressArc: RectF? = null
    private var mPadding = 0
    private var mBackgroundColor = 0
    private lateinit var mTexts: Array<String>
    private var mRectText: Rect? = null
    private var mRectFTextArc: RectF? = null

    //    private var mRadius // 画布边缘半径（去除padding后的半径）
//            = 0
    private var mPath: Path? = null
    private var isAnimFinish = true
    private var mAngleWhenAnim = 0f
    private var degreePer: Float? = null

    //    private var progressArcBitmap: Bitmap? = null
//    private var scaleArcBitmap:Bitmap? = null
    private var resistanceValue = 0 // 电阻值
    private var resistanceUnit = "K"
    private var tempLastDegree = 225f//上一次最终动画落点角度
//    private var mSolidResistanceValue = resistanceValue // 电阻值(设定后不变)

    init {
        init()
    }

    fun init() {
        mBackgroundColor = ContextCompat.getColor(context, R.color.transparent)
        mProgressWidth = context.resources.getDimension(R.dimen.dp_5).toInt()//dp2px(7)
        mCalibrationWidth = context.resources.getDimension(R.dimen.dp_5).toInt()//dp2px(7)
        scaleToArcLength =
            mProgressWidth + context.resources.getDimension(R.dimen.dp_6).toInt() + dp2px(10)
        mPaint = Paint()
        mPath = Path()
        mPaint!!.isAntiAlias = true
        mRectFProgressArc = RectF()
        mRectText = Rect()
        mTexts = arrayOf("0", "100k", "1M", "10M", "100M", "1G", "10G", "100G", "1T", "∞")

        mRectFTextArc = RectF()
        degreePer = mSweepAngle.toFloat() / mSection
//        progressArcBitmap = ContextCompat.getDrawable(context,R.drawable.common_progress_arc)?.toBitmap()
//        scaleArcBitmap = ContextCompat.getDrawable(context,R.drawable.common_scale_arc)?.toBitmap()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mPadding =
            paddingLeft.coerceAtLeast(paddingTop).coerceAtLeast(
                paddingRight.coerceAtLeast(
                    paddingBottom
                )
            )
        mCenterX = measuredWidth / 2f
        mCenterY = mCenterX + mPadding + dp2px(10).toFloat() / 2

//        val width = resolveSize(dp2px(540), widthMeasureSpec)
//        mRadius = width / 2
        mPaint!!.textSize = sp2px(10).toFloat()
        mPaint!!.getTextBounds("0", 0, "0".length, mRectText)
        mRectFTextArc?.set(
            mRectText!!.height().toFloat(),
            (mPadding + mRectText!!.height()).toFloat(),
            (measuredWidth - mRectText!!.height()).toFloat(),
            (measuredWidth + mPadding - mRectText!!.height()).toFloat()
        )
        mRectFProgressArc?.set(
            (mProgressWidth + mRectText!!.height()).toFloat(),
            (mPadding + mProgressWidth + mRectText!!.height() + dp2px(2)).toFloat(),
            (measuredWidth - mProgressWidth - mRectText!!.height()).toFloat(),
            (measuredWidth + mPadding - mProgressWidth - mRectText!!.height() + dp2px(2)).toFloat()
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(mBackgroundColor)
        /**
         * 画进度圆弧背景
         */
        mPaint!!.strokeCap = Paint.Cap.SQUARE
        mPaint!!.color = ContextCompat.getColor(context, R.color.color_3f4842)
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeWidth = mProgressWidth.toFloat()
        canvas.drawArc(
            mRectFProgressArc!!,
            mStartAngle.toFloat(),
            mSweepAngle.toFloat(),
            false,
            mPaint!!
        )
        /**
         * 画当前进度
         */
        mPaint!!.color = ContextCompat.getColor(context, R.color.color_b5e808)
        if (isAnimFinish) {
            val withValue = calculateRelativeAngleWithValue(resistanceValue, resistanceUnit)
            tempLastDegree = mStartAngle.toFloat() + withValue
            if(withValue == 90f){
                mPaint!!.color = ContextCompat.getColor(context, R.color.color_E87155)
            }
            LogUtil.i("ykw","----withValue----$withValue")
            canvas.drawArc(
                mRectFProgressArc!!, mStartAngle.toFloat(),
                withValue, false, mPaint!!
            )
        } else {
            canvas.drawArc(
                mRectFProgressArc!!, mStartAngle.toFloat(),
                mAngleWhenAnim - mStartAngle, false, mPaint!!
            )
        }
        /**
         * 画长刻度
         * 画好起始角度的一条刻度后通过canvas绕着原点旋转来画剩下的长刻度
         */
        mPaint!!.strokeCap = Paint.Cap.ROUND
        mPaint!!.color = ContextCompat.getColor(context, R.color.color_718279)
        mPaint!!.strokeWidth = dp2px(1).toFloat()
        val x0 = mCenterX
        val y0 = mPadding + scaleToArcLength + dp2px(1)
        val x1 = mCenterX
        val y1 = y0 + mCalibrationWidth

        canvas.save()
        canvas.rotate(-mSweepAngle.toFloat() / 2, mCenterX, mCenterY)
        canvas.drawLine(x0, y0.toFloat(), x1, y1.toFloat(), mPaint!!)
        val degree: Float = mSweepAngle.toFloat() / mSection
        for (i in 0 until mSection) {
            canvas.rotate(degree, mCenterX, mCenterY)
            canvas.drawLine(x0, y0.toFloat(), x1, y1.toFloat(), mPaint!!)
        }
        canvas.restore()
        /**
         * 画短刻度
         * 同样采用canvas的旋转原理
         */
//        mPaint!!.strokeWidth = dp2px(1).toFloat()
//        val x2 = mCenterX
//        val y2 = y0 + mCalibrationWidth - dp2px(2)
//        // 逆时针到开始处
//        canvas.save()
//        canvas.drawLine(x0, y0.toFloat(), x2, y2.toFloat(), mPaint!!)
//        degree = mSweepAngle / (mSection * mPortion).toFloat()
//        for (i in 0 until mSection * mPortion / 2) {
//            canvas.rotate(-degree, mCenterX, mCenterY)
//            canvas.drawLine(x0, y0.toFloat(), x2, y2.toFloat(), mPaint!!)
//        }
//        canvas.restore()
//        // 顺时针到结尾处
//        canvas.save()
//        for (i in 0 until mSection * mPortion / 2) {
//            canvas.rotate(degree, mCenterX, mCenterY)
//            canvas.drawLine(x0, y0.toFloat(), x2, y2.toFloat(), mPaint!!)
//        }
//        canvas.restore()
        /**
         * 画长刻度读数
         * 添加一个圆弧path，文字沿着path绘制
         */
        mPaint!!.textSize = sp2px(16).toFloat()
        mPaint!!.color = ContextCompat.getColor(context, R.color.white)
        mPaint!!.textAlign = Paint.Align.LEFT
        mPaint!!.style = Paint.Style.FILL
        for (i in mTexts.indices) {
            mPaint!!.getTextBounds(mTexts[i], 0, mTexts[i].length, mRectText)
            //θ = （180*弧长）/(Π*r) 粗略把文字的宽度视为圆心角2*θ对应的弧长，利用弧长公式得到θ，下面用于修正角度
            val angle: Float =
                (((180 * mRectText!!.width()
                    .toFloat()) / (Math.PI * (measuredWidth - 2 * mRectText!!.height()) / 2)) / 2).toFloat()  //180 * mRectText!!.width().toFloat() / 2 / (Math.PI * (measuredWidth - mRectText!!.height())).toFloat()
            mPath!!.reset()
            mPath!!.addArc(
                mRectFTextArc!!,
                mStartAngle + i * (mSweepAngle.toFloat() / 9) - angle,  // 正起始角度减去θ使文字居中对准长刻度
                mSweepAngle
                    .toFloat()
            )
            canvas.drawTextOnPath(mTexts[i], mPath!!, 0f, 0f, mPaint!!)
        }
    }

    /**
     * 相对起始角度计算电阻值所对应的角度大小
     */
    private fun calculateRelativeAngleWithValue(value: Int, unit: String): Float {
        if (value == 0) {
            return 0f
        }
        val degreePerSection = mSweepAngle.toFloat() / mSection
        return when (unit) {
            "K" -> {
                when (value) {
                    in 0..100 -> {
                        value.toFloat() / 5 * degreePerSection
                    }
                    in 101..999 -> {
                        20 * degreePerSection + value.toFloat() / 45 * degreePerSection
                    }
                    else -> {
                        40 * degreePerSection
                    }
                }
            }
            "M" -> {
                when (value) {
                    in 1..10 -> {
                        40 * degreePerSection + value.toFloat() * 2 * degreePerSection
                    }
                    in 11..100 -> {
                        60 * degreePerSection + (value * 2).toFloat() / 9 * degreePerSection
                    }
                    in 101..999 -> {
                        80 * degreePerSection + value.toFloat() / 45 * degreePerSection
                    }
                    else -> {
                        100 * degreePerSection
                    }
                }
            }
            "G" -> {
                when (value) {
                    in 1..10 -> {
                        100 * degreePerSection + value.toFloat() * 2 * degreePerSection
                    }
                    in 11..100 -> {
                        120 * degreePerSection + (value * 2).toFloat() / 9 * degreePerSection
                    }
                    in 101..999 -> {
                        140 * degreePerSection + value.toFloat() / 45 * degreePerSection
                    }
                    else -> {
                        160 * degreePerSection
                    }
                }
            }
            "T" -> {
                when (value) {
                    in 1..1000 -> {
                        160 * degreePerSection + value.toFloat() / 50 * degreePerSection
                    }
                    else -> {
                        mSweepAngle.toFloat()
                    }
                }
            }
            else -> {
                mSweepAngle.toFloat()
            }
        }
    }

    /**
     * 设置阻值
     *
     * @param resValue 阻值
     */
    fun setValue(resValue: Int, resUnit: String) {
        isAnimFinish = true
        resistanceUnit = resUnit
        resistanceValue = resValue
        postInvalidate()
    }

    /**
     * 设置阻值并播放动画
     *
     * @param resValue 阻值
     */
    fun setValueWithAnim(resValue: Int, resUnit: String) {
        if (!isAnimFinish) {
            setValue(resValue, resUnit)
            return
        }
        resistanceUnit = resUnit
        resistanceValue = resValue
        // 计算最终值对应的角度，以扫过的角度的线性变化来播放动画
        launch {
            withContext(Dispatchers.IO) {
                val degree = calculateRelativeAngleWithValue(resistanceValue, resUnit)
//                val delayTime = 2000/(degree/degreePer!!)
                isAnimFinish = false
                var tempDegree = tempLastDegree - mStartAngle
                when {
                    tempLastDegree < mStartAngle + degree -> {
                        while (tempDegree <= degree && !isAnimFinish) {
                            tempDegree += degreePer!!
                            mAngleWhenAnim = mStartAngle + tempDegree
                            withContext(Dispatchers.Main) {
                                postInvalidate()
                            }
                            delay(20)
                        }
                    }
                    tempLastDegree > mStartAngle + degree -> {
                        while (tempDegree >= degree && !isAnimFinish) {
                            tempDegree -= degreePer!!
                            mAngleWhenAnim = mStartAngle + tempDegree
                            withContext(Dispatchers.Main) {
                                postInvalidate()
                            }
                            delay(20)
                        }
                    }
                }
            }
            isAnimFinish = true
            postInvalidate()
        }
    }

    private fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    private fun sp2px(sp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, sp.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    override fun onDetachedFromWindow() {
        (this as CoroutineScope).cancel()
        super.onDetachedFromWindow()
    }
}