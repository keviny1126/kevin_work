package com.power.baseproject.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.power.baseproject.R
import kotlinx.coroutines.*
import kotlin.math.cos
import kotlin.math.sin

class DashboardPressureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), CoroutineScope by CoroutineScope(
    Dispatchers.Main
) {
    private val mStartAngle = 135f // 起始角度
    private val mSweepAngle = 270f // 绘制角度
    private val mMin = 0 // 最小值
    private val mMax = 50 // 最大值
    private var mRealMax = 50f
    private var mRealMin = 0f
    private val mSection = 50 // 值域（mMax-mMin）等分份数
    private val mPortion = 5 // 一个mSection等分份数
    private var mScale = 1f
    private var mRealTimeValue = mMin.toFloat() // 实时读数
    private var mCenterX = 0f
    private var mCenterY // 圆心坐标
            = 0f
    private var mPLRadius // 指针长半径
            = 0
    private var mPSRadius // 指针短半径
            = 0
    private var colorDialLower //转盘下游颜色
            = 0
    private var colorDialMiddle //转盘中游颜色
            = 0
    private var colorDialHigh //转盘上游颜色
            = 0
    private var colorScaleColor = 0//刻度颜色
    private var strokeWidthDial //转盘中风宽度
            = 0f
    private var radiusDial //转盘半径
            = 0f
    private var mRealRadius //实际半径
            = 0f
    private var textSizeDial //转盘文字大小
            = 0
    private var fontMetrics //字体度量
            : Paint.FontMetrics? = null
    private var mRect //矩形
            : RectF? = null
    private var mPath: Path? = null
    private lateinit var scaleValueList: ArrayList<String>
    private var arcPaint: Paint? = null//弧的画笔
    private var pointerPaint: Paint? = null//指针

    private var isAnimFinish = true

    init {
        initAttrs(context, attrs)
        initPaint()
        initView()
    }

    private fun initView() {
        mRect = RectF() //矩形 左上右下
        mPath = Path()
        scaleValueList =
            arrayListOf("0", "1.1", "2.2", "3.3", "4.4", "5.5", "6.6", "7.7", "8.8", "9.9", "11.0")
        mPSRadius = dp2px(10)
        mScale = mMax.toFloat() / mSection
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.DashboardPressureView)
        colorDialLower = attributes.getColor(
            R.styleable.DashboardPressureView_color_dial_lower,
            DEFAULT_COLOR_LOWER
        ) //转盘下游颜色
        colorDialMiddle = attributes.getColor(
            R.styleable.DashboardPressureView_color_dial_middle,
            DEFAULT_COLOR_MIDDLE
        ) //转盘中游颜色
        colorDialHigh =
            attributes.getColor(
                R.styleable.DashboardPressureView_color_dial_high,
                DEFAULT_COLOR_HIGH
            ) //转盘上游颜色
        colorScaleColor =
            attributes.getColor(
                R.styleable.DashboardPressureView_color_dial_high,
                DEFAULT_COLOR_SCALE
            ) //刻度颜色
        textSizeDial = attributes.getDimension(
            R.styleable.DashboardPressureView_text_size_dial,
            sp2px(DEFAULT_TEXT_SIZE_DIAL).toFloat()
        ).toInt() //文字大小
        strokeWidthDial = attributes.getDimension(
            R.styleable.DashboardPressureView_stroke_width_dial,
            dp2px(DEFAULT_STROKE_WIDTH).toFloat()
        ) //线条宽度
        radiusDial = attributes.getDimension(
            R.styleable.DashboardPressureView_radius_circle_dial,
            dp2px(DEFAULT_RADIUS_DIAL).toFloat()
        ) //转盘半径周期

        attributes.recycle()
    }

    private fun initPaint() {
        //圆弧画笔
        arcPaint = Paint()
        arcPaint!!.isAntiAlias = true //抗锯齿
        arcPaint!!.style = Paint.Style.STROKE //风格
        arcPaint!!.strokeWidth = strokeWidthDial //转盘中风宽度

        //指针画笔
        pointerPaint = Paint()
        pointerPaint!!.isAntiAlias = true //抗锯齿
        pointerPaint!!.textSize = textSizeDial.toFloat() //文字大小
        pointerPaint!!.textAlign = Paint.Align.CENTER //排成一行 居中
        fontMetrics = pointerPaint!!.fontMetrics //获得字体度量
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec) //获得测量宽的模式
        val widthSize = MeasureSpec.getSize(widthMeasureSpec) //获得测量宽的大小
        val heightMode = MeasureSpec.getMode(heightMeasureSpec) //获得测量高的模式
        val heightSize = MeasureSpec.getSize(heightMeasureSpec) //获得测量高的大小
        var mWidth: Int
        var mHeight: Int
        if (widthMode == MeasureSpec.EXACTLY) { //精确的
            mWidth = widthSize
        } else {
            mWidth = (paddingLeft + radiusDial * 2 + paddingRight).toInt()
            if (widthMode == MeasureSpec.AT_MOST) { //大概
                mWidth = mWidth.coerceAtMost(widthSize)
            }
        }
        if (heightMode == MeasureSpec.EXACTLY) { //精确的
            mHeight = heightSize
        } else {
            mHeight = (paddingTop + radiusDial * 2 + paddingBottom).toInt()
            if (heightMode == MeasureSpec.AT_MOST) { //大概
                mHeight = mHeight.coerceAtMost(heightSize)
            }
        }

        //设置测量的大小
        setMeasuredDimension(mWidth, mHeight)
        radiusDial =
            ((measuredWidth - paddingLeft - paddingRight).coerceAtMost(measuredHeight - paddingTop - paddingBottom) / 2).toFloat()
        mRealRadius = radiusDial - strokeWidthDial / 2 //真实的半径
        mRect?.set(
            (-mRealRadius),
            (-mRealRadius),
            mRealRadius,
            mRealRadius
        )
        mCenterX = 0f
        mCenterY = 0f

        mPLRadius = (mRealRadius - (strokeWidthDial + dp2px(20))).toInt()

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawArc(canvas) //画弧
        drawPointerLine(canvas) //画指针线
        drawPointer(canvas)
    }

    private fun drawArc(canvas: Canvas) {
        canvas.translate((paddingLeft + radiusDial), (paddingTop + radiusDial))
        arcPaint!!.color = colorDialLower //转盘下游颜色
        canvas.drawArc(
            mRect!!, mStartAngle, 81f,
            false, arcPaint!!
        )
        arcPaint!!.color = colorDialMiddle //转盘中游颜色
        canvas.drawArc(
            mRect!!, 216f, 108f,
            false, arcPaint!!
        )
        arcPaint!!.color = colorDialHigh //转盘高游颜色
        canvas.drawArc(
            mRect!!, 324f, 81f,
            false, arcPaint!!
        )
    }

    //画指针线
    private fun drawPointerLine(canvas: Canvas) {
        canvas.save()
        //画布旋转
        canvas.rotate(mStartAngle)
        for (i in mMin..mMax) {     //一共需要绘制51个表针
//            when {
//                i <= 15 -> {
//                    pointerPaint!!.color = colorDialLower
//                }
//                i <= 35 -> {
//                    pointerPaint!!.color = colorDialMiddle
//                }
//                else -> {
//                    pointerPaint!!.color = colorDialHigh
//                }
//            }
            pointerPaint!!.color = colorScaleColor
            if (i % mPortion == 0) {     //长表针
                pointerPaint!!.strokeWidth = 3f
                canvas.drawLine(
                    radiusDial,
                    0f,
                    (radiusDial - strokeWidthDial),
                    0f,
                    pointerPaint!!
                )
                drawPointerText(canvas, i)
            } else {    //短表针
                pointerPaint!!.strokeWidth = 2f
                canvas.drawLine(
                    radiusDial,
                    0f,
                    (radiusDial - strokeWidthDial + dp2px(6)),
                    0f,
                    pointerPaint!!
                )
            }
            canvas.rotate(5.4f)
        }
        canvas.restore() //恢复坐标系为起始中心位置
    }

    private fun drawPointer(canvas: Canvas) {
        /**
         * 画指针
         */
        val θ =
            (mStartAngle + mSweepAngle * (mRealTimeValue - mMin) / (mMax - mMin)) // 指针与水平线夹角
        val d = dp2px(5) // 指针由两个等腰三角形构成，d为共底边长的一半
        mPath!!.reset()
        val p1 = getCoordinatePoint(d, θ - 90)
        mPath!!.moveTo(p1[0], p1[1])
        val p2 = getCoordinatePoint(mPLRadius, θ)
        mPath!!.lineTo(p2[0], p2[1])
        val p3 = getCoordinatePoint(d, θ + 90)
        mPath!!.lineTo(p3[0], p3[1])
        val p4 = getCoordinatePoint(mPSRadius, θ - 180)
        mPath!!.lineTo(p4[0], p4[1])
        mPath!!.close()
        pointerPaint!!.color = Color.parseColor("#333333")
        canvas.drawPath(mPath!!, pointerPaint!!)
    }

    private fun getCoordinatePoint(radius: Int, angle: Float): FloatArray {
        val point = FloatArray(2)
        var arcAngle = Math.toRadians(angle.toDouble()) //将角度转换为弧度
        if (angle < 90) {
            point[0] = (mCenterX + cos(arcAngle) * radius).toFloat()
            point[1] = (mCenterY + sin(arcAngle) * radius).toFloat()
        } else if (angle == 90f) {
            point[0] = mCenterX
            point[1] = mCenterY + radius
        } else if (angle > 90 && angle < 180) {
            arcAngle = Math.PI * (180 - angle) / 180.0
            point[0] = (mCenterX - cos(arcAngle) * radius).toFloat()
            point[1] = (mCenterY + sin(arcAngle) * radius).toFloat()
        } else if (angle == 180f) {
            point[0] = mCenterX - radius
            point[1] = mCenterY
        } else if (angle > 180 && angle < 270) {
            arcAngle = Math.PI * (angle - 180) / 180.0
            point[0] = (mCenterX - cos(arcAngle) * radius).toFloat()
            point[1] = (mCenterY - sin(arcAngle) * radius).toFloat()
        } else if (angle == 270f) {
            point[0] = mCenterX
            point[1] = mCenterY - radius
        } else {
            arcAngle = Math.PI * (360 - angle) / 180.0
            point[0] = (mCenterX + cos(arcAngle) * radius).toFloat()
            point[1] = (mCenterY - sin(arcAngle) * radius).toFloat()
        }
        return point
    }

    //画指针文字
    private fun drawPointerText(canvas: Canvas, i: Int) {
        canvas.save()
        pointerPaint!!.color = Color.parseColor("#333333")
        val currentCenterX =
            (radiusDial - strokeWidthDial - dp2px(6) - pointerPaint!!.measureText(i.toString()) / 2).toInt()
        canvas.translate(currentCenterX.toFloat(), 0f)
        canvas.rotate(360 - mStartAngle - 5.4f * i) //坐标系总旋转角度为360度
        val textBaseLine =
            (0 + (fontMetrics!!.bottom - fontMetrics!!.top) / 2 - fontMetrics!!.bottom).toInt()
        val scaleValue = scaleValueList[i / 5]
        canvas.drawText(scaleValue, 0f, textBaseLine.toFloat(), pointerPaint!!)
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

    fun setMinAndMaxValue(min: Float, max: Float) {
        mRealMax = max
        mRealMin = min
        //"%.2f".format(min)保留两位小数点 会自动执行四舍五入
        val unitScale = (max - min) / mMax
        //LogUtil.i("kevin", "表盘单元刻度大小：$unitScale")
        scaleValueList.clear()
        var tempValue = 0f
        for (i in 0 until 11) {
            when (i) {
                0 -> tempValue = min
                else -> tempValue += unitScale * 5
            }
            scaleValueList.add("%.1f".format(tempValue))
        }

        postInvalidate()
    }

    var realPreValue: Float
        get() = mRealTimeValue
        set(realPreValue) {
            launch {
                val realValue = (mMax * realPreValue) / (mRealMax - mRealMin)
                //LogUtil.i("kevin", "传入值:$value，“转化值：$realValue")
                if (mRealTimeValue == realValue || realValue < mMin || realValue > mMax) {
                    return@launch
                }
                if (!isAnimFinish) {
                    isAnimFinish = true
                    mRealTimeValue = realValue
                    postInvalidate()
                    return@launch
                }
                withContext(Dispatchers.IO) {
                    isAnimFinish = false
                    when {
                        realValue > mRealTimeValue -> {
                            while (mRealTimeValue < realValue && !isAnimFinish) {
                                mRealTimeValue += mScale
                                withContext(Dispatchers.Main) {
                                    postInvalidate()
                                }
                                delay(30)
                            }
                        }
                        realValue < mRealTimeValue -> {
                            while (mRealTimeValue > realValue && !isAnimFinish) {
                                mRealTimeValue -= mScale
                                withContext(Dispatchers.Main) {
                                    postInvalidate()
                                }
                                delay(30)
                            }
                        }
                    }
                }
                isAnimFinish = true
                mRealTimeValue = realValue
                postInvalidate()
            }

        }

    override fun onDetachedFromWindow() {
        (this as CoroutineScope).cancel()
        super.onDetachedFromWindow()
    }

    companion object {
        private val DEFAULT_COLOR_LOWER = Color.parseColor("#DCBE00") //下游颜色
        private val DEFAULT_COLOR_MIDDLE = Color.parseColor("#00B140") //中间颜色
        private val DEFAULT_COLOR_HIGH = Color.parseColor("#D54D00") //高的颜色
        private val DEFAULT_COLOR_SCALE = Color.parseColor("#ffffff") //刻度颜色

        private const val DEFAULT_STROKE_WIDTH = 12 //线的宽度
        private const val DEFAULT_RADIUS_DIAL = 128 //转盘半径
        private const val DEFAULT_TEXT_SIZE_DIAL = 12 //转盘 字体大小
    }
}