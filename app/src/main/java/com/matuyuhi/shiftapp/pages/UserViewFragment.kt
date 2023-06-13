package com.matuyuhi.shiftapp.pages

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.matuyuhi.shiftapp.component.UserView
import com.matuyuhi.shiftapp.component.setImageFromUrl
import com.matuyuhi.shiftapp.databinding.FragmentUserviewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray


class UserViewFragment: Fragment() {
    private var sharedPref: SharedPreferences? = null
    private var binding: FragmentUserviewBinding? = null

    private var userList: JSONArray? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserviewBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPref = requireContext().getSharedPreferences("userdata", Context.MODE_PRIVATE) // SharedPreferencesのインスタンスを取得

        reloadHome()
//        val icon = ResourcesCompat.getDrawable(resources, )
//        binding.reloadButton.setCompoundDrawables()
    }

    private suspend fun reDraw(list: JSONArray) {
        val linearLayout: LinearLayout = binding?.HomeListView ?: return
        // UIスレッドでUI要素を更新
        val context = requireContext()
        withContext(Dispatchers.Main) {
            linearLayout.removeAllViews()
            for (u_i in 0 until list.length()) {
                val textView = UserView(context)
                val userinfo = list.getJSONObject(u_i)
                textView.apply {
                    setText(userinfo.optString("name","none"))
                    if (userinfo.optString("icon","") != "") {
                        imageView.setImageFromUrl(userinfo.getString("icon"))
                    }

                    setPadding(16, 16, 16, 16)
                }

                linearLayout.addView(textView)
            }
        }

    }


    fun reloadHome() {
        CoroutineScope(Dispatchers.IO).launch {
            userList?.let { reDraw(it) }
        }


    }

    fun setUserList(_list: JSONArray) {
        userList = _list
    }
}