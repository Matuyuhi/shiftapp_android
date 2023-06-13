package com.matuyuhi.shiftapp.component

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.matuyuhi.shiftapp.R

class WeekButton : AppCompatButton {

    private var isday: Boolean = false
    @SuppressLint("UseCompatLoadingForDrawables")
    private val layers = arrayOf(
        resources.getDrawable(R.drawable.on_weekbutton, null),
        resources.getDrawable(R.drawable.off_weekbutton, null),
        resources.getDrawable(R.drawable.onday_weekbutton, null),
    )

    constructor(context: Context, width: Int, day: Boolean) : super(context) {
        init(context, width, day)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, 200, false)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context,200, false)
    }

    private fun init(context: Context, width: Int, isday: Boolean) {
        this.isday = isday
        val margin = (width * 0.1).toInt()
        // 独自の振る舞いやスタイルを実装する
        textSize = 16f
        setTypeface(null, Typeface.BOLD)
        setTextColor(ContextCompat.getColor(context, R.color.WeekButton_Tintcolor))
        setBackgroundColor(ContextCompat.getColor(context, R.color.WeekButton_background))
//        clipToOutline = true
//        outlineProvider = object : ViewOutlineProvider() {
//            override fun getOutline(view: View?, outline: Outline?) {
//                outline?.setRoundRect(margin, margin, width-margin, width-margin, (width-margin)/2.0f)
//
//            }
//        }


        change(false)

//        setOnClickListener {
//            isEnabled = !isEnabled
//        }
    }

    fun change(isOn: Boolean) {
        val layerDrawable = LayerDrawable(layers)
        if (!this.isday){
            layerDrawable.setDrawable(2, null)
        }
        if (isOn){
            layerDrawable.setDrawable(1, null)
        }
        background = layerDrawable

    }


}