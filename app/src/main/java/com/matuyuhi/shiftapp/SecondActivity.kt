package com.matuyuhi.shiftapp



import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.matuyuhi.shiftapp.component.ShiftFunc
import com.matuyuhi.shiftapp.component.ShiftView
import com.matuyuhi.shiftapp.component.ViewPagerAdapter

import com.matuyuhi.shiftapp.databinding.ActivitySecondBinding
import com.matuyuhi.shiftapp.pages.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray


class SecondActivity : AppCompatActivity() {

    private var sharedPref: SharedPreferences? = null

    private lateinit var binding: ActivitySecondBinding

    private var shiftView = ShiftViewFragment()
    private var userView = UserViewFragment()
    private var missView = MissViewFragment()
    private var profileView =  ProfileViewFragment()

    private val shiftFunc = ShiftFunc()

    private lateinit var tabLayout: TabLayout

    private var viewFragmentId: Int? = null

    private var isLoading: Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_second)
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.bottomNav_background)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tabLayout = binding.tabLayout

        setupViewPager()

        sharedPref = getSharedPreferences("userdata", Context.MODE_PRIVATE) // SharedPreferencesのインスタンスを取得

        //ボタンにリスナーを設定
        binding.reloadButton.setOnClickListener {
            val animation = RotateAnimation(
                0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
            )
            animation.duration = 1000
            animation.setAnimationListener(object: Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    // アニメーションが開始されたときに呼び出される
//                    println("start anime")
                }

                override fun onAnimationEnd(animation: Animation?) {
                    // アニメーションが終了したときに呼び出される
                    // ここで処理を実行する
                    if (isLoading) {
                        binding.reloadButton.startAnimation(animation)
                    }
                }

                override fun onAnimationRepeat(animation: Animation?) {
                    // アニメーションが繰り返されるときに呼び出される
//                    println("start replay")
                }
            })
            binding.reloadButton.startAnimation(animation)
            isLoading = true
            CoroutineScope(Dispatchers.IO).launch {
                getShift()
            }
        }

        //ラムダ式の設定
        profileView.logoutLambda = { returnLogin() }
        shiftView.getShiftLambda = { getShift() }


        // val item = navBottomView.menu.findItem(R.id.navigation_ProfileView)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun start() {
        viewFragmentId = R.id.navigation_ShiftView
//        replaceFragment(shiftView)
        val job = CoroutineScope(Dispatchers.IO).launch {
            getShift()
        }
        job.join()
    }


    //ここでmainをいじってる
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getShift() {
        var userList: JSONArray? = null
        var shiftList: JSONArray? = null
        var username: String? = null
        val job = CoroutineScope(Dispatchers.IO).launch {

            val result = shiftFunc.getShift(
                sharedPref?.getString("url", "").toString(),
                sharedPref?.getString("token", "").toString()
            )

            shiftView.whologin = result.getString("user")

            userList = result.optJSONArray("userlist")
            shiftList = result.optJSONArray("shift")
            username = result.optString("user", "username")
        }
        job.join()
        userList?.let {user ->
            userView.setUserList(user)
            shiftView.setUserList(user)
            username?.let {name ->
                for (u_i in 0 until user.length()) {
                    val info = user.getJSONObject(u_i)
                    if (info.optString("name", "name")==name){
                        profileView.setUserIcon(info.optString("icon",""))
                        break
                    }
                }
                profileView.setUserName(name)
            }
        }
        shiftList?.let {
            shiftView.setShift(it)
        }
        changeFunc()
        isLoading = false
    }

    //logoutボタンをクロージャーで渡す
    private fun returnLogin() {

        sharedPref?.edit {
            this.putString("token","")
        }
        println("トークン情報")
        sharedPref?.getString("token","")
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    var changeFunc: () -> Unit = {
        when (viewFragmentId) {
            0 -> {
                shiftView.reload()
            }
            1 -> {
                userView.reloadHome()
            }
            2 -> {

            }
            3 -> {
                profileView.reload()
            }
            else -> {
            }
        }
    }

    private fun setupViewPager() {
        val viewPager2 = binding.viewPager2
        // ViewPagerAdapterのインスタンス化
        viewPager2.adapter = ViewPagerAdapter(this).apply {
            addFragment(shiftView, "ShiftView")
            addFragment(userView, "UserView")
            addFragment(missView, "MissView")
            addFragment(profileView, "ProfileView")


            // OnPageChangeCallbackの登録
            onScrollEnd = {
                println("Scroll End")
            }
            onScrollStart = {
                println("Scroll Start")
            }
            onChangePosition = { pos ->
                tabLayout.selectTab(tabLayout.getTabAt(pos))
                viewFragmentId = pos
                changeFunc()
            }
            viewPager2.registerOnPageChangeCallback(pageChangeFunc)
        }
        val icons = arrayOf(
            R.drawable.baseline_home_24,
            R.drawable.baseline_format_list_bulleted_24,
            R.drawable.baseline_sentiment_very_dissatisfied_48,
            R.drawable.baseline_sentiment_very_dissatisfied_48
        )
        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            tab.setIcon(icons[position])
        }.attach()

    }


}
