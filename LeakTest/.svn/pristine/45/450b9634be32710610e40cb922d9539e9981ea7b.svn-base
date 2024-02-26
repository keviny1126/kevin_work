//package com.power.baseproject.widget
//
//import android.content.Context
//import android.graphics.Color
//import android.graphics.LinearGradient
//import android.graphics.Paint
//import android.graphics.Path
//import android.util.AttributeSet
//import android.view.View
//import androidx.core.content.ContextCompat
//import com.power.baseproject.R
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//
//class ProgressBottomView @JvmOverloads constructor(
//    context: Context,
//    attrs: AttributeSet? = null,
//    defStyleAttr: Int = 0
//) : View(context, attrs, defStyleAttr), CoroutineScope by CoroutineScope(
//    Dispatchers.Main
//) {
//
//    private var mWidth = 0f
//    private var mHeight = 0f
//    private var mYMax = 0f//Y轴最大值
//    private val mSectionY = 4 //Y轴等分份数
//    private var mXMax = 0//X轴最大值
//    private var mSectionX = 5 //X轴等分份数
//    private var gridColor = 0
//    private var textSize //文字大小
//            = 0
//    private lateinit var gridPaint: Paint
//    private lateinit var lineFullPaint: Paint
//    private lateinit var lineStrokePaint: Paint
//    private lateinit var mFullPath: Path
//    private lateinit var mStrokePath: Path
//    private var fontMetrics //字体度量
//            : Paint.FontMetrics? = null
//    private lateinit var mGradientColor: IntArray
//    private var linearGradient: LinearGradient? = null
//    private var tempDataMapList = mutableMapOf<Float, Float>()//历史坐标点集合
//    private var xTypeMapList = mutableMapOf<Int, Int>()//x轴各功能值
//    private var unitValueY: Float = 1f
//    private var unitValueX: Float = 1f
//    init {
//        initAttrs(context, attrs)
//        initObject()
//    }
//
//    private fun initAttrs(context: Context, attrs: AttributeSet?) {
//        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CurProgressView)
//        mYMax = attributes.getFloat(
//            R.styleable.CurProgressView_y_max_value,
//            CurProgressView.DEFAULT_MAX_VALUE_Y
//        )
//        mXMax = CurProgressView.DEFAULT_MAX_VALUE_X
//        for (i in 0 until mSectionX) {
//            xTypeMapList[i] = mXMax / mSectionX
//        }
//        gridColor = attributes.getColor(
//            R.styleable.CurProgressView_grid_color,
//            CurProgressView.DEFAULT_COLOR_GRID
//        )
//        textSize = attributes.getDimension(
//            R.styleable.CurProgressView_text_size_y,
//            sp2px(CurProgressView.DEFAULT_TEXT_SIZE).toFloat()
//        ).toInt() //文字大小
//
//        attributes.recycle()
//    }
//
//    private fun initObject() {
//        gridPaint = Paint()
//        gridPaint.isAntiAlias = true //抗锯齿
//        gridPaint.style = Paint.Style.FILL //风格
//        gridPaint.textSize = textSize.toFloat() //文字大小
//        gridPaint.textAlign = Paint.Align.CENTER //排成一行 居中
//        fontMetrics = gridPaint.fontMetrics //获得字体度量
//        lineStrokePaint = Paint()
//        lineStrokePaint.isAntiAlias = true //抗锯齿
//        lineStrokePaint.style = Paint.Style.STROKE //风格
//        lineStrokePaint.color = Color.parseColor("#00C55C")
//
//        lineFullPaint = Paint()
//        lineFullPaint.isAntiAlias = true //抗锯齿
//        lineFullPaint.style = Paint.Style.FILL //风格
//        mGradientColor =
//            intArrayOf(ContextCompat.getColor(context, R.color.color_00EC6E), Color.WHITE)
//        mFullPath = Path()
//        mStrokePath = Path()
//    }
//
//    companion object {
//        const val DEFAULT_GRID_NUMBER = 4
//        const val DEFAULT_MAX_VALUE_X = 70
//        private const val DEFAULT_TEXT_SIZE = 12 //字体大小
//        private val DEFAULT_COLOR_GRID = Color.parseColor("#dddddd") //网格颜色
//    }
//}