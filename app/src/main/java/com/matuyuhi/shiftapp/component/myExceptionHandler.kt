package com.matuyuhi.shiftapp.component

import com.google.firebase.crashlytics.FirebaseCrashlytics

class myExceptionHandler : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
        // デフォルトの例外ハンドラーを呼び出す（アプリがクラッシュしないようにするため）
        // https://developer.android.com/reference/java/lang/Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread,%20java.lang.Throwable)
//        Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(thread, throwable)
    }
}