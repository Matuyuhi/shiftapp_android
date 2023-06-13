package com.matuyuhi.shiftapp.component

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.matuyuhi.shiftapp.R.*

class ShiftView: ConstraintLayout {
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
        this.setTextColor(ContextCompat.getColor(context, color.TextView_Tintcolor))
        textSize = 14f
    }
    private var textView2: TextView = TextView(context).apply{
        this.setTextColor(ContextCompat.getColor(context, color.TextView_SubTintcolor))
        textSize = 11f
    }
    private var LinearShift: LinearLayout = LinearLayout(context).apply {
        this.orientation = LinearLayout.VERTICAL
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

    @SuppressLint("ResourceAsColor")
    private fun init(context: Context) {
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(8, 8, 8, 8)
            setBackgroundColor(ContextCompat.getColor(context, color.TextView_background))
        }
        // カスタムフォントを設定する場合
        // typeface = Typeface.createFromAsset(context.assets, "your_custom_font.ttf")
        // imageView を配置する
        // ImageView のインスタンスを生成し、レイアウトパラメータに id を指定して追加する
        addView(imageView, LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            startToStart = LayoutParams.PARENT_ID
            topToTop = LayoutParams.PARENT_ID
        })
        addView(LinearShift,LayoutParams(0, 0).apply {
            startToEnd = imageView.id
            endToEnd = LayoutParams.PARENT_ID
            marginStart = 20 // 適宜調整
            topToTop = LayoutParams.PARENT_ID
            bottomToBottom = LayoutParams.PARENT_ID

        })

        LinearShift.addView(textView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
//            startToEnd = imageView.id
//            endToEnd = LayoutParams.PARENT_ID
            marginStart = 40 // 適宜調整
//            topToTop = LayoutParams.PARENT_ID

        })

        LinearShift.addView(textView2, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
//            startToEnd = imageView.id
//            endToEnd = LayoutParams.PARENT_ID
            marginStart = 40 // 適宜調整
//            bottomToBottom = LayoutParams.PARENT_ID
        })
        textView2.text = "BMC"



    }

    @SuppressLint("SetTextI18n")
    fun setShift(_in: String, _out: String, _name: String, _comment: String="") {
        textView.text = "$_in-$_out $_name"
        textView2.text = _comment
    }



}

fun ImageView.setImageFromUrl(url: String) {
    val requestOptions = RequestOptions().apply {
        override(100, 100) // 画像のサイズを変更 (幅: 100px, 高さ: 100px)
    }
    Glide.with(context)
        .load(url)
        .apply(requestOptions)
        .into(object : CustomTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                this@setImageFromUrl.setImageDrawable(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                this@setImageFromUrl.setImageDrawable(placeholder)
            }
        })
}