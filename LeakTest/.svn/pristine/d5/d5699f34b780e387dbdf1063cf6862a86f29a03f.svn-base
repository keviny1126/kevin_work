package com.power.baseproject.widget

import android.animation.*
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.power.baseproject.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * DashboardView style 2 仿芝麻信用分
 * Created by woxingxiao on 2016-11-19.
 */
class DashboardView2 @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mRadius // 画布边缘半径（去除padding后的半径）
            = 0
    private val mStartAngle = 150 // 起始角度
    private val mSweepAngle = 240 // 绘制角度
    private val mMin = 350 // 最小值
    private val mMax = 950 // 最大值
    private val mSection = 10 // 值域（mMax-mMin）等分份数
    private val mPortion = 3 // 一个mSection等分份数
    private val mHeaderText = "BETA" // 表头
    private var mCreditValue = 650 // 信用分
    private var mSolidCreditValue = mCreditValue // 信用分(设定后不变)
    private var mSparkleWidth // 亮点宽度
            = 0
    private var mProgressWidth // 进度圆弧宽度
            = 0
    private var mLength1 // 刻度顶部相对边缘的长度
            = 0f
    private var mCalibrationWidth // 刻度圆弧宽度
            = 0
    private var mLength2 // 刻度读数顶部相对边缘的长度
            = 0f
    private var mPadding = 0
    private var mCenterX = 0f
    private var mCenterY // 圆心坐标
            = 0f
    private var mPaint: Paint? = null
    private var mRectFProgressArc: RectF? = null
    private var mRectFCalibrationFArc: RectF? = null
    private var mRectFTextArc: RectF? = null
    private var mPath: Path? = null
    private var mRectText: Rect? = null
    private lateinit var mTexts: Array<String>
    private var mBackgroundColor = 0
    private lateinit var mBgColors: IntArray

    /**
     * 由于真实的芝麻信用界面信用值不是线性排布，所以播放动画时若以信用值为参考，则会出现忽慢忽快
     * 的情况（开始以为是卡顿）。因此，先计算出最终到达角度，以扫过的角度为线性参考，动画就流畅了
     */
    private var isAnimFinish = true
    private var mAngleWhenAnim = 0f
    private fun init() {
        mSparkleWidth = dp2px(10)
        mProgressWidth = dp2px(3)
        mCalibrationWidth = dp2px(10)
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPaint!!.strokeCap = Paint.Cap.ROUND
        mRectFProgressArc = RectF()
        mRectFCalibrationFArc = RectF()
        mRectFTextArc = RectF()
        mPath = Path()
        mRectText = Rect()
        mTexts = arrayOf("350", "较差", "550", "中等", "600", "良好", "650", "优秀", "700", "极好", "950")
        mBgColors = intArrayOf(
            ContextCompat.getColor(context, R.color.color_red),
            ContextCompat.getColor(context, R.color.color_orange),
            ContextCompat.getColor(context, R.color.color_yellow),
            ContextCompat.getColor(context, R.color.color_green),
            ContextCompat.getColor(context, R.color.color_blue)
        )
        mBackgroundColor = mBgColors[0]
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mPadding = Math.max(
            Math.max(paddingLeft, paddingTop),
            Math.max(paddingRight, paddingBottom)
        )
        setPadding(mPadding, mPadding, mPadding, mPadding)
        mLength1 = mPadding + mSparkleWidth / 2f + dp2px(8)
        mLength2 = mLength1 + mCalibrationWidth + dp2px(1) + dp2px(5)
        val width = resolveSize(dp2px(220), widthMeasureSpec)
        mRadius = (width - mPadding * 2) / 2
        setMeasuredDimension(width, width - dp2px(30))
        mCenterY = measuredWidth / 2f
        mCenterX = mCenterY
        mRectFProgressArc!![mPadding + mSparkleWidth / 2f, mPadding + mSparkleWidth / 2f, measuredWidth - mPadding - mSparkleWidth / 2f] =
            measuredWidth - mPadding - mSparkleWidth / 2f
        mRectFCalibrationFArc!![mLength1 + mCalibrationWidth / 2f, mLength1 + mCalibrationWidth / 2f, measuredWidth - mLength1 - mCalibrationWidth / 2f] =
            measuredWidth - mLength1 - mCalibrationWidth / 2f
        mPaint!!.textSize = sp2px(10).toFloat()
        mPaint!!.getTextBounds("0", 0, "0".length, mRectText)
        mRectFTextArc!![mLength2 + mRectText!!.height(), mLength2 + mRectText!!.height(), measuredWidth - mLength2 - mRectText!!.height()] =
            measuredWidth - mLength2 - mRectText!!.height()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(mBackgroundColor)
        /**
         * 画进度圆弧背景
         */
        mPaint!!.strokeCap = Paint.Cap.ROUND
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeWidth = mProgressWidth.toFloat()
        mPaint!!.alpha = 80
        canvas.drawArc(
            mRectFProgressArc!!,
            (mStartAngle + 1).toFloat(),
            (mSweepAngle - 2).toFloat(),
            false,
            mPaint!!
        )
        mPaint!!.alpha = 255
        if (isAnimFinish) {
            /**
             * 画进度圆弧(起始到信用值)
             */
            mPaint!!.shader = generateSweepGradient()
            canvas.drawArc(
                mRectFProgressArc!!, (mStartAngle + 1).toFloat(),
                calculateRelativeAngleWithValue(mCreditValue) - 2, false, mPaint!!
            )
            /**
             * 画信用值指示亮点
             */
            val point = getCoordinatePoint(
                mRadius - mSparkleWidth / 2f,
                mStartAngle + calculateRelativeAngleWithValue(mCreditValue)
            )
            mPaint!!.style = Paint.Style.FILL
            mPaint!!.shader = generateRadialGradient(point[0], point[1])
            canvas.drawCircle(point[0], point[1], mSparkleWidth / 2f, mPaint!!)
        } else {
            /**
             * 画进度圆弧(起始到信用值)
             */
            mPaint!!.shader = generateSweepGradient()
            canvas.drawArc(
                mRectFProgressArc!!, (mStartAngle + 1).toFloat(),
                mAngleWhenAnim - mStartAngle - 2, false, mPaint!!
            )
            /**
             * 画信用值指示亮点
             */
            val point = getCoordinatePoint(
                mRadius - mSparkleWidth / 2f,
                mAngleWhenAnim
            )
            mPaint!!.style = Paint.Style.FILL
            mPaint!!.shader = generateRadialGradient(point[0], point[1])
            canvas.drawCircle(point[0], point[1], mSparkleWidth / 2f, mPaint!!)
        }
        /**
         * 画刻度圆弧
         */
        mPaint!!.shader = null
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.color = Color.WHITE
        mPaint!!.alpha = 80
        mPaint!!.strokeCap = Paint.Cap.SQUARE
        mPaint!!.strokeWidth = mCalibrationWidth.toFloat()
        canvas.drawArc(
            mRectFCalibrationFArc!!,
            (mStartAngle + 3).toFloat(),
            (mSweepAngle - 6).toFloat(),
            false,
            mPaint!!
        )
        /**
         * 画长刻度
         * 画好起始角度的一条刻度后通过canvas绕着原点旋转来画剩下的长刻度
         */
        mPaint!!.strokeCap = Paint.Cap.ROUND
        mPaint!!.strokeWidth = dp2px(2).toFloat()
        mPaint!!.alpha = 120
        val x0 = mCenterX
        val y0 = mPadding + mLength1 + dp2px(1)
        val x1 = mCenterX
        val y1 = y0 + mCalibrationWidth
        // 逆时针到开始处
        canvas.save()
        canvas.drawLine(x0, y0, x1, y1, mPaint!!)
        var degree = (mSweepAngle / mSection).toFloat()
        for (i in 0 until mSection / 2) {
            canvas.rotate(-degree, mCenterX, mCenterY)
            canvas.drawLine(x0, y0, x1, y1, mPaint!!)
        }
        canvas.restore()
        // 顺时针到结尾处
        canvas.save()
        for (i in 0 until mSection / 2) {
            canvas.rotate(degree, mCenterX, mCenterY)
            canvas.drawLine(x0, y0, x1, y1, mPaint!!)
        }
        canvas.restore()
        /**
         * 画短刻度
         * 同样采用canvas的旋转原理
         */
        mPaint!!.strokeWidth = dp2px(1).toFloat()
        mPaint!!.alpha = 80
        val x2 = mCenterX
        val y2 = y0 + mCalibrationWidth - dp2px(2)
        // 逆时针到开始处
        canvas.save()
        canvas.drawLine(x0, y0, x2, y2, mPaint!!)
        degree = (mSweepAngle / (mSection * mPortion)).toFloat()
        for (i in 0 until mSection * mPortion / 2) {
            canvas.rotate(-degree, mCenterX, mCenterY)
            canvas.drawLine(x0, y0, x2, y2, mPaint!!)
        }
        canvas.restore()
        // 顺时针到结尾处
        canvas.save()
        for (i in 0 until mSection * mPortion / 2) {
            canvas.rotate(degree, mCenterX, mCenterY)
            canvas.drawLine(x0, y0, x2, y2, mPaint!!)
        }
        canvas.restore()
        /**
         * 画长刻度读数
         * 添加一个圆弧path，文字沿着path绘制
         */
        mPaint!!.textSize = sp2px(10).toFloat()
        mPaint!!.textAlign = Paint.Align.LEFT
        mPaint!!.style = Paint.Style.FILL
        mPaint!!.alpha = 160
        for (i in mTexts.indices) {
            mPaint!!.getTextBounds(mTexts[i], 0, mTexts[i].length, mRectText)
            // 粗略把文字的宽度视为圆心角2*θ对应的弧长，利用弧长公式得到θ，下面用于修正角度
            val θ = (180 * mRectText!!.width() / 2 /
                    (Math.PI * (mRadius - mLength2 - mRectText!!.height()))).toFloat()
            mPath!!.reset()
            mPath!!.addArc(
                mRectFTextArc!!,
                mStartAngle + i * (mSweepAngle / mSection) - θ,  // 正起始角度减去θ使文字居中对准长刻度
                mSweepAngle
                    .toFloat()
            )
            canvas.drawTextOnPath(mTexts[i], mPath!!, 0f, 0f, mPaint!!)
        }
        /**
         * 画实时度数值
         */
        mPaint!!.alpha = 255
        mPaint!!.textSize = sp2px(50).toFloat()
        mPaint!!.textAlign = Paint.Align.CENTER
        val value = mSolidCreditValue.toString()
        canvas.drawText(value, mCenterX, mCenterY + dp2px(30), mPaint!!)
        /**
         * 画表头
         */
        mPaint!!.alpha = 160
        mPaint!!.textSize = sp2px(12).toFloat()
        canvas.drawText(mHeaderText, mCenterX, mCenterY - dp2px(20), mPaint!!)
        /**
         * 画信用描述
         */
        mPaint!!.alpha = 255
        mPaint!!.textSize = sp2px(20).toFloat()
        canvas.drawText(calculateCreditDescription(), mCenterX, mCenterY + dp2px(55), mPaint!!)
        /**
         * 画评估时间
         */
        mPaint!!.alpha = 160
        mPaint!!.textSize = sp2px(10).toFloat()
        canvas.drawText(formatTimeStr, mCenterX, mCenterY + dp2px(70), mPaint!!)
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

    private fun generateSweepGradient(): SweepGradient {
        val sweepGradient = SweepGradient(
            mCenterX,
            mCenterY,
            intArrayOf(Color.argb(0, 255, 255, 255), Color.argb(200, 255, 255, 255)),
            floatArrayOf(0f, calculateRelativeAngleWithValue(mCreditValue) / 360)
        )
        val matrix = Matrix()
        matrix.setRotate((mStartAngle - 1).toFloat(), mCenterX, mCenterY)
        sweepGradient.setLocalMatrix(matrix)
        return sweepGradient
    }

    private fun generateRadialGradient(x: Float, y: Float): RadialGradient {
        return RadialGradient(
            x,
            y,
            mSparkleWidth / 2f,
            intArrayOf(Color.argb(255, 255, 255, 255), Color.argb(80, 255, 255, 255)),
            floatArrayOf(0.4f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    private fun getCoordinatePoint(radius: Float, angle: Float): FloatArray {
        val point = FloatArray(2)
        var arcAngle = Math.toRadians(angle.toDouble()) //将角度转换为弧度
        if (angle < 90) {
            point[0] = (mCenterX + Math.cos(arcAngle) * radius).toFloat()
            point[1] = (mCenterY + Math.sin(arcAngle) * radius).toFloat()
        } else if (angle == 90f) {
            point[0] = mCenterX
            point[1] = mCenterY + radius
        } else if (angle > 90 && angle < 180) {
            arcAngle = Math.PI * (180 - angle) / 180.0
            point[0] = (mCenterX - Math.cos(arcAngle) * radius).toFloat()
            point[1] = (mCenterY + Math.sin(arcAngle) * radius).toFloat()
        } else if (angle == 180f) {
            point[0] = mCenterX - radius
            point[1] = mCenterY
        } else if (angle > 180 && angle < 270) {
            arcAngle = Math.PI * (angle - 180) / 180.0
            point[0] = (mCenterX - Math.cos(arcAngle) * radius).toFloat()
            point[1] = (mCenterY - Math.sin(arcAngle) * radius).toFloat()
        } else if (angle == 270f) {
            point[0] = mCenterX
            point[1] = mCenterY - radius
        } else {
            arcAngle = Math.PI * (360 - angle) / 180.0
            point[0] = (mCenterX + Math.cos(arcAngle) * radius).toFloat()
            point[1] = (mCenterY - Math.sin(arcAngle) * radius).toFloat()
        }
        return point
    }

    /**
     * 相对起始角度计算信用分所对应的角度大小
     */
    private fun calculateRelativeAngleWithValue(value: Int): Float {
        val degreePerSection = 1f * mSweepAngle / mSection
        return if (value > 700) {
            8 * degreePerSection + 2 * degreePerSection / 250 * (value - 700)
        } else if (value > 650) {
            6 * degreePerSection + 2 * degreePerSection / 50 * (value - 650)
        } else if (value > 600) {
            4 * degreePerSection + 2 * degreePerSection / 50 * (value - 600)
        } else if (value > 550) {
            2 * degreePerSection + 2 * degreePerSection / 50 * (value - 550)
        } else {
            2 * degreePerSection / 200 * (value - 350)
        }
    }

    /**
     * 信用分对应信用描述
     */
    private fun calculateCreditDescription(): String {
        if (mSolidCreditValue > 700) {
            return "信用极好"
        } else if (mSolidCreditValue > 650) {
            return "信用优秀"
        } else if (mSolidCreditValue > 600) {
            return "信用良好"
        } else if (mSolidCreditValue > 550) {
            return "信用中等"
        }
        return "信用较差"
    }

    private var mDateFormat: SimpleDateFormat? = null
    private val formatTimeStr: String
        private get() {
            if (mDateFormat == null) {
                mDateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.CHINA)
            }
            return String.format("评估时间:%s", mDateFormat!!.format(Date()))
        }

    /**
     * 设置信用值
     *
     * @param creditValue 信用值
     */
    var creditValue: Int
        get() = mCreditValue
        set(creditValue) {
            if (mSolidCreditValue == creditValue || creditValue < mMin || creditValue > mMax) {
                return
            }
            mSolidCreditValue = creditValue
            mCreditValue = creditValue
            postInvalidate()
        }

    /**
     * 设置信用值并播放动画
     *
     * @param creditValue 信用值
     */
    fun setCreditValueWithAnim(creditValue: Int) {
        if (creditValue < mMin || creditValue > mMax || !isAnimFinish) {
            return
        }
        mSolidCreditValue = creditValue
        val creditValueAnimator = ValueAnimator.ofInt(350, mSolidCreditValue)
        creditValueAnimator.addUpdateListener { animation ->
            mCreditValue = animation.animatedValue as Int
            postInvalidate()
        }

        // 计算最终值对应的角度，以扫过的角度的线性变化来播放动画
        val degree = calculateRelativeAngleWithValue(mSolidCreditValue)
        val degreeValueAnimator = ValueAnimator.ofFloat(mStartAngle.toFloat(), mStartAngle + degree)
        degreeValueAnimator.addUpdateListener { animation ->
            mAngleWhenAnim = animation.animatedValue as Float
        }
        val colorAnimator =
            ObjectAnimator.ofInt(this, "mBackgroundColor", mBgColors[0], mBgColors[0])
        // 实时信用值对应的背景色的变化
        var delay: Long = 1000
        if (mSolidCreditValue > 700) {
            colorAnimator.setIntValues(
                mBgColors[0],
                mBgColors[1],
                mBgColors[2],
                mBgColors[3],
                mBgColors[4]
            )
            delay = 3000
        } else if (mSolidCreditValue > 650) {
            colorAnimator.setIntValues(mBgColors[0], mBgColors[1], mBgColors[2], mBgColors[3])
            delay = 2500
        } else if (mSolidCreditValue > 600) {
            colorAnimator.setIntValues(mBgColors[0], mBgColors[1], mBgColors[2])
            delay = 2000
        } else if (mSolidCreditValue > 550) {
            colorAnimator.setIntValues(mBgColors[0], mBgColors[1])
            delay = 1500
        }
        colorAnimator.setEvaluator(ArgbEvaluator())
        colorAnimator.addUpdateListener { animation ->
            mBackgroundColor = animation.animatedValue as Int
        }
        val animatorSet = AnimatorSet()
        animatorSet
            .setDuration(delay)
            .playTogether(creditValueAnimator, degreeValueAnimator, colorAnimator)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                isAnimFinish = false
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                isAnimFinish = true
            }

            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
                isAnimFinish = true
            }
        })
        animatorSet.start()
    }

    init {
        init()
    }
}