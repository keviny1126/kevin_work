package com.power.baseproject.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.power.baseproject.R
import kotlinx.coroutines.*

class ScaleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), CoroutineScope by CoroutineScope(
    Dispatchers.Main
) {
    private var scaleColor = 0//刻度颜色
    private var colorLower //下游颜色
            = 0
    private var colorMiddle //中游颜色
            = 0
    private var colorHigh //上游颜色
            = 0
    private var scaleUnitWidth = 0f//矩形单元宽度
    private var scaleUnitHeight = 0f//矩形单元高度
    private var startRectX = 0//矩形起始X
    private var endRectX = 0//矩形起始Y
    private var startRectY = 0//矩形结束X
    private var endRectY = 0//矩形结束Y
    private var mMin = 0f // 最小值
    private var mMax = 48f // 最大值
    private val mSection = 3 // 值域（mMax-mMin）等分份数
    private val mPortion = 5 // 一个mSection等分份数
    private var textSize //文字大小
            = 0
    private var fontMetrics //字体度量
            : Paint.FontMetrics? = null
    private var unitValue = 10f
    private var rectPaint: Paint? = null//矩形画笔
    private var pointerPaint: Paint? = null//指针
    private var mLowRect: Rect? = null
    private var mMidRect: Rect? = null
    private var mHighRect: Rect? = null
    private var mPath: Path? = null

    private var mRealPreValue = 0f//实际指针值
    private var isAnimFinish = true

    init {
        initAttrs(context, attrs)
        initObjects()
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ScaleView)
        scaleColor = attributes.getColor(R.styleable.ScaleView_scale_color, DEFAULT_COLOR_SCALE)
        colorLower = attributes.getColor(
            R.styleable.ScaleView_low_color,
            DEFAULT_COLOR_LOWER
        ) //转盘下游颜色
        colorMiddle = attributes.getColor(
            R.styleable.ScaleView_mid_color,
            DEFAULT_COLOR_MIDDLE
        ) //转盘中游颜色
        colorHigh =
            attributes.getColor(
                R.styleable.ScaleView_high_color,
                DEFAULT_COLOR_HIGH
            ) //转盘上游颜色
        textSize = attributes.getDimension(
            R.styleable.ScaleView_text_size,
            sp2px(DEFAULT_TEXT_SIZE).toFloat()
        ).toInt() //文字大小
        scaleUnitWidth = attributes.getFloat(R.styleable.ScaleView_scale_width, DEFAULT_SCALE_WIDTH)
        scaleUnitHeight =
            attributes.getFloat(R.styleable.ScaleView_scale_height, DEFAULT_SCALE_HEIGHT)
        attributes.recycle()
    }

    private fun initObjects() {
        rectPaint = Paint()
        rectPaint?.isAntiAlias = true //抗锯齿
        rectPaint?.style = Paint.Style.FILL //风格

        //指针画笔
        pointerPaint = Paint()
        pointerPaint!!.isAntiAlias = true //抗锯齿
        pointerPaint!!.textSize = textSize.toFloat() //文字大小
        pointerPaint!!.textAlign = Paint.Align.CENTER //排成一行 居中
        fontMetrics = pointerPaint!!.fontMetrics //获得字体度量

        mLowRect = Rect()
        mMidRect = Rect()
        mHighRect = Rect()
        mPath = Path()

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
            mWidth = (paddingLeft + scaleUnitWidth * 3 + paddingRight).toInt()
            if (widthMode == MeasureSpec.AT_MOST) { //大概
                mWidth = mWidth.coerceAtMost(widthSize)
            }
        }
        if (heightMode == MeasureSpec.EXACTLY) { //精确的
            mHeight = heightSize
        } else {
            mHeight =
                (paddingTop + scaleUnitHeight * 3 + paddingBottom).toInt()
            if (heightMode == MeasureSpec.AT_MOST) { //大概
                mHeight = mHeight.coerceAtMost(heightSize)
            }
        }

        //设置测量的大小
        setMeasuredDimension(mWidth, mHeight)
        scaleUnitWidth =
            (measuredWidth - paddingLeft - paddingRight).toFloat() / 3
        scaleUnitHeight =
            (measuredHeight - paddingTop - paddingBottom - DEFAULT_PADDING * 2).toFloat() / 3
        startRectX = (scaleUnitWidth + paddingLeft).toInt()
        endRectX = (startRectX + scaleUnitWidth).toInt()
        startRectY = paddingTop + DEFAULT_PADDING * 2 / 3
        endRectY = (scaleUnitHeight * 3 + startRectY).toInt()
        mLowRect?.set(startRectX, (startRectY + scaleUnitHeight * 2).toInt(), endRectX, endRectY)
        mMidRect?.set(
            startRectX,
            (startRectY + scaleUnitHeight).toInt(),
            endRectX,
            (startRectY + scaleUnitHeight * 2).toInt()
        )
        mHighRect?.set(startRectX, startRectY, endRectX, (startRectY + scaleUnitHeight).toInt())
        unitValue = scaleUnitWidth / 2
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawRect(canvas)//画矩形
        drawPointerLine(canvas) //画指针线
        drawPointer(canvas)//画当前指针图
    }

    private fun drawRect(canvas: Canvas) {
        rectPaint?.color = colorLower
        canvas.drawRect(mLowRect!!, rectPaint!!)
        rectPaint?.color = colorMiddle
        canvas.drawRect(mMidRect!!, rectPaint!!)
        rectPaint?.color = colorHigh
        canvas.drawRect(mHighRect!!, rectPaint!!)
    }

    //画指针线
    private fun drawPointerLine(canvas: Canvas) {
        val unitValue: Float = (endRectY - startRectY).toFloat() / (mSection * mPortion)//一个单元刻度值
        for (i in 0..mSection * mPortion) {
            pointerPaint!!.color = scaleColor
            when {
                i % mPortion == 0 -> {//长表针
                    pointerPaint!!.strokeWidth = 3f
                    canvas.drawLine(
                        endRectX.toFloat(), endRectY - unitValue * i,
                        startRectX.toFloat(), endRectY - unitValue * i, pointerPaint!!
                    )

                    drawPointerText(canvas, i, endRectY - unitValue * i)
                }
                else -> {//短表针
                    pointerPaint!!.strokeWidth = 2f
                    canvas.drawLine(
                        endRectX.toFloat(), endRectY - unitValue * i,
                        startRectX + scaleUnitWidth / 2, endRectY - unitValue * i, pointerPaint!!
                    )
                }
            }
        }
    }

    //画指针文字
    private fun drawPointerText(canvas: Canvas, i: Int, y: Float) {
        pointerPaint!!.color = Color.parseColor("#333333")
        val textBaseLine = y + (fontMetrics!!.bottom - fontMetrics!!.top) / 2 - fontMetrics!!.bottom
        val scaleValue = (((mMax - mMin).toInt() / (mSection * mPortion)) * i).toString()
        canvas.drawText(
            scaleValue, (startRectX - 18).toFloat(), textBaseLine, pointerPaint!!
        )
    }

    //画当前指针图
    private fun drawPointer(canvas: Canvas) {
        val curValue = endRectY - ((endRectY - startRectY) * mRealPreValue) / (mMax - mMin)
        //LogUtil.i("kevin", "当前Y坐标点:$curValue")

        mPath?.reset()
        mPath?.moveTo((endRectX + 10).toFloat(), curValue)
        mPath?.rLineTo(unitValue / 3, -unitValue / 2)
        mPath?.rLineTo(0f, unitValue / 4)
        mPath?.rLineTo((unitValue / 3) * 2, 0f)
        mPath?.rLineTo(0f, unitValue / 2)
        mPath?.rLineTo(-(unitValue / 3) * 2, 0f)
        mPath?.rLineTo(0f, unitValue / 4)
        mPath?.close()
        when {
            curValue >= endRectY - scaleUnitHeight -> {
                pointerPaint!!.color = colorLower
            }
            curValue >= startRectY + scaleUnitHeight && curValue < startRectY + scaleUnitHeight * 2 -> {
                pointerPaint!!.color = colorMiddle
            }
            curValue < startRectY + scaleUnitHeight -> {
                pointerPaint!!.color = colorHigh
            }
        }

        canvas.drawPath(mPath!!, pointerPaint!!)
    }

    private fun dp2px(dpVal: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpVal,
            resources.displayMetrics
        )
    }

    private fun sp2px(spVal: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            spVal.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    fun setMinAndMaxValue(min: Float, max: Float) {
        mMin = min
        mMax = max
        postInvalidate()
    }

    var realPreValue: Float
        get() = mRealPreValue
        set(realPreValue) {
            launch {
                if (mRealPreValue == realPreValue || realPreValue < mMin) {
                    return@launch
                }
                if (realPreValue > mMax) {
                    mMax = realPreValue
                }
                if (!isAnimFinish) {
                    isAnimFinish = true
                    mRealPreValue = realPreValue
                    postInvalidate()
                    return@launch
                }
                withContext(Dispatchers.IO) {
                    isAnimFinish = false
                    when {
                        realPreValue > mRealPreValue -> {
                            while (mRealPreValue < realPreValue && !isAnimFinish) {
                                mRealPreValue += 1
                                withContext(Dispatchers.Main) {
                                    postInvalidate()
                                }
                                delay(20)
                            }
                        }
                        realPreValue < mRealPreValue -> {
                            while (mRealPreValue > realPreValue && !isAnimFinish) {
                                mRealPreValue -= 1
                                withContext(Dispatchers.Main) {
                                    postInvalidate()
                                }
                                delay(20)
                            }
                        }
                    }
                }
                isAnimFinish = true
                mRealPreValue = realPreValue
                postInvalidate()
            }

        }

    companion object {
        private val DEFAULT_COLOR_SCALE = Color.WHITE //刻度颜色
        private val DEFAULT_COLOR_LOWER = Color.parseColor("#00B052") //下游颜色
        private val DEFAULT_COLOR_MIDDLE = Color.parseColor("#4576D5") //中间颜色
        private val DEFAULT_COLOR_HIGH = Color.parseColor("#D96F26") //高的颜色
        private const val DEFAULT_SCALE_WIDTH = 16f
        private const val DEFAULT_SCALE_HEIGHT = 50f
        private const val DEFAULT_TEXT_SIZE = 12 //字体大小
        private const val DEFAULT_PADDING = 20
    }
}