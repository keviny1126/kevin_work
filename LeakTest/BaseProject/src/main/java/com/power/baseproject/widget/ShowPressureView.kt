package com.power.baseproject.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import com.power.baseproject.R
import com.power.baseproject.utils.ConstantsUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.sin


class ShowPressureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), CoroutineScope by CoroutineScope(
    Dispatchers.Main
) {
    private var mPadding = 0
    private val mStartAngle = 135f // 起始角度
    private val mSweepAngle = 270f // 绘制角度
    private val mMin = 0 // 最小值
    private val mMax = 28 // 最大值
    private var mRealMax = 14f
    private var mRealMin = 0f
    private val mSection = 28 // 值域（mMax-mMin）等分份数
    private val mPortion = 2 // 一个mSection等分份数
    private var mScale = 1f
    private var mRealPressureValue = mMin.toFloat() // 实时读数
    private var mCenterX = 0f
    private var mCenterY // 圆心坐标
            = 0f
    private var dashboardColor = 0//边框颜色
    private var dashboardInColor = 0//内边框颜色
    private var colorScaleColor = 0//刻度颜色
    private var strokeWidthDial //转盘外框宽度
            = 0f
    private var mInStrokeWidth = 0f //转盘中风宽度

    private var pointerRadius = 0//指针长半径

    private var radiusDial //转盘半径
            = 128f
    private var mRealRadius //实际半径
            = 0f
    private var mInRadius = 0f
    private var textSizeDial //转盘文字大小
            = 0
    private var fontMetrics //字体度量
            : Paint.FontMetrics? = null
    private lateinit var mRect: RectF//外边框矩形
    private lateinit var mInRect: RectF//内边框矩形
    private lateinit var mPath: Path
    private lateinit var scaleValueList: ArrayList<String>
    private lateinit var arcPaint: Paint//弧的画笔
    private lateinit var arcInPaint: Paint//弧的画笔
    private lateinit var pointerPaint: Paint//指针
    private lateinit var textPaint: Paint//刻度文字
    private var inSpace = 4

    private var isAnimFinish = true

    init {
        initAttrs(context, attrs)
        initPaint()
        initView()
    }

    private fun initView() {
        mRect = RectF()
        mInRect = RectF()
        mPath = Path()
        scaleValueList =
            arrayListOf(
                "0",
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
                "10",
                "11",
                "12",
                "13",
                "14"
            )
        mScale = mMax.toFloat() / mSection
        inSpace = dp2px(6)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ShowPressureView)
        dashboardColor = attributes.getColor(
            R.styleable.ShowPressureView_dashboard_color,
            DEFAULT_COLOR_DASHBOARD
        ) //转盘颜色
        dashboardInColor = attributes.getColor(
            R.styleable.ShowPressureView_dashboard_in_color,
            DEFAULT_COLOR_DASHBOARD
        )
        colorScaleColor = Color.parseColor("#ffffff")
        textSizeDial = attributes.getDimension(
            R.styleable.ShowPressureView_text_size_dial,
            sp2px(DEFAULT_TEXT_SIZE_DIAL).toFloat()
        ).toInt() //文字大小
        strokeWidthDial = attributes.getDimension(
            R.styleable.ShowPressureView_stroke_width_dial,
            dp2px(DEFAULT_STROKE_WIDTH).toFloat()
        ) //线条宽度
        mInStrokeWidth = strokeWidthDial * 7 / 9
        pointerRadius = mInStrokeWidth.toInt() * 2

        attributes.recycle()
    }

    fun setDashboardColor(@ColorInt color: Int, @ColorInt colorIn: Int) {
        dashboardColor = color
        dashboardInColor = colorIn
        postInvalidate()
    }

    private fun initPaint() {
        //圆弧画笔
        arcPaint = Paint()
        arcPaint.isAntiAlias = true //抗锯齿
        arcPaint.style = Paint.Style.STROKE //风格
        arcPaint.strokeWidth = strokeWidthDial //转盘中风宽度

        arcInPaint = Paint()
        arcInPaint.isAntiAlias = true //抗锯齿
        arcInPaint.style = Paint.Style.STROKE //风格
        arcInPaint.strokeWidth = 1f //转盘中风宽度
        //指针画笔
        pointerPaint = Paint()
        pointerPaint.isAntiAlias = true //抗锯齿
        pointerPaint.textSize = textSizeDial.toFloat() //文字大小
        pointerPaint.textAlign = Paint.Align.CENTER //排成一行 居中
        //fontMetrics = pointerPaint.fontMetrics //获得字体度量
        //刻度文字
        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.isAntiAlias = true //抗锯齿
        textPaint.isSubpixelText = true
        textPaint.textSize = textSizeDial.toFloat() //文字大小
        textPaint.textAlign = Paint.Align.CENTER //排成一行 居中
        textPaint.color = Color.parseColor("#333333")
        fontMetrics = textPaint.fontMetrics //获得字体度量
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mPadding = paddingLeft.coerceAtLeast(paddingTop).coerceAtLeast(
            paddingRight.coerceAtLeast(
                paddingBottom
            )
        )
        mPadding += pointerRadius
        radiusDial = measuredWidth.toFloat() / 2 - mPadding
        mRealRadius = radiusDial - strokeWidthDial / 2 //真实的半径
        mInRadius = mRealRadius - mRealRadius / 3.5f
        mRect.set(
            (-mRealRadius + mPadding),
            (-mRealRadius + mPadding),
            mRealRadius + mPadding,
            mRealRadius + mPadding
        )
        mInRect.set(
            (-mInRadius + mPadding),
            (-mInRadius + mPadding),
            mInRadius + mPadding,
            mInRadius + mPadding
        )
        mCenterX = mPadding.toFloat()
        mCenterY = mPadding.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        arcPaint.color = dashboardColor
        arcInPaint.color = dashboardInColor

        drawArc(canvas) //画弧
        drawPointerLine(canvas) //画指针线
        drawInPointerLine((canvas)) //画指针线
        drawPointer(canvas)
    }

    private fun drawArc(canvas: Canvas) {
        canvas.translate((paddingLeft + radiusDial), (paddingTop + radiusDial))

        canvas.drawArc(mRect, mStartAngle, mSweepAngle, false, arcPaint)
        canvas.drawArc(mInRect, mStartAngle, mSweepAngle, false, arcInPaint)
    }

    //画外部指针线
    private fun drawPointerLine(canvas: Canvas) {
        canvas.save()
        //画布旋转
        canvas.rotate(mStartAngle, mCenterX, mCenterY)
        for (i in mMin..mMax) {
            pointerPaint.color = colorScaleColor
            if (i % mPortion == 0) {     //长表针
                pointerPaint.strokeWidth = 2f
                canvas.drawLine(
                    radiusDial + mCenterX,
                    mCenterY,
                    (radiusDial + mCenterX - strokeWidthDial),
                    mCenterY,
                    pointerPaint
                )
                drawPointerText(canvas, i)
            } else {    //短表针
                pointerPaint.strokeWidth = 2f
                canvas.drawLine(
                    radiusDial + mCenterX,
                    mCenterY,
                    (radiusDial + mCenterX - strokeWidthDial),
                    mCenterY,
                    pointerPaint
                )
            }
            canvas.rotate(9.64f, mCenterX, mCenterY)
        }
        canvas.restore() //恢复坐标系为起始中心位置
    }

    //画内部指针线
    private fun drawInPointerLine(canvas: Canvas) {
        canvas.save()
        //画布旋转
        canvas.rotate(mStartAngle, mCenterX, mCenterY)
        for (i in mMin..(mMax * 2)) {
            if (i % 2 == 0) {
                //短表针
                canvas.drawLine(
                    mInRadius + mCenterX,
                    mCenterY,
                    mInRadius + mCenterX - mInStrokeWidth / 2,
                    mCenterY,
                    arcInPaint
                )
            } else {
                //长表针
                canvas.drawLine(
                    mInRadius + mCenterX,
                    mCenterY,
                    mInRadius + mCenterX - mInStrokeWidth,
                    mCenterY,
                    arcInPaint
                )
            }
            canvas.rotate(4.82f, mCenterX, mCenterY)
        }
        canvas.restore() //恢复坐标系为起始中心位置
    }

    private fun drawPointer(canvas: Canvas) {
        /**
         * 画指针
         */
        val angle =
            (mStartAngle + mSweepAngle * (mRealPressureValue - mMin) / (mMax - mMin)) // 指针与水平线夹角
        mPath.reset()
        val p1 = getCoordinatePoint(radiusDial.toInt() + 1, angle)
        val p2 = getCoordinatePoint(pointerRadius, angle - 30, p1[0], p1[1])
        val p3 = getCoordinatePoint(pointerRadius, angle + 30, p1[0], p1[1])

        mPath.moveTo(p1[0], p1[1])
        mPath.lineTo(p2[0], p2[1])
        mPath.lineTo(p3[0], p3[1])

        mPath.close()
        pointerPaint.color = dashboardColor
        canvas.drawPath(mPath, pointerPaint)
    }

    private fun getCoordinatePoint(
        radius: Int,
        angle: Float,
        centerX: Float = mCenterX,
        centerY: Float = mCenterY
    ): FloatArray {
        val point = FloatArray(2)
        var arcAngle = Math.toRadians(angle.toDouble()) //将角度转换为弧度
        if (angle < 90) {
            point[0] = (centerX + cos(arcAngle) * radius).toFloat()
            point[1] = (centerY + sin(arcAngle) * radius).toFloat()
        } else if (angle == 90f) {
            point[0] = centerX
            point[1] = centerY + radius
        } else if (angle > 90 && angle < 180) {
            arcAngle = Math.PI * (180 - angle) / 180.0
            point[0] = (centerX - cos(arcAngle) * radius).toFloat()
            point[1] = (centerY + sin(arcAngle) * radius).toFloat()
        } else if (angle == 180f) {
            point[0] = centerX - radius
            point[1] = centerY
        } else if (angle > 180 && angle < 270) {
            arcAngle = Math.PI * (angle - 180) / 180.0
            point[0] = (centerX - cos(arcAngle) * radius).toFloat()
            point[1] = (centerY - sin(arcAngle) * radius).toFloat()
        } else if (angle == 270f) {
            point[0] = centerX
            point[1] = centerY - radius
        } else {
            arcAngle = Math.PI * (360 - angle) / 180.0
            point[0] = (centerX + cos(arcAngle) * radius).toFloat()
            point[1] = (centerY - sin(arcAngle) * radius).toFloat()
        }
        return point
    }

    //画指针文字
    private fun drawPointerText(canvas: Canvas, i: Int) {
        canvas.save()
        textPaint.textSize = textSizeDial.toFloat()
        val scaleValue = scaleValueList[i / 2]
        inSpace = when(scaleValue.replace(".", "").length) {
            in 0..2 -> dp2px(6)
            3 -> dp2px(2)
            4->  {
                textPaint.textSize = textSizeDial.toFloat() - 2.3f
                dp2px(2)
            }
            5->  {
                textPaint.textSize = textSizeDial.toFloat() - 2.7f
                dp2px(2)
            }
            else -> {
                textPaint.textSize = textSizeDial.toFloat() - 3f
                dp2px(2)
            }
        }
        val currentCenterX =
            (radiusDial + mCenterX - strokeWidthDial - inSpace - textPaint.measureText(scaleValue) / 2).toInt()
        canvas.translate(currentCenterX.toFloat(), mCenterY)
        canvas.rotate(360 - mStartAngle - 9.64f * i) //坐标系总旋转角度为360度
        val textBaseLine =
            (0 + (fontMetrics!!.bottom - fontMetrics!!.top) / 2 - fontMetrics!!.bottom).toInt()

        canvas.drawText(scaleValue, 0f, textBaseLine.toFloat(), textPaint)
        canvas.restore()
    }

    private fun dp2px(dpVal: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpVal.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    private fun sp2px(spVal: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            spVal.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    fun setMinAndMaxValue(min: Float, max: Float, unit: String) {
        mRealMax = max
        mRealMin = min
        val allValue = max - min
        val unitScale = allValue / mMax
        scaleValueList.clear()
        val stringBuilder = StringBuilder()
        var tempValue = 0f
        for (i in 0 until MAX_SCALE_VALUES) {
            tempValue = when (i) {
                0 -> min
                MAX_SCALE_VALUES - 1 -> max
                else -> tempValue + unitScale * 2
            }
            if (allValue >= MAX_SCALE_VALUES) {
                scaleValueList.add(tempValue.toInt().toString())
            } else {
                stringBuilder.clear()
                stringBuilder.append(
                    "%.${if (unit == ConstantsUtils.PSI || max < 0.1) 3 else 1}f".format(
                        tempValue
                    )
                )
                scaleValueList.add(stringBuilder.toString())
            }
        }
        postInvalidate()
    }

    var realPreValue: Float
        get() = mRealPressureValue
        set(realPreValue) {
            launch {
                val realValue = (mMax * realPreValue) / (mRealMax - mRealMin)
                //LogUtil.i("kevin", "传入值:$value，“转化值：$realValue")
                if (mRealPressureValue == realValue || realValue < mMin || realValue > mMax) {
                    return@launch
                }
                if (!isAnimFinish) {
                    isAnimFinish = true
                    mRealPressureValue = realValue
                    postInvalidate()
                    return@launch
                }
                withContext(Dispatchers.IO) {
                    isAnimFinish = false
                    when {
                        realValue > mRealPressureValue -> {
                            while (mRealPressureValue + mScale <= realValue && !isAnimFinish) {
                                mRealPressureValue += mScale
                                withContext(Dispatchers.Main) {
                                    postInvalidate()
                                }
                                delay(30)
                            }
                        }

                        realValue < mRealPressureValue -> {
                            while (mRealPressureValue - mScale >= realValue && !isAnimFinish) {
                                mRealPressureValue -= mScale
                                withContext(Dispatchers.Main) {
                                    postInvalidate()
                                }
                                delay(30)
                            }
                        }
                    }
                }
                isAnimFinish = true
                mRealPressureValue = realValue
                postInvalidate()
            }

        }

    override fun onDetachedFromWindow() {
        (this as CoroutineScope).cancel()
        super.onDetachedFromWindow()
    }

    companion object {
        private val DEFAULT_COLOR_DASHBOARD = Color.parseColor("#000000")
        const val MAX_SCALE_VALUES = 15
        private const val DEFAULT_STROKE_WIDTH = 9 //线的宽度
        private const val DEFAULT_TEXT_SIZE_DIAL = 12 //转盘 字体大小
    }
}