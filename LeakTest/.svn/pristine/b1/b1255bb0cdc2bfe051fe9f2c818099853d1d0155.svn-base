package com.power.baseproject.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.power.baseproject.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class PressureProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), CoroutineScope by CoroutineScope(
    Dispatchers.Main
) {
    private var mWidth = 0f
    private var mHeight = 0f
    private var startScaleY = 0f//y轴起始高度
    private var endScaleY = 0f//y轴结束高度

    private var startScaleX = 0f//x轴起始
    private var endScaleX = 0f//x轴结束

    private var titleHeight = 0f//标题高度
    private var titleBgColor = 0
    private var titleFullColor = 0
    private var lineColor = 0
    private var mYMax = 0f//Y轴最大值
    private var mSectionY = 3 //Y轴等分份数
    private var mXMax = 0//X轴最大值
    private var mSectionX = 5 //X轴等分份数
    private var gridColor = 0
    private var gridDottedColor = 0
    private var textSize //文字大小
            = 0
    private var spaceWidth = 0f
    private var pressureUnit = "kpa"
    private lateinit var gridPaint: Paint
    private lateinit var gridXDottedPaint: Paint
    private lateinit var gridYDottedPaint: Paint
    private lateinit var lineFullPaint: Paint
    private lateinit var lineStrokePaint: Paint
    private lateinit var mTextPaint: TextPaint
    private lateinit var rectPaint: Paint
    private lateinit var mFullPath: Path
    private lateinit var mStrokePath: Path
    private lateinit var gridLinePath: Path

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
    private var sectionWidth = 0f
    private var product = 0//1:evt501

    init {
        initAttrs(context, attrs)
        initObject()
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.PressureProgressView)
        mYMax = DEFAULT_MAX_VALUE_Y
        mXMax = DEFAULT_MAX_VALUE_X
        mSectionY = attributes.getInteger(
            R.styleable.PressureProgressView_section_y,
            3
        )
        titleHeight = attributes.getDimension(
            R.styleable.PressureProgressView_title_height,
            DEFAULT_TITLE_HEIGHT
        )
        titleBgColor = attributes.getColor(
            R.styleable.PressureProgressView_title_color,
            Color.parseColor("#F0F0F0")
        )
        titleFullColor = attributes.getColor(
            R.styleable.PressureProgressView_title_full_color,
            0
        )
        lineColor = attributes.getColor(
            R.styleable.PressureProgressView_line_color,
            Color.parseColor("#00B969")
        )
        val sectionUnit = mXMax / mSectionX
        xTypeList = arrayOf(sectionUnit, sectionUnit, sectionUnit, sectionUnit, sectionUnit)

        gridColor = attributes.getColor(
            R.styleable.PressureProgressView_ppv_grid_color,
            DEFAULT_COLOR_GRID
        )
        gridDottedColor = attributes.getColor(
            R.styleable.PressureProgressView_grid_dotted_color,
            Color.parseColor("#CFEEE8")
        )
        product = attributes.getInteger(
            R.styleable.PressureProgressView_product,
            0
        )

        textSize = attributes.getDimension(
            R.styleable.PressureProgressView_text_size_axis,
            sp2px(DEFAULT_TEXT_SIZE).toFloat()
        ).toInt() //文字大小
        spaceWidth = attributes.getDimension(
            R.styleable.PressureProgressView_space_width,
            40f
        )
        attributes.recycle()
    }

    private fun initObject() {
        gridPaint = Paint()
        gridPaint.isAntiAlias = true //抗锯齿
        gridPaint.style = Paint.Style.FILL //风格
        gridPaint.textSize = textSize.toFloat() //文字大小
        gridPaint.textAlign = Paint.Align.CENTER //排成一行 居中
        gridPaint.strokeWidth = 2f

        gridXDottedPaint = Paint()
        gridXDottedPaint.color = gridDottedColor
        gridXDottedPaint.style = Paint.Style.STROKE
        gridXDottedPaint.strokeWidth = 1f
        gridXDottedPaint.pathEffect = DashPathEffect(floatArrayOf(2f, 2f), 0f)

        gridYDottedPaint = Paint()
        gridYDottedPaint.style = Paint.Style.STROKE
        gridYDottedPaint.strokeWidth = 1f
        gridYDottedPaint.pathEffect = DashPathEffect(floatArrayOf(6f, 6f), 0f)

        rectPaint = Paint()
        rectPaint.isAntiAlias = true //抗锯齿
        rectPaint.style = Paint.Style.FILL //风格

        fontMetrics = gridPaint.fontMetrics //获得字体度量
        lineStrokePaint = Paint()
        lineStrokePaint.isAntiAlias = true //抗锯齿
        lineStrokePaint.style = Paint.Style.STROKE //风格
        lineStrokePaint.color = lineColor
        lineStrokePaint.strokeWidth = 4f

        lineFullPaint = Paint()
        lineFullPaint.isAntiAlias = true //抗锯齿
        lineFullPaint.style = Paint.Style.FILL //风格
        lineFullPaint.color = Color.parseColor("#00B969")

        mTextPaint = TextPaint()
        mTextPaint.flags = Paint.ANTI_ALIAS_FLAG
        mTextPaint.textSize = textSize.toFloat()
        mTextPaint.color = Color.parseColor("#333333")
        mGradientColor =
            intArrayOf(Color.parseColor("#00B969"), Color.WHITE)
        mFullPath = Path()
        mStrokePath = Path()
        gridLinePath = Path()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        /** 获取控件的宽高 */
        mWidth = measuredWidth.toFloat()
        mHeight = measuredHeight.toFloat()

        val paddingHeight = mTextPaint.fontMetrics.bottom * 3 + 12f
        startScaleY = mHeight - paddingHeight
        endScaleY = titleHeight + 1
        unitValueY = (startScaleY - endScaleY) / mYMax

        startScaleX = spaceWidth
        endScaleX = mWidth - spaceWidth
        if (linearGradient == null) {
            linearGradient = LinearGradient(
                0f,
                0f,
                0f,
                startScaleY,
                mGradientColor[0],
                mGradientColor[1],
                Shader.TileMode.CLAMP
            )
        }
        lineFullPaint.shader = linearGradient

        initUnitValueX()
    }

    private fun initUnitValueX() {
        sectionWidth = (endScaleX - startScaleX) / mSectionX
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
        rectPaint.color = titleBgColor
        canvas.drawRect(startScaleX, 0f, endScaleX, titleHeight, rectPaint)
        drawGraph(canvas)//画曲线图
        drawGrid(canvas)//画网格
    }

    private fun drawGrid(canvas: Canvas) {
        val allHeight = startScaleY - endScaleY

        for (i in 0..mSectionY) {
            val startY = startScaleY - (allHeight / mSectionY) * i
            canvas.drawLine(startScaleX, startY, startScaleX + 4f, startY, gridPaint)
            when (i) {
                0 -> {
                    gridPaint.color = gridColor
                    canvas.drawLine(startScaleX, startY, endScaleX, startY, gridPaint)
                }

                else -> {
                    gridLinePath.reset()
                    gridLinePath.moveTo(startScaleX, startY)
                    gridLinePath.lineTo(endScaleX, startY)
                    canvas.drawPath(gridLinePath, gridXDottedPaint)
//                    canvas.drawLine(startScaleX, startY, endScaleX, startY, gridDottedPaint)
                }
            }
            val text = "%.1f".format((mYMax / mSectionY) * i)
            val textWidth = mTextPaint.measureText(text)
            val textHeight = mTextPaint.fontMetrics.bottom
            canvas.drawText(
                text,
                startScaleX - textWidth - 12f,
                startY + textHeight,
                mTextPaint
            )
        }
        val unitWith = mTextPaint.measureText(pressureUnit)
        val unitHeight = mTextPaint.fontMetrics.bottom
        canvas.drawText(
            pressureUnit,
            startScaleX - unitWith - 12f,
            unitHeight * 3 + 8f,
            mTextPaint
        )

        var startX = startScaleX

        for (j in 0..mSectionX) {
            if (product == 1) {
                if (j != 0 && j != mSectionX) {
                    gridYDottedPaint.color = gridDottedColor
                    canvas.drawLine(startX, 0f, startX, startScaleY, gridYDottedPaint)
                }
            } else {
                when (j) {
                    0 -> {
                        gridPaint.color = gridColor
                        canvas.drawLine(startX, 0f, startX, startScaleY, gridPaint)
                    }

                    1 -> {
                        gridYDottedPaint.color = Color.parseColor("#58CFA4")
                        gridLinePath.reset()
                        gridLinePath.moveTo(startX, 0f)
                        gridLinePath.lineTo(startX, startScaleY)
                        canvas.drawPath(gridLinePath, gridYDottedPaint)
//                    canvas.drawLine(startX, 0f, startX, startScaleY, gridDottedPaint)
                    }

                    2 -> {
                        gridYDottedPaint.color = Color.parseColor("#79C745")
                        gridLinePath.reset()
                        gridLinePath.moveTo(startX, 0f)
                        gridLinePath.lineTo(startX, startScaleY)
                        canvas.drawPath(gridLinePath, gridYDottedPaint)
//                    canvas.drawLine(startX, 0f, startX, startScaleY, gridDottedPaint)
                    }

                    3 -> {
                        gridYDottedPaint.color = Color.parseColor("#FF9C9B")
                        gridLinePath.reset()
                        gridLinePath.moveTo(startX, 0f)
                        gridLinePath.lineTo(startX, startScaleY)
                        canvas.drawPath(gridLinePath, gridYDottedPaint)
//                    canvas.drawLine(startX, 0f, startX, startScaleY, gridDottedPaint)
                    }

                    4 -> {
                        gridYDottedPaint.color = Color.parseColor("#FFD040")
                        gridLinePath.reset()
                        gridLinePath.moveTo(startX, 0f)
                        gridLinePath.lineTo(startX, startScaleY)
                        canvas.drawPath(gridLinePath, gridYDottedPaint)
//                    canvas.drawLine(startX, 0f, startX, startScaleY, gridDottedPaint)
                    }
                }
            }
            startX += sectionWidth
        }

    }

    private fun drawGraph(canvas: Canvas) {
        if (tempDataMapList.isEmpty()) {
            return
        }
        mFullPath.reset()
        mStrokePath.reset()
        mFullPath.moveTo(startScaleX, startScaleY)
        mStrokePath.moveTo(startScaleX, startScaleY)
        var curEndX = startScaleX//最终X坐标
        var curEndY = startScaleY - 2f//最终Y坐标
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
        mFullPath.lineTo(curEndX, startScaleY)
        mFullPath.close()

        if (product != 1) {
            canvas.drawPath(mFullPath, lineFullPaint)
        }
        canvas.drawPath(mStrokePath, lineStrokePaint)

        rectPaint.color = if (titleFullColor != 0) titleFullColor else Color.parseColor("#C4DCF4")
        canvas.drawRect(
            startScaleX,
            0f,
            if (curEndX < startScaleX + sectionWidth) curEndX else startScaleX + sectionWidth,
            titleHeight,
            rectPaint
        )

        if (curEndX >= startScaleX + sectionWidth) {
            rectPaint.color =
                if (titleFullColor != 0) titleFullColor else Color.parseColor("#AFE9D4")
            canvas.drawRect(
                startScaleX + sectionWidth,
                0f,
                if (curEndX < startScaleX + sectionWidth * 2) curEndX else startScaleX + sectionWidth * 2,
                titleHeight,
                rectPaint
            )
            if (curEndX >= startScaleX + sectionWidth * 2) {
                rectPaint.color =
                    if (titleFullColor != 0) titleFullColor else Color.parseColor("#D7F4C4")
                canvas.drawRect(
                    startScaleX + sectionWidth * 2,
                    0f,
                    if (curEndX < startScaleX + sectionWidth * 3) curEndX else startScaleX + sectionWidth * 3,
                    titleHeight,
                    rectPaint
                )
                if (curEndX >= startScaleX + sectionWidth * 3) {
                    rectPaint.color =
                        if (titleFullColor != 0) titleFullColor else Color.parseColor("#FFE3E3")
                    canvas.drawRect(
                        startScaleX + sectionWidth * 3,
                        0f,
                        if (curEndX < startScaleX + sectionWidth * 4) curEndX else startScaleX + sectionWidth * 4,
                        titleHeight,
                        rectPaint
                    )
                    if (curEndX >= startScaleX + sectionWidth * 4) {
                        rectPaint.color =
                            if (titleFullColor != 0) titleFullColor else Color.parseColor("#FFF6DA")
                        canvas.drawRect(
                            startScaleX + sectionWidth * 4,
                            0f,
                            if (curEndX <= startScaleX + sectionWidth * 5) curEndX else startScaleX + sectionWidth * 5,
                            titleHeight,
                            rectPaint
                        )
                    }
                }
            }
        }
    }

    private fun sp2px(spVal: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            spVal.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    private fun getPointX(pointX: Float): Float {
        return when {
            pointX <= xTypeList[0] -> {
                startScaleX + pointX * prepareUnitValueX
            }

            pointX <= xTypeList[1] + xTypeList[0] -> {
                startScaleX + xTypeList[0] * prepareUnitValueX + (pointX - xTypeList[0]) * inflationUnitValueX
            }

            pointX <= xTypeList[2] + xTypeList[1] + xTypeList[0] -> {
                startScaleX + xTypeList[0] * prepareUnitValueX + xTypeList[1] * inflationUnitValueX + (pointX - xTypeList[0] - xTypeList[1]) * stabilizationUnitValueX
            }

            pointX <= xTypeList[3] + xTypeList[2] + xTypeList[1] + xTypeList[0] -> {
                startScaleX + xTypeList[0] * prepareUnitValueX + xTypeList[1] * inflationUnitValueX + xTypeList[2] * stabilizationUnitValueX + (pointX - xTypeList[0] - xTypeList[1] - xTypeList[2]) * detectionUnitValueX
            }

            pointX <= xTypeList[4] + xTypeList[3] + xTypeList[2] + xTypeList[1] + xTypeList[0] -> {
                startScaleX + xTypeList[0] * prepareUnitValueX + xTypeList[1] * inflationUnitValueX + xTypeList[2] * stabilizationUnitValueX + xTypeList[3] * detectionUnitValueX + (pointX - xTypeList[0] - xTypeList[1] - xTypeList[2] - xTypeList[3]) * exhaustTimeUnitValueX
            }

            else -> startScaleX
        }
    }

    fun setPointData(pointX: Float, pointY: Float) {
        if (pointX > mXMax || pointY > mYMax) {
            return
        }
        val x = getPointX(pointX)
        val y = startScaleY - 2f - pointY * unitValueY
        tempDataMapList[x] = y
        postInvalidate()
    }

    fun setAllPointer(list: MutableMap<Float, Float>) {
        tempDataMapList.clear()
        for (key in list.keys) {
            val pointY = list[key] ?: continue
            if (key > mXMax || pointY > mYMax) {
                continue
            }
            val x = getPointX(key)
            val y = startScaleY - 2f - pointY * unitValueY
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
        unitValueY = (startScaleY - endScaleY) / mYMax
        postInvalidate()
    }

    fun setPressureUnit(unit: String, space: Float) {
        pressureUnit = unit
        spaceWidth = space
        postInvalidate()
    }

    companion object {
        const val KPA = "kPa"
        const val PA = "Pa"
        const val PSI = "psi"

        const val DEFAULT_GRID_NUMBER = 4
        const val DEFAULT_MAX_VALUE_Y = 36f
        const val DEFAULT_MAX_VALUE_X = 70
        const val DEFAULT_TITLE_HEIGHT = 32f
        private const val DEFAULT_TEXT_SIZE = 12 //字体大小
        private val DEFAULT_COLOR_GRID = Color.parseColor("#006837") //网格颜色
    }
}