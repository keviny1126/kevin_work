package com.power.baseproject.widget

import kotlin.jvm.JvmOverloads
import android.content.res.TypedArray
import com.power.baseproject.R
import com.power.baseproject.widget.ClockView2
import android.view.View.MeasureSpec
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.util.TypedValue
import android.view.View

class ClockView2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var colorDialLower //转盘下游颜色
            = 0
    private var colorDialMiddle //转盘中游颜色
            = 0
    private var colorDialHigh //转盘上游颜色
            = 0
    private var textSizeDial //转盘文字大小
            = 0
    private var strokeWidthDial //转盘中风宽度
            = 0
    private var titleDial //转盘标题
            : String? = null
    private var titleDialSize //转盘标题大小
            = 0
    private var titleDialColor //转盘标题颜色
            = 0
    private var valueTextSize //值的大小
            = 0
    private var animPlayTime //动画时间
            = 0
    private var radiusDial //转盘半径
            = 0
    private var mRealRadius //实际半径
            = 0
    private var currentValue //当前值
            = 0f
    private var arcPaint //弧的画笔
            : Paint? = null
    private var mRect //矩形
            : RectF? = null
    private var pointerPaint //指针
            : Paint? = null
    private var fontMetrics //字体度量
            : Paint.FontMetrics? = null
    private var titlePaint //标题画笔
            : Paint? = null
    private var pointerPath //指示器路径
            : Path? = null

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        //获得样式属性
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ClockView)
        colorDialLower = attributes.getColor(
            R.styleable.ClockView_color_dial_lower,
            DEFAULT_COLOR_LOWER
        ) //转盘下游颜色
        colorDialMiddle = attributes.getColor(
            R.styleable.ClockView_color_dial_middle,
            DEFAULT_COLOR_MIDDLE
        ) //转盘中游颜色
        colorDialHigh =
            attributes.getColor(R.styleable.ClockView_color_dial_high, DEFAULT_COLOR_HIGH) //转盘上游颜色
        textSizeDial = attributes.getDimension(
            R.styleable.ClockView_text_size_dial,
            sp2px(DEFAULT_TEXT_SIZE_DIAL).toFloat()
        ).toInt() //文字大小
        strokeWidthDial = attributes.getDimension(
            R.styleable.ClockView_stroke_width_dial,
            dp2px(DEFAULT_STROKE_WIDTH).toFloat()
        ).toInt() //线条宽度
        radiusDial = attributes.getDimension(
            R.styleable.ClockView_radius_circle_dial,
            dp2px(DEFAULT_RADIUS_DIAL).toFloat()
        ).toInt() //转盘半径周期
        titleDial = attributes.getString(R.styleable.ClockView_text_title_dial) //转盘标题
        titleDialSize = attributes.getDimension(
            R.styleable.ClockView_text_title_size,
            dp2px(DEAFAULT_TITLE_SIZE).toFloat()
        ).toInt() //转盘标题大小
        titleDialColor = attributes.getColor(
            R.styleable.ClockView_text_title_color,
            DEAFAULT_COLOR_TITLE
        ) //转盘标题颜色
        valueTextSize = attributes.getDimension(
            R.styleable.ClockView_text_size_value,
            dp2px(DEFAULT_VALUE_SIZE).toFloat()
        ).toInt() //转盘值
        animPlayTime = attributes.getInt(
            R.styleable.ClockView_animator_play_time,
            DEFAULT_ANIM_PLAY_TIME
        ) //动画时间
    }

    private fun initPaint() {
        //圆弧画笔
        arcPaint = Paint()
        arcPaint!!.isAntiAlias = true //抗锯齿
        arcPaint!!.style = Paint.Style.STROKE //风格
        arcPaint!!.strokeWidth = strokeWidthDial.toFloat() //转盘中风宽度

        //指针画笔
        pointerPaint = Paint()
        pointerPaint!!.isAntiAlias = true //抗锯齿
        pointerPaint!!.textSize = textSizeDial.toFloat() //文字大小
        pointerPaint!!.textAlign = Paint.Align.CENTER //排成一行 居中
        fontMetrics = pointerPaint!!.fontMetrics //获得字体度量

        //标题画笔
        titlePaint = Paint()
        titlePaint!!.isAntiAlias = true //抗锯齿
        titlePaint!!.textAlign = Paint.Align.CENTER //排成一行 居中
        titlePaint!!.isFakeBoldText = true //设置黑体

        //指针条
        pointerPath = Path()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec) //获得测量宽的模式
        val widthSize = MeasureSpec.getSize(widthMeasureSpec) //获得测量宽的大小
        val heightMode = MeasureSpec.getMode(heightMeasureSpec) //获得测量高的模式
        val heightSize = MeasureSpec.getSize(heightMeasureSpec) //获得测量高的大小
        var mWidth: Int
        var mHeight: Int
        if (widthMode == MeasureSpec.EXACTLY) { //精确的
            mWidth = widthSize
        } else {
            mWidth = paddingLeft + radiusDial * 2 + paddingRight
            if (widthMode == MeasureSpec.AT_MOST) { //大概
                mWidth = Math.min(mWidth, widthSize)
            }
        }
        if (heightMode == MeasureSpec.EXACTLY) { //精确的
            mHeight = heightSize
        } else {
            mHeight = paddingTop + radiusDial * 2 + paddingBottom
            if (heightMode == MeasureSpec.AT_MOST) { //大概
                mHeight = Math.min(mHeight, heightSize)
            }
        }

        //设置测量的大小
        setMeasuredDimension(mWidth, mHeight)
        radiusDial = Math.min(
            measuredWidth - paddingLeft - paddingRight,
            measuredHeight - paddingTop - paddingBottom
        ) / 2
        mRealRadius = radiusDial - strokeWidthDial / 2 //真实的半径
        mRect = RectF(
            (-mRealRadius).toFloat(),
            (-mRealRadius).toFloat(),
            mRealRadius.toFloat(),
            mRealRadius.toFloat()
        ) //矩形 左上右下
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawArc(canvas) //画弧
        drawPointerLine(canvas) //画指针线
        drawTitleDial(canvas) //画标题
        drawPointer(canvas) //画指针
    }

    //画弧
    private fun drawArc(canvas: Canvas) {
        //画布转换
        canvas.translate((paddingLeft + radiusDial).toFloat(), (paddingTop + radiusDial).toFloat())
        arcPaint!!.color = colorDialLower //转盘下游颜色
        canvas.drawArc(mRect!!, 135f, 54f, false, arcPaint!!)
        arcPaint!!.color = colorDialMiddle //转盘中游颜色
        canvas.drawArc(mRect!!, 189f, 162f, false, arcPaint!!)
        arcPaint!!.color = colorDialHigh //转盘高游颜色
        canvas.drawArc(mRect!!, 351f, 54f, false, arcPaint!!)
    }

    //画指针线
    private fun drawPointerLine(canvas: Canvas) {
        //画布旋转
        canvas.rotate(135f)
        for (i in 0..100) {     //一共需要绘制101个表针
            if (i <= 20) {
                pointerPaint!!.color = colorDialLower
            } else if (i <= 80) {
                pointerPaint!!.color = colorDialMiddle
            } else {
                pointerPaint!!.color = colorDialHigh
            }
            if (i % 10 == 0) {     //长表针
                pointerPaint!!.strokeWidth = 6f
                canvas.drawLine(
                    radiusDial.toFloat(),
                    0f,
                    (radiusDial - strokeWidthDial - dp2px(15)).toFloat(),
                    0f,
                    pointerPaint!!
                )
                drawPointerText(canvas, i)
            } else {    //短表针
                pointerPaint!!.strokeWidth = 3f
                canvas.drawLine(
                    radiusDial.toFloat(),
                    0f,
                    (radiusDial - strokeWidthDial - dp2px(5)).toFloat(),
                    0f,
                    pointerPaint!!
                )
            }
            canvas.rotate(2.7f)
        }
    }

    //画指针文字
    private fun drawPointerText(canvas: Canvas, i: Int) {
        canvas.save()
        val currentCenterX =
            (radiusDial - strokeWidthDial - dp2px(21) - pointerPaint!!.measureText(i.toString()) / 2).toInt()
        canvas.translate(currentCenterX.toFloat(), 0f)
        canvas.rotate(360 - 135 - 2.7f * i) //坐标系总旋转角度为360度
        val textBaseLine =
            (0 + (fontMetrics!!.bottom - fontMetrics!!.top) / 2 - fontMetrics!!.bottom).toInt()
        canvas.drawText(i.toString(), 0f, textBaseLine.toFloat(), pointerPaint!!)
        canvas.restore()
    }

    //画标题的值
    private fun drawTitleDial(canvas: Canvas) {
        titlePaint!!.color = titleDialColor
        titlePaint!!.textSize = titleDialSize.toFloat()
        canvas.rotate(-47.7f) //恢复坐标系为起始中心位置
        canvas.drawText(titleDial!!, 0f, (-radiusDial / 3).toFloat(), titlePaint!!)
        if (currentValue <= 20) {
            titlePaint!!.color = colorDialLower
        } else if (currentValue <= 80) {
            titlePaint!!.color = colorDialMiddle
        } else {
            titlePaint!!.color = colorDialHigh
        }
        titlePaint!!.textSize = valueTextSize.toFloat()
        canvas.drawText("$currentValue%", 0f, (radiusDial * 2 / 3).toFloat(), titlePaint!!)
    }

    //画旋转的指针
    private fun drawPointer(canvas: Canvas) {
        val currentDegree = (currentValue * 2.7 + 135).toInt()
        canvas.rotate(currentDegree.toFloat())
        pointerPath!!.moveTo((radiusDial - strokeWidthDial - dp2px(12)).toFloat(), 0f)
        pointerPath!!.lineTo(0f, -dp2px(5).toFloat())
        pointerPath!!.lineTo(-12f, 0f)
        pointerPath!!.lineTo(0f, dp2px(5).toFloat())
        pointerPath!!.close()
        canvas.drawPath(pointerPath!!, titlePaint!!)
    }

    //设置完成程度
    fun setCompleteDegree(degree: Float) {
        val animator = ValueAnimator.ofFloat(0f, degree)
        animator.addUpdateListener { animation ->
            currentValue = Math.round(animation.animatedValue as Float * 100).toFloat() / 100
            invalidate()
        }
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = animPlayTime.toLong()
        animator.start()
    }

    protected fun dp2px(dpVal: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpVal.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    protected fun sp2px(spVal: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            spVal.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    fun setNum(degree: Float) {
        currentValue = degree
        invalidate()
    }

    fun setNumAnimator(degree: Float) {
        val animator = ValueAnimator.ofFloat(currentValue, degree)
        animator.addUpdateListener { animation ->
            currentValue = Math.round(animation.animatedValue as Float * 100).toFloat() / 100
            invalidate()
        }
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = 1000
        animator.start()
    }

    companion object {
        private val DEFAULT_COLOR_LOWER = Color.parseColor("#1d953f") //下游颜色
        private val DEFAULT_COLOR_MIDDLE = Color.parseColor("#228fbd") //中间颜色
        private const val DEFAULT_COLOR_HIGH = Color.RED //高的颜色
        private const val DEAFAULT_COLOR_TITLE = Color.BLACK //标题颜色
        private const val DEFAULT_TEXT_SIZE_DIAL = 11 //转盘 字体大小
        private const val DEFAULT_STROKE_WIDTH = 8 //线的宽度
        private const val DEFAULT_RADIUS_DIAL = 128 //转盘半径
        private const val DEAFAULT_TITLE_SIZE = 16 //标题大小
        private const val DEFAULT_VALUE_SIZE = 28 //值的大小
        private const val DEFAULT_ANIM_PLAY_TIME = 2000 //动画时间
    }

    init {

        //初始化属性
        initAttrs(context, attrs)
        initPaint()
    }
}