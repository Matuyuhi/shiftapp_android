package com.matuyuhi.shiftapp.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
//import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.matuyuhi.shiftapp.component.setImageFromUrl
//import com.matuyuhi.shiftapp.component.MyComposeView
import com.matuyuhi.shiftapp.databinding.FragmentProfileviewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ProfileViewFragment: Fragment() {
    private var binding: FragmentProfileviewBinding? = null

    //ラムダ式
    lateinit var logoutLambda: () -> Unit

    private var iconUrl: String = ""
    private var userName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileviewBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListener()


    }

    private fun setListener(){
        binding?.logoutButton?.setOnClickListener{
            //引数なしの場合は→なし
            logoutLambda()
        }
    }
    private suspend fun redraw(){
        binding.let{
            if (it == null) {
                return
            }
            withContext(Dispatchers.Main) {
                it.Username.text = userName
                if (iconUrl != ""){
                    it.userImage.setImageFromUrl(iconUrl)
                }
            }
        }
    }

    fun reload() {
        CoroutineScope(Dispatchers.IO).launch {
            redraw()
        }
    }





    init {
//        val myComposeView = ComposeView(requireContext()).apply {
//            setContent {
//                MyComposeView()
//            }
//        }
//        binding.profileView.addView(myComposeView)


    }

    fun setUserName(username: String) {
        this.userName = username
    }
    fun setUserIcon(iconUrl: String) {
        if (iconUrl != "") {
            this.iconUrl = iconUrl
        }


    }
}