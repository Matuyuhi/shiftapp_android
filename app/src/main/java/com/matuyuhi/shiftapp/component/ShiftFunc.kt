package com.matuyuhi.shiftapp.component

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class ShiftFunc {

    suspend fun fetchHttpGet(urlString: String) {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))

                val response = reader.use { it.readText() }
                // レスポンスを利用した処理を記述
                println(response)
                withContext(Dispatchers.Main) {
                    //binding.responseText1.text = response.toString()
                }

                connection.disconnect()
            }
        } catch (e: Exception) {
            Log.e("fetchHttp", "予期しないエラーが発生しました", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getLogin(urlString: String, name: String, pass: String): JSONObject {
        val url = URL(urlString)
        val connection = withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection
        try {
            println(urlString)


            // POSTデータを作成
            val postData = JSONObject().apply {
                put("username", name)
                put("password", pass)
            }
            val userCredentials = "bmc:lkj12345" // ユーザー名とパスワードを組み合わせた文字列
            val basicAuth = "Basic " + Base64.getEncoder().encodeToString(userCredentials.toByteArray())
            connection.setRequestProperty("Authorization", basicAuth)
            connection.requestMethod = "POST"

            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.doOutput = true

            // POSTデータを書き込みラッパー
            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer -> //安全に
                writer.write(postData.toString())
            }


            // リクエストを送信
            connection.connect()

            val responseCode = connection.responseCode
            println("response code : " + responseCode.toString())
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))

                val response = reader.use { it.readText() }
                // レスポンスを利用した処理を記述
                //println(response)
                val login = JSONObject(response)
                println(login)
                return login



            }
            return JSONObject().apply {
                put("verify", false)
            }
        } catch (e: Exception) {
            Log.e("fetchHttp", "予期しないエラーが発生しました", e)
            return JSONObject().apply {
                put("verify", false)
            }
        } finally {
            connection.disconnect()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getShift(hostname: String, token: String): JSONObject {
        val url = URL(hostname+ "/ios/get")
        val connection = withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection
        try {
            // POSTリクエストの設定
            connection.requestMethod = "GET"
            connection.setRequestProperty("token", token)
            val userCredentials = "bmc:lkj12345" // ユーザー名とパスワードを組み合わせた文字列
            val basicAuth = "Basic " + Base64.getEncoder().encodeToString(userCredentials.toByteArray())
            connection.setRequestProperty("Authorization", basicAuth)

            // リクエストを送信
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))

                val response = reader.use { it.readText() }
                // レスポンスを利用した処理を記述
                //println(response)
                val login = JSONObject(response)
                println(login)
                return login



            }
            return JSONObject().apply {
                //もし取れなかったら{verify:false}みたいなかんじのobjectがかえってくる
                put("verify", false)
            }
        } catch (e: Exception) {
            Log.e("fetchHttp", "予期しないエラーが発生しました", e)
            FirebaseCrashlytics.getInstance().recordException(e)
            return JSONObject().apply {
                put("verify", false)
            }
        } finally {
            connection.disconnect()
        }

    }
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun postShift(
        hostname: String, token: String,
        date: String, intime: String, outtime: String, comment: String,
        post_id: Int = 0
    ): JSONObject {
        val url = URL(hostname+ "/ios/post")
        val connection = withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection
        try {
            // POSTデータを作成
            val postData = JSONObject().apply {
                put("date", date)
                put("intime", intime)
                put("outtime", outtime)
                put("comment", comment)
                if (post_id != 0) put("post_id", post_id)
            }
            println("create body "+ postData)
            val userCredentials = "bmc:lkj12345" // ユーザー名とパスワードを組み合わせた文字列
            val basicAuth = "Basic " + Base64.getEncoder().encodeToString(userCredentials.toByteArray())
            connection.setRequestProperty("Authorization", basicAuth)
            // POSTリクエストの設定
            connection.requestMethod = "POST"
            connection.setRequestProperty("token", token)
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.doOutput = true

            // POSTデータを書き込みラッパー
            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer -> //安全に
                writer.write(postData.toString())
            }
            // リクエストを送信
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))

                val response = reader.use { it.readText() }
                // レスポンスを利用した処理を記述
                //println(response)
                val login = JSONObject(response)
                println(login)
                return login



            }
            return JSONObject().apply {
                //もし取れなかったら{verify:false}みたいなかんじのobjectがかえってくる
                put("verify", false)
            }
        } catch (e: Exception) {
            Log.e("fetchHttp", "予期しないエラーが発生しました", e)
            return JSONObject().apply {
                put("verify", false)
            }
        } finally {
            connection.disconnect()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getMiss(hostname: String, token: String): JSONObject {
        val url = URL(hostname+ "/ios/miss")
        val connection = withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection
        try {
            // POSTリクエストの設定
            connection.requestMethod = "GET"
            connection.setRequestProperty("token", token)
            val userCredentials = "bmc:lkj12345" // ユーザー名とパスワードを組み合わせた文字列
            val basicAuth = "Basic " + Base64.getEncoder().encodeToString(userCredentials.toByteArray())
            connection.setRequestProperty("Authorization", basicAuth)

            // リクエストを送信
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))

                val response = reader.use { it.readText() }
                // レスポンスを利用した処理を記述
                //println(response)
                val login = JSONObject(response)
                println(login)
                return login



            }
            return JSONObject().apply {
                //もし取れなかったら{verify:false}みたいなかんじのobjectがかえってくる
                put("verify", false)
            }
        } catch (e: Exception) {
            Log.e("fetchHttp", "予期しないエラーが発生しました", e)
            FirebaseCrashlytics.getInstance().recordException(e)
            return JSONObject().apply {
                put("verify", false)
            }
        } finally {
            connection.disconnect()
        }

    }

    fun getDayOfWeek(dateString: String): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = format.parse(dateString) ?: return ""
        val calendar = Calendar.getInstance()
        calendar.time = date
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val weekdays = arrayOf("日", "月", "火", "水", "木", "金", "土")
        return weekdays[dayOfWeek - 1]
    }
    fun getDayOfWeekIndex(dateString: String): Int {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = format.parse(dateString) ?: return 0
        val calendar = Calendar.getInstance()
        calendar.time = date
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return dayOfWeek
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getSundayOfCurrentWeek(): LocalDate? {
        val now = LocalDate.now()
        return now.with(DayOfWeek.SUNDAY)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMondayOfNextWeek(): LocalDate? {
        val now = LocalDate.now()
        return now.with(DayOfWeek.MONDAY).plusWeeks(1)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getSundayOfNextWeek(): LocalDate? {
        val now = LocalDate.now()
        return now.with(DayOfWeek.SUNDAY).plusWeeks(1)
    }

}