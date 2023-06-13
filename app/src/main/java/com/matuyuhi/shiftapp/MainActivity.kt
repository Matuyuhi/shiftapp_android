package com.matuyuhi.shiftapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.annotation.RequiresApi
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.matuyuhi.shiftapp.component.ShiftFunc
import com.matuyuhi.shiftapp.component.myExceptionHandler
import com.matuyuhi.shiftapp.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private lateinit var analytics: FirebaseAnalytics

    private var shiftFunc = ShiftFunc()
    private var sharedPref: SharedPreferences? = null

    private var isInputUsername: Boolean by Delegates.observable(false) { _, _, _ ->
        changeButtonEnable()
    }
    private var isInputPassword: Boolean by Delegates.observable(false) { _, _, _ ->
        changeButtonEnable()
    }

    private lateinit var binding: ActivityMainBinding
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Firebase 
        analytics = Firebase.analytics
        // crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        FirebaseCrashlytics.getInstance().sendUnsentReports()
        //Thread.setDefaultUncaughtExceptionHandler(myExceptionHandler())

        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        sharedPref = getSharedPreferences("userdata", Context.MODE_PRIVATE)
        //login
        println(sharedPref?.getString("token",""))
        if (sharedPref?.getString("token", "") != "") {
            goHomeView()
        }
        with (sharedPref?.edit()) {
            // todo ここにHOST
            this?.putString("url", "")
            this?.apply()
        }
        supportActionBar?.hide()
//        binding.countButton.setOnClickListener {
//            updateCount()
//            binding.responseText.text = ' '.toString()
//            // コルーチン定義
//            CoroutineScope(Dispatchers.IO).launch {
//               fetchHttp()
//            }
//        }
//        val countButton : Button = findViewById(R.id.countButton)
//        val countLabel : Button = findViewById(R.id.countLabel)
        setting()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setting() {
        binding.InputPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 入力内容が空でない場合にボタンを有効にする
                isInputPassword = s.toString() != ""
            }
        })

        binding.InputUsername.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 入力内容が空でない場合にボタンを有効にする
                isInputUsername = s.toString() != ""
            }
        })

        binding.loginButton.setOnClickListener {
            clickButton()
        }
    }

    private fun changeButtonEnable() {
        if (isInputPassword && isInputUsername) {
            binding.loginButton.isEnabled = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun clickButton() {
        //binding.responseText.text = "no res"
        // コルーチン定義
        CoroutineScope(Dispatchers.IO).launch {
            val login = shiftFunc.getLogin(sharedPref?.getString("url", "")+"/ios/login",
                binding.InputUsername.text.toString(),
                binding.InputPassword.text.toString())
//            withContext(Dispatchers.Main) {
//                if (login.optString("message", "") != "") {
//                    binding.responseText.text = login.get("message").toString()
//                }
//                else {
//                    binding.responseText.text = "エラー"
//                }
//
//
//            }
            if (login.optBoolean("verify", false) && login.optString("token", "") != "") {

                with (sharedPref?.edit()) {
                    this?.putString("token", login.optString("token"))
                    this?.apply() // 変更をコミット
                }
                println("login成功")
                goHomeView()
            }


        }
    }

    private fun goHomeView() {
        val intent = Intent(this, SecondActivity::class.java)
        startActivity(intent)
        finish()
    }

}
