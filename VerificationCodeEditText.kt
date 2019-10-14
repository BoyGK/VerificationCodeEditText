package com.example.wedget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Handler
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View.OnClickListener
import android.view.inputmethod.EditorInfo
import android.widget.EditText

/**
 * Created by BaiGuoQing on 2019/10/14.
 */

class VerificationCodeEditText : EditText {

    /**
     * default text num
     */
    var mTextNum = 4
        set(value) {
            field = value
            mTextPath = Array(value) { Path() }
            mTextString = Array(mTextNum) { String() }
            mLinePath = Array(mTextNum) { Path() }
        }

    /**
     * text params
     */
    private var mTextPath: Array<Path> = Array(mTextNum) { Path() }
    private var mTextString: Array<String> = Array(mTextNum) { String() }
    private val mTextPaint = Paint()
    private val mTextStrokeWidth = 2f
    var mTextSize = dp2px(24)
    var mTextColor = Color.BLACK

    /**
     * baseline params
     */
    private var mLinePath: Array<Path> = Array(mTextNum) { Path() }
    private val mLinePaint = Paint()
    var mLineColor = Color.BLACK
    var mLineColorSelected = Color.BLUE
    private val mLineStrokeWidth = dp2px(2)

    /**
     * baseline interval unit dp
     */
    var mLineInterval = dp2px(16)

    /**
     * left & right margin unit dp
     */
    var mLeftMargin = dp2px(16)
    var mRightMargin = dp2px(16)

    /**
     * cursor
     */
    private var mCursorIndex = 0
    private var mCursorOffset = 0
    private val mCursorPaint = Paint()
    private val mCursorStrokeWidth = 2f
    private val mTimeInterval = 500L
    var mCursorColor = Color.BLACK
    var isShowCursorBar: Boolean = true
        set(value) {
            field = value
            if (value) {
                mCursorAnimationHandler.removeCallbacks(mCursorAnimationRunnable)
                mCursorAnimationHandler.post(mCursorAnimationRunnable)
            } else {
                mCursorAnimationHandler.removeCallbacks(mCursorAnimationRunnable)
            }
        }

    /**
     * The number that has been entered
     */
    private var mInputNum = 0

    /**
     * code变动响应
     */
    private val mTextWatcher: TextWatcher = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            mInputNum = s.toString().length
            if (mInputNum > mTextNum) {
                mInputNum = mTextNum
                return
            } else if (mInputNum < 0) {
                mInputNum = 0
                return
            }
            if (mInputNum == mTextNum) {
                mCodeWatcher?.codeWatch(s.toString())
            }
            for (i in 0 until mTextNum) {
                mTextString[i] = ""
            }
            for (i in 0 until s.length) {
                mTextString[i] = s[i].toString()
            }
            mCursorIndex = mInputNum
        }
    }

    /**
     * 点击获取焦点
     */
    private val mClick: OnClickListener = OnClickListener {
        isFocusableInTouchMode = true
        requestFocus()
        mClick4Outer?.onClick(it)
    }

    /**
     * 禁止长按弹窗
     */
    private val callback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return false
        }

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
        }

    }

    /**
     * 光标闪烁效果
     */
    private val mCursorAnimationHandler: Handler = Handler()
    private val mCursorAnimationRunnable: Runnable = object : Runnable {
        override fun run() {
            if (mCursorPaint.color == mCursorColor) {
                mCursorPaint.color = Color.WHITE
            } else {
                mCursorPaint.color = mCursorColor
            }
            invalidate()
            mCursorAnimationHandler.postDelayed(this, mTimeInterval)
        }
    }

    constructor(context: Context) : this(
        context,
        null
    )

    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        0
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        defaultOption()
        addTextChangedListener(mTextWatcher)
        super.setOnClickListener(mClick)
    }

    init {
        mTextPaint.color = mTextColor
        mTextPaint.textSize = mTextSize.toFloat()
        mTextPaint.strokeWidth = mTextStrokeWidth
        mTextPaint.style = Paint.Style.FILL_AND_STROKE

        mLinePaint.color = mLineColor
        mLinePaint.style = Paint.Style.FILL_AND_STROKE
        mLinePaint.strokeWidth = mLineStrokeWidth.toFloat()

        mCursorPaint.color = mCursorColor
        mCursorPaint.strokeWidth = mCursorStrokeWidth
        mCursorPaint.style = Paint.Style.FILL_AND_STROKE

        if (isShowCursorBar) {
            mCursorAnimationHandler.post(mCursorAnimationRunnable)
        }
    }

    /**
     * default setting
     */
    private fun defaultOption() {
        background = null
        customSelectionActionModeCallback = callback
        imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
        isLongClickable = false
        inputType = InputType.TYPE_CLASS_NUMBER
        isCursorVisible = false
    }

    override fun onDraw(canvas: Canvas) {

        val thisTop = 0
        val thisBottom = bottom - top
        val lineWidth =
            (width - mLeftMargin - mRightMargin - mLineInterval * (mTextNum - 1)) / mTextNum
        var thisLeft = left

        for (i in 0 until mTextNum) {
            val startX = (thisLeft + mLeftMargin).toFloat()
            val fontMetrics = mTextPaint.fontMetrics
            val distance = (fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.bottom
            val baseline = (thisTop + thisBottom) / 2f + distance
            val offsetBaseLine2Bottom = distance / 2
            val baseLineY = thisBottom - mLineStrokeWidth - offsetBaseLine2Bottom

            mTextPaint.textAlign = Paint.Align.CENTER
            mTextPath[i].moveTo(startX, baseline)
            mTextPath[i].lineTo(startX + lineWidth, baseline)
            mLinePath[i].moveTo(startX, baseLineY)
            mLinePath[i].lineTo(startX + lineWidth, baseLineY)

            if (mInputNum - 1 == i) {
                mLinePaint.color = mLineColorSelected
            } else {
                mLinePaint.color = mLineColor
            }
            canvas.drawTextOnPath(mTextString[i], mTextPath[i], 0f, 0f, mTextPaint)
            canvas.drawPath(mLinePath[i], mLinePaint)

            if (isShowCursorBar && hasFocus()) {
                if (mCursorIndex == i) {
                    mCursorOffset = (thisBottom - thisTop) / 3
                    canvas.drawLine(
                        startX + lineWidth / 2,
                        (thisTop + mCursorOffset).toFloat(),
                        startX + lineWidth / 2,
                        (thisBottom - mCursorOffset).toFloat(),
                        mCursorPaint
                    )
                }
            }

            thisLeft += lineWidth + mLineInterval
        }

    }

    private var mClick4Outer: OnClickListener? = null

    override fun setOnClickListener(l: OnClickListener?) {
        mClick4Outer = l
    }

    /**
     * use this function can change px to dp
     */
    fun dp2px(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }


    interface VerificationCodeWatcher {
        /**
         * callback after filling
         */
        fun codeWatch(vfCode: String)
    }

    var mCodeWatcher: VerificationCodeWatcher? = null
}
