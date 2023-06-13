package com.matuyuhi.shiftapp.component

import android.content.Context
import android.graphics.Outline
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.matuyuhi.shiftapp.R
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class UserView : ConstraintLayout {
    var imageView: ImageView = ImageView(context).apply{
        scaleType = ImageView.ScaleType.CENTER_CROP

        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                view?.let {
                    val radius = 50f
                    outline?.setRoundRect(0, 0, it.width, it.height, radius)
                }
            }
        }
        layoutParams = LayoutParams(0,0).apply {
            id = View.generateViewId()
        }
    }
    private var textView: TextView = TextView(context).apply{
        this.setTextColor(ContextCompat.getColor(context, R.color.TextView_Tintcolor))
        textSize = 14f
    }
    private var textView2: TextView = TextView(context).apply{
        this.setTextColor(ContextCompat.getColor(context, R.color.TextView_Tintcolor))
        textSize = 14f
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        layoutParams = ConstraintLayout.LayoutParams(
            MATCH_PARENT,
            130
        ).apply {
            setMargins(8, 8, 8, 8)
            setBackgroundColor(ContextCompat.getColor(context, R.color.TextView_background))
        }
        // カスタムフォントを設定する場合
        // typeface = Typeface.createFromAsset(context.assets, "your_custom_font.ttf")
        // imageView を配置する
        // ImageView のインスタンスを生成し、レイアウトパラメータに id を指定して追加する
        addView(imageView, LayoutParams( WRAP_CONTENT, WRAP_CONTENT).apply {
            startToStart = PARENT_ID
            topToTop = PARENT_ID
        })

        addView(textView, LayoutParams(MATCH_CONSTRAINT, WRAP_CONTENT).apply {
            startToEnd = imageView.id
            endToEnd = PARENT_ID
            marginStart = 40 // 適宜調整
            topToTop = PARENT_ID

        })

        addView(textView2, LayoutParams(MATCH_CONSTRAINT, WRAP_CONTENT).apply {
            startToEnd = imageView.id
            endToEnd = PARENT_ID
            marginStart = 40 // 適宜調整
            bottomToBottom = PARENT_ID
        })
        textView2.text = "BMC"



    }

    fun setText(text: String) {
        textView.text = text
    }


}
