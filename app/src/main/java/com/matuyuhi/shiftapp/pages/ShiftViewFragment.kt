package com.matuyuhi.shiftapp.pages

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.widget.PopupWindowCompat
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.matuyuhi.shiftapp.R.*
import com.matuyuhi.shiftapp.component.ShiftFunc
import com.matuyuhi.shiftapp.component.ShiftView
import com.matuyuhi.shiftapp.component.WeekButton
import com.matuyuhi.shiftapp.component.setImageFromUrl
import com.matuyuhi.shiftapp.databinding.FragmentShiftviewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.properties.Delegates


class ShiftViewFragment: Fragment() {
    private var sharedPref: SharedPreferences? = null
    private lateinit var binding: FragmentShiftviewBinding

    private val shiftFunc = ShiftFunc()

    private var shiftList: JSONArray? = null
    private var userList: JSONArray? = null
    lateinit var whologin: String

    private val weekButtons = mutableListOf<WeekButton>()

    //シフト表示用の変数
    private var viewIndex: Int = 0
    private var timerangeIndex: Int = 0
    private var changePersonOption: String = ""

    //シフト追加関連の変数
    @Suppress("DEPRECATION")
    private val handler = Handler()
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var dateEdittext: EditText
    private lateinit var inTimeEdittext:EditText
    private lateinit var outTimeEdittext:EditText
    private lateinit var commentEditText: EditText
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var inTimeSwitch:Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var outTimeSwitch: Switch
    private lateinit var cancelButton:Button
    private lateinit var submitButton: Button
    private lateinit var timeTabLayout: TabLayout
    private lateinit var changePersonButton: ImageButton

    //99:99選択時のもとの値保持用
    private lateinit var inTimeDefault:String
    private lateinit var outTimeDefault:String

    //ラムダ式
    lateinit var getShiftLambda: suspend () -> Unit


    //送信ボタンのOn Offの切り替え
    private var isInputDate: Boolean by Delegates.observable(false) { _, _, _ ->
        submitButtonEnable()
    }
    private var isInputInTime: Boolean by Delegates.observable(false) { _, _, _ ->
        submitButtonEnable()
    }
    private var isInputOutTime: Boolean by Delegates.observable(false) { _, _, _ ->
        submitButtonEnable()
    }
    private fun submitButtonEnable() {
        if (isInputDate && isInputInTime && isInputOutTime) {
            println("正しく入力できています")
            binding.postView.submitButton.isEnabled = true
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShiftviewBinding.inflate(inflater, container, false)
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onGlobalLayout() {
                val width = binding.root.width

                println("width $width")
                //widthが決まったら曜日viewを作成
                initWeekView(width)
                // 初期位置はALL
                changeView(0)

                // リスナーを削除する
                binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        //viewのインスタンス化
        timeTextView = binding.postView.timetext
        dateTextView = binding.postView.datetext
        dateEdittext = binding.postView.dateedit
        inTimeEdittext = binding.postView.intimeedit
        inTimeSwitch = binding.postView.intimeswitch
        outTimeEdittext = binding.postView.outtimeedit
        outTimeSwitch = binding.postView.outtimeswitch
        submitButton = binding.postView.submitButton
        cancelButton = binding.postView.cancelButton
        commentEditText = binding.postView.commentedit
        timeTabLayout = binding.timetablayout
        changePersonButton = binding.changePersonButton
        //viewの初期設定
        binding.postView.shiftpostview.visibility = View.GONE
        binding.postView.submitButton.isEnabled = false
//        binding.cover.visibility = View.GONE
        timeTabLayout.addTab(timeTabLayout.newTab().setText("今週"))
        timeTabLayout.addTab(timeTabLayout.newTab().setText("来週"))
        timeTabLayout.addTab(timeTabLayout.newTab().setText("全部"))


        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPref = requireContext().getSharedPreferences("userdata", Context.MODE_PRIVATE) // SharedPreferencesのインスタンスを取得

        //以下シフト作成ページの設定
        // 時間表示の設定(formの時刻も含む)
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                handler.post {
                    // 現在の日時を取得
                    val calendar = Calendar.getInstance()

                    //年月日表示
                    val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                    //val sdfForForm = SimpleDateFormat("MM/dd", Locale.getDefault())
                    val date = sdf.format(calendar.time)
                    //val dateForForm = sdfForForm.format(calendar.time)
                    dateTextView.text = date
                    //フォームの値を初期化
                    if(binding.postView.shiftpostview.visibility != View.VISIBLE){
                        dateEdittext.setText(date)
                    }

                    // 時刻表示
                    val now = calendar.time
                    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                    timeTextView.text = format.format(now)
                }
            }
        }, 0, 1000)

        //入力formのりすなー設定
        setListner()

        reload()
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setListner(){
        //表示範囲の変更
        timeTabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {
                //最初の表示設定は決める
                val index = tab.position
                println("現在選択されてるのは$index")
                timerangeIndex = index
                //ここで表示の情報を変更
                reload()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {
            }
            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

        //ページ遷移
        //シフト追加ボタン起動
        binding.addshiftbutton.setOnClickListener{
            //ここでbackdropを呼ぶ
            //binding.overlay.visibility = View.VISIBLE
            val slideUp = TranslateAnimation(0f, 0f, 2000f, 0f)
            slideUp.setAnimationListener(object: Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    // アニメーションが開始されたときに呼び出される
//                    println("start anime")
                    binding.postView.shiftpostview.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animation?) {
                    // アニメーションが終了したときに呼び出される
                    // ここで処理を実行する
                }

                override fun onAnimationRepeat(p0: Animation?) {

                }

            })
            slideUp.duration = 250
            binding.postView.shiftpostview.startAnimation(slideUp)
//            binding.cover.visibility = View.VISIBLE
        }

        //入力form関連
        //日付
        dateEdittext.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 入力内容が空でない場合にボタンを有効にする
                isInputDate = s.toString() != ""
            }
        })

        dateEdittext.setOnClickListener{
            println("日付入力スタート")
            showDatePickerDialog()
        }
        //開始時刻
        inTimeEdittext.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (inTimeEdittext.text.toString() != "99:99"){
                    inTimeSwitch.isChecked = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 入力内容が空でない場合にボタンを有効にする
                isInputInTime = s.toString() != ""
            }
        })

        inTimeEdittext.setOnClickListener {
            println("時刻入力スタート")
            showIntimePickerDialog()
        }

        inTimeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                inTimeDefault = inTimeEdittext.text.toString()
                inTimeEdittext.setText("99:99")
            } else {
                inTimeEdittext.setText(inTimeDefault)
            }
        }

        //終了時刻
        outTimeEdittext.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (outTimeEdittext.text.toString() != "99:99"){
                    outTimeSwitch.isChecked = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 入力内容が空でない場合にボタンを有効にする
                isInputOutTime = s.toString() != ""
            }
        })

        outTimeEdittext.setOnClickListener {
            println("退社時刻入力スタート")
            showOuttimePickerDialog()
        }

        outTimeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                outTimeDefault = outTimeEdittext.text.toString()
                outTimeEdittext.setText("99:99")
            } else {
                outTimeEdittext.setText(outTimeDefault)
            }
        }

        //送信、cancelボタン
        cancelButton.setOnClickListener{
            println("closed")
            //初期化
            val slideUp = TranslateAnimation(0f, 0f, 0f, 2000f)
            slideUp.setAnimationListener(object: Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    // アニメーションが開始されたときに呼び出される
//                    println("start anime")
                }

                override fun onAnimationEnd(animation: Animation?) {
                    // アニメーションが終了したときに呼び出される
                    // ここで処理を実行する
                    dateEdittext.setText("")
                    inTimeEdittext.setText("")
                    outTimeEdittext.setText("")
                    commentEditText.setText("")
                    inTimeSwitch.isChecked = false
                    outTimeSwitch.isChecked = false
                    binding.postView.shiftpostview.visibility = View.GONE
                    submitButton.isEnabled = false
                }

                override fun onAnimationRepeat(p0: Animation?) {

                }

            })
            slideUp.duration = 250
            binding.postView.shiftpostview.startAnimation(slideUp)

//            binding.cover.visibility = View.GONE
        }

        submitButton.setOnClickListener{
            println("submitted")
            //入力完了のpopupを出す
            val date = dateEdittext.text
            val intime = inTimeEdittext.text
            val localintime = intime.split(":")[0].toInt() * 60 + intime.split(":")[1].toInt()
            val outtime = outTimeEdittext.text
            val localouttime = outtime.split(":")[0].toInt() * 60 + outtime.split(":")[1].toInt()
            val comment = commentEditText.text


            //TODO: dateが一桁なら0を先頭につける 2023/4/6 -> 2023/04/06
            // * : intime > outtimeにならないように X in 17:00 out: 14:00
            // * : postしたらgetShiftして再描画する
            println("date -> $date intime -> $intime  outtime -> $outtime comment -> $comment")
            if ((localintime >= localouttime) && (intime.toString() != "99:99") && (outtime.toString() != "99:99")){
                println("bad format")
                //エラーメッセージの表示
                val builder = AlertDialog.Builder(context)
                builder.setMessage("終了時刻が開始時刻より前です")
                builder.setPositiveButton("OK") { _, _ ->
                }
                builder.show()
            }
            else{
                //初期化
                dateEdittext.setText("")
                inTimeEdittext.setText("")
                outTimeEdittext.setText("")
                commentEditText.setText("")
                inTimeSwitch.isChecked = false
                outTimeSwitch.isChecked = false
                submitButton.isEnabled = false
                //別スレッドで待たせるにはcoroutinで囲む
                CoroutineScope(Dispatchers.IO).launch {
                    shiftFunc.postShift(
                        sharedPref?.getString("url", "").toString(),
                        sharedPref?.getString("token", "").toString(),
                        // TODO : dateの先頭に年をつける 04/01 -> 2023/04/01
                        date="$date",
                        intime=intime.toString(), outtime=outtime.toString(),
                        comment=comment.toString()
                    )
                    getShiftLambda()
                }
                println("入力された時刻は$date")
                binding.postView.shiftpostview.visibility = View.GONE
//                binding.cover.visibility = View.GONE
            }
        }
    }


    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireActivity(), { _, year, month, dayOfMonth ->
            // 日付が選択された時の処理(format指定)

            val monthStrings:String = if(month <= 8){
                "0${month + 1}"
            } else {
                (month + 1).toString()
            }
            val dayStrings:String = if(dayOfMonth <= 9){
                "0$dayOfMonth"
            } else {
                dayOfMonth.toString()
            }
            val selectedDate = "$year/$monthStrings/$dayStrings"
            dateEdittext.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showIntimePickerDialog() {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val hourStrings:String = if(selectedHour <= 9){
                    "0$selectedHour"
                } else{
                    "$selectedHour"
                }
                val minuteStrings:String = if(selectedMinute <= 9){
                    "0$selectedMinute"
                } else{
                    "$selectedMinute"
                }
                inTimeEdittext.setText("$hourStrings:$minuteStrings")
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showOuttimePickerDialog() {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val hourStrings:String = if(selectedHour <= 9){
                    "0$selectedHour"
                } else{
                    "$selectedHour"
                }
                val minuteStrings:String = if(selectedMinute <= 9){
                    "0$selectedMinute"
                } else{
                    "$selectedMinute"
                }
                outTimeEdittext.setText("$hourStrings:$minuteStrings")
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun initWeekView(viewWidth: Int) {
        //今日の曜日 (1=日, 2=月....7=土)
        val calendar: Calendar = Calendar.getInstance()
        val dayOfWeek: Int = calendar.get(Calendar.DAY_OF_WEEK)

        val weekText = arrayOf("ALL", "月", "火", "水", "木", "金", "土", "日")
        val weekLayout: LinearLayout = binding.changeLinearView
        for (b_i in 0 until 8) {
            val weekButton: WeekButton = if (dayOfWeek-1 == b_i) {
                WeekButton(requireContext(), viewWidth / 8, true)
            }else{
                WeekButton(requireContext(), viewWidth / 8, false)
            }

            val layoutParams = LinearLayout.LayoutParams((viewWidth / 8) - 20, (viewWidth / 8) - 20).apply {
                setMargins(10,10,10,10)
            }
            weekButton.layoutParams = layoutParams
            weekButton.text = weekText[b_i]
            weekButton.setOnClickListener {
                this.changeView(b_i)
            }
            weekButtons += weekButton
            weekLayout.addView(weekButton)
        }
        println("date is : $dayOfWeek")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun changeView(viewIndex: Int){
        this.viewIndex = viewIndex+1
        for (b_i in weekButtons.indices){
            if (b_i == viewIndex){
                weekButtons[b_i].change(true)
            }else{
                weekButtons[b_i].change(false)
            }
        }
        reload()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "ResourceAsColor")
    private suspend fun reDraw(list: JSONArray) {
        //userlistを取得してプルダウンを更新
        userList?.let {
            //val menuelements = arrayOf("nakajima", "yamane")
            //一はあとで微調整
            val changePersonPopup = PopupMenu(context, changePersonButton,Gravity.BOTTOM)
            //全員分表示のところ
            val menuItemAll = changePersonPopup.menu.add(Menu.NONE,0,0,"全員分表示")
            menuItemAll.setOnMenuItemClickListener{item ->
                changePersonOption = item.title.toString()
                reload()
                false
            }
            //whologinに基づいてiconを表示する
            val menuItemSelf = changePersonPopup.menu.add(Menu.NONE,1,1,"自分のみ表示")
            menuItemSelf.setOnMenuItemClickListener{item ->
                changePersonOption = item.title.toString()
                reload()
                false
            }
            for (u_i in 0 until userList!!.length()) {
                Log.d("usercheck","${userList!!.getString(u_i)}")
                val userinfo = JSONObject(userList!!.getString(u_i))
                val name = userinfo.getString("name")
                if(whologin == name){
                    continue
                }
                val menuItem = changePersonPopup.menu.add(Menu.NONE,u_i + 2,u_i + 2,name)
                menuItem.setOnMenuItemClickListener{item ->
                    changePersonOption = item.title.toString()
                    reload()
                    false
                }
                menuItem.setIcon(drawable.baseline_person_12)
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }

            changePersonButton.setOnClickListener {
                /// 押下時にポップアップ表示
                println(changePersonPopup.menu.getItem(1).icon)
                changePersonPopup.show()
            }
        }

        // init shift list
        val linearLayout: LinearLayout = binding.ShiftListView
        // UIスレッドでUI要素を更新
        val context = requireContext()
        withContext(Dispatchers.Main) {
            linearLayout.removeAllViews()
            var nowDate = "0000-00-00"
            for (s_i in 0 until list.length()) {
                val shiftInfo = list.getJSONObject(s_i)
                val date = shiftInfo.optString("date","0000-00-00")

                //表示する人の変更
                if(changePersonOption == "全員分表示"){
                }else if(changePersonOption == "自分のみ表示"){
                    if(shiftInfo.getString("name") != whologin){
                        continue
                    }
                }else{
                    if(shiftInfo.getString("name") != changePersonOption && changePersonOption != ""){
                        continue
                    }
                }

                //範囲選択(timerangeIndexは0が今週、1が来週、2が全部)
                if (timerangeIndex == 0){
                    //今週
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val localdate = LocalDate.parse(date,formatter)
                    if(localdate > shiftFunc.getSundayOfCurrentWeek()){
                        continue
                    }
                }
                else if(timerangeIndex == 1){
                    //来週
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val localdate = LocalDate.parse(date,formatter)
                    if(localdate < shiftFunc.getMondayOfNextWeek() || localdate > shiftFunc.getSundayOfNextWeek()){
                        continue
                    }
                }
                else if(timerangeIndex == 2){
                }

                //曜日選択
                if (shiftFunc.getDayOfWeekIndex(date) != viewIndex && viewIndex != 1){
                    continue
                }

                //dateと曜日のラベルの設定（違う曜日がでてくるたびに更新）
                if (nowDate != date) {
                    nowDate = date
                    val dateView = TextView(context)
                    dateView.apply {
                        height = 100
                        width = LinearLayout.LayoutParams.MATCH_PARENT
                        text = "$nowDate (${shiftFunc.getDayOfWeek(nowDate)})"
                        gravity = Gravity.CENTER
                        textSize = 18f
                        setTextColor(color.TextView_Tintcolor)
                    }
                    linearLayout.addView(dateView)
                }

                //各シフトの設定
                val textView = ShiftView(context)
                textView.apply {
                    setShift(
                        _in = shiftInfo.optString("intime","??:??"),
                        _out = shiftInfo.optString("outtime","??:??"),
                        _name = shiftInfo.optString("name", "??"),
                        _comment = shiftInfo.optString("comment", ""),
                    )
                    userList?.let {
                        val userIndex = shiftInfo.optString("name", "").toString()
                        for (user_i in 0 until it.length()) {
                            val userInfo = it.getJSONObject(user_i)
                            if (
                                userIndex == userInfo.optString("name", "??") &&
                                userInfo.optString("icon","") != ""
                            ) {
                                imageView.setImageFromUrl(userInfo.getString("icon"))
                                break
                            }
                        }
                    }
                    setPadding(16, 16, 16, 16)
                }

                linearLayout.addView(textView)
            }
        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun reload() {
        CoroutineScope(Dispatchers.IO).launch {
            shiftList?.let { reDraw(it) }
        }
    }
    fun setShift(_shift: JSONArray) {
        shiftList = _shift
    }
    fun setUserList(_list: JSONArray) {
        userList = _list
    }
}
