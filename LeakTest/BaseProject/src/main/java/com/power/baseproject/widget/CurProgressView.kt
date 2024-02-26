package com.power.baseproject.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.power.baseproject.R
import com.power.baseproject.utils.log.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class CurProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), CoroutineScope by CoroutineScope(
    Dispatchers.Main
) {
    private var mWidth = 0f
    private var mHeight = 0f
    private var startScaleHeight = 0f//y轴刻度总高度
    private var mYMax = 0f//Y轴最大值
    private val mSectionY = 4 //Y轴等分份数
    private var mXMax = 0//X轴最大值
    private var mSectionX = 5 //X轴等分份数
    private var gridColor = 0
    private var textSize //文字大小
            = 0
    private lateinit var gridPaint: Paint
    private lateinit var lineFullPaint: Paint
    private lateinit var lineStrokePaint: Paint
    private lateinit var rectPaint: Paint
    private lateinit var mFullPath: Path
    private lateinit var mStrokePath: Path

    //    private var mBottomRect: Rect? = null
    private var fontMetrics //字体度量
            : Paint.FontMetrics? = null
    private lateinit var mGradientColor: IntArray
    private var linearGradient: LinearGradient? = null
    private var tempDataMapList = mutableMapOf<Float, Float>()//历史坐标点集合
    private lateinit var xTypeList: Array<Int>
    private var unitValueY: Float = 1f
    private var prepareUnitValueX: Float = 1f
    private var inflationUnitValueX: Float = 1f
    private var stabilizationUnitValueX: Float = 1f
    private var detectionUnitValueX: Float = 1f
    private var exhaustTimeUnitValueX: Float = 1f

    init {
        initAttrs(context, attrs)
        initObject()
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CurProgressView)
        mYMax = attributes.getFloat(R.styleable.CurProgressView_y_max_value, DEFAULT_MAX_VALUE_Y)
        mXMax = DEFAULT_MAX_VALUE_X
        val sectionUnit = mXMax / mSectionX
        xTypeList = arrayOf(sectionUnit, sectionUnit, sectionUnit, sectionUnit, sectionUnit)

        gridColor = attributes.getColor(R.styleable.CurProgressView_grid_color, DEFAULT_COLOR_GRID)
        textSize = attributes.getDimension(
            R.styleable.CurProgressView_text_size_y,
            sp2px(DEFAULT_TEXT_SIZE).toFloat()
        ).toInt() //文字大小

        attributes.recycle()
    }

    private fun initObject() {
        gridPaint = Paint()
        gridPaint.isAntiAlias = true //抗锯齿
        gridPaint.style = Paint.Style.FILL //风格
        gridPaint.textSize = textSize.toFloat() //文字大小
        gridPaint.textAlign = Paint.Align.CENTER //排成一行 居中

        rectPaint = Paint()
        rectPaint.isAntiAlias = true //抗锯齿
        rectPaint.style = Paint.Style.FILL //风格

        fontMetrics = gridPaint.fontMetrics //获得字体度量
        lineStrokePaint = Paint()
        lineStrokePaint.isAntiAlias = true //抗锯齿
        lineStrokePaint.style = Paint.Style.STROKE //风格
        lineStrokePaint.color = Color.parseColor("#00C55C")

        lineFullPaint = Paint()
        lineFullPaint.isAntiAlias = true //抗锯齿
        lineFullPaint.style = Paint.Style.FILL //风格
        lineFullPaint.color = Color.parseColor("#00C55C")
        mGradientColor =
            intArrayOf(ContextCompat.getColor(context, R.color.color_00EC6E), Color.WHITE)
        mFullPath = Path()
        mStrokePath = Path()
//        mBottomRect = Rect()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        /** 获取控件的宽高 */
        mWidth = measuredWidth.toFloat()
        mHeight = measuredHeight.toFloat()

        startScaleHeight = mHeight * 0.8f
        if (linearGradient == null) {
            linearGradient = LinearGradient(
                0f,
                0f,
                0f,
                startScaleHeight,
                mGradientColor[0],
                mGradientColor[1],
                Shader.TileMode.CLAMP
            )
        }
        lineFullPaint.shader = linearGradient
        unitValueY = startScaleHeight / mYMax
        initUnitValueX()
    }

    private fun initUnitValueX() {
        val sectionWidth = mWidth / mSectionX
        if (xTypeList.size > 4) {
            prepareUnitValueX = sectionWidth / xTypeList[0]
            inflationUnitValueX = sectionWidth / xTypeList[1]
            stabilizationUnitValueX = sectionWidth / xTypeList[2]
            detectionUnitValueX = sectionWidth / xTypeList[3]
            exhaustTimeUnitValueX = sectionWidth / xTypeList[4]
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawGrid(canvas)//画网格
        drawGraph(canvas)//画曲线图
    }

    private fun drawGrid(canvas: Canvas) {
        for (i in 0 until mSectionY) {
            val startY = startScaleHeight - (startScaleHeight / mSectionY) * i
            gridPaint.color = gridColor
            canvas.drawLine(0f, startY, mWidth, startY, gridPaint)
            val text = ((mYMax.toInt() / mSectionY) * i).toString()
            gridPaint.color = Color.parseColor("#888888")
            canvas.drawText(text, 15f, startY - 6f, gridPaint)
        }

        var startX = 0f
        val sectionWidth = mWidth / mSectionX
        for (j in 0 until mSectionX) {
//            if (xTypeMapList[j] == null) {
//                continue
//            }
//            startX += xTypeMapList[j]!!.times(unitValueX)
            startX += sectionWidth
            gridPaint.color = gridColor
            if (j != mSectionX - 1) {
                canvas.drawLine(startX, 0f, startX, startScaleHeight, gridPaint)
            }
        }
        rectPaint.color = Color.parseColor("#DDDDDD")
        canvas.drawRect(0f, startScaleHeight + 3, mWidth, mHeight, rectPaint)
    }

    private fun drawGraph(canvas: Canvas) {
        if (tempDataMapList.isEmpty()) {
            return
        }
        mFullPath.reset()
        mStrokePath.reset()
        mFullPath.moveTo(0f, startScaleHeight)
        mStrokePath.moveTo(0f, startScaleHeight)
        var curEndX = 0f//最终X坐标
        var curEndY = startScaleHeight//最终Y坐标
        var quadTempX = 0f//曲线中点X坐标
        var quadTempY = 0f//曲线中点Y坐标

        for (key in tempDataMapList.keys) {
            if (tempDataMapList[key] != null) {
                mFullPath.quadTo(
                    curEndX,
                    curEndY,
                    (key + curEndX) / 2,
                    (tempDataMapList[key]!! + curEndY) / 2
                )
                mStrokePath.quadTo(
                    curEndX,
                    curEndY,
                    (key + curEndX) / 2,
                    (tempDataMapList[key]!! + curEndY) / 2
                )
                curEndX = key
                curEndY = tempDataMapList[key]!!

                quadTempX = (key + curEndX) / 2
                quadTempY = (tempDataMapList[key]!! + curEndY) / 2
            }
        }
        mStrokePath.quadTo(quadTempX, quadTempY, curEndX, curEndY)

        mFullPath.quadTo(quadTempX, quadTempY, curEndX, curEndY)
        mFullPath.lineTo(curEndX, startScaleHeight)
        mFullPath.close()

        canvas.drawPath(mFullPath, lineFullPaint)
        canvas.drawPath(mStrokePath, lineStrokePaint)

        rectPaint.color = Color.parseColor("#ffff00")
        canvas.drawRect(0f, startScaleHeight + 3, curEndX, mHeight, rectPaint)
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

    fun setPointData(pointX: Float, pointY: Float) {
        if (pointX > mXMax || pointY > mYMax) {
            return
        }
        val x = getPointX(pointX)
        LogUtil.i("pointx", "时间：${pointX} --- x坐标:$x")
        val y = startScaleHeight - pointY * unitValueY
        tempDataMapList[x] = y
        postInvalidate()
    }

    private fun getPointX(pointX: Float): Float {
        return when {
            pointX <= xTypeList[0] -> {
                pointX * prepareUnitValueX
            }
            pointX <= xTypeList[1] + xTypeList[0] -> {
                xTypeList[0] * prepareUnitValueX + (pointX - xTypeList[0]) * inflationUnitValueX
            }
            pointX <= xTypeList[2] + xTypeList[1] + xTypeList[0] -> {
                xTypeList[0] * prepareUnitValueX + xTypeList[1] * inflationUnitValueX + (pointX - xTypeList[0] - xTypeList[1]) * stabilizationUnitValueX
            }
            pointX <= xTypeList[3] + xTypeList[2] + xTypeList[1] + xTypeList[0] -> {
                xTypeList[0] * prepareUnitValueX + xTypeList[1] * inflationUnitValueX + xTypeList[2] * stabilizationUnitValueX + (pointX - xTypeList[0] - xTypeList[1] - xTypeList[2]) * detectionUnitValueX
            }
            pointX <= xTypeList[4] + xTypeList[3] + xTypeList[2] + xTypeList[1] + xTypeList[0] -> {
                xTypeList[0] * prepareUnitValueX + xTypeList[1] * inflationUnitValueX + xTypeList[2] * stabilizationUnitValueX + xTypeList[3] * detectionUnitValueX + (pointX - xTypeList[0] - xTypeList[1] - xTypeList[2] - xTypeList[3]) * exhaustTimeUnitValueX
            }
            else -> 0f
        }
    }

    fun setAllPointer(list: MutableMap<Float, Float>) {
        tempDataMapList.clear()
        for (key in list.keys) {
            val pointY = list[key] ?: continue
            if (key > mXMax || pointY > mYMax) {
                continue
            }
            val x = getPointX(key)
            val y = startScaleHeight - pointY * unitValueY
            tempDataMapList[x] = y
        }
        postInvalidate()
    }


    fun clearData() {
        tempDataMapList.clear()
        postInvalidate()
    }

    fun setXMaxValue(
        inflationTime: Int,
        stabilizationTime: Int,
        detectionTime: Int,
        exhaustTime: Int
    ) {
        mXMax = 10 + inflationTime + stabilizationTime + detectionTime + exhaustTime

        mSectionX = 5
        xTypeList.clone()
        xTypeList[0] = 10
        xTypeList[1] = inflationTime
        xTypeList[2] = stabilizationTime
        xTypeList[3] = detectionTime
        xTypeList[4] = exhaustTime

        initUnitValueX()
        postInvalidate()
    }

    fun setYMaxValue(value: Float) {
        mYMax = value
        unitValueY = startScaleHeight / mYMax
        postInvalidate()
    }

    companion object {
        const val DEFAULT_GRID_NUMBER = 4
        const val DEFAULT_MAX_VALUE_Y = 36f
        const val DEFAULT_MAX_VALUE_X = 70
        private const val DEFAULT_TEXT_SIZE = 12 //字体大小
        private val DEFAULT_COLOR_GRID = Color.parseColor("#dddddd") //网格颜色
    }
}