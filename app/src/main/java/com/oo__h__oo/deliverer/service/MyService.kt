package com.oo__h__oo.deliverer.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import java.util.*

class MyService : Service() {
    lateinit var handler: Handler

//    Service启动时调用
    override fun onCreate() {
        super.onCreate()
        Log.v("Service", "OnCreate 服务启动")
        handler = Handler(Looper.getMainLooper())
        handler.post { Toast.makeText(applicationContext, "服务启动", Toast.LENGTH_LONG).show() }
        Timer().schedule(object : TimerTask() {
            override fun run() {
                Log.v("Service", "服务还活着")
//                handler = Handler(Looper.getMainLooper())
//                handler.post { Toast.makeText(applicationContext, "服务还活着", Toast.LENGTH_LONG).show() }
            }
        }, 2000, (60 * 60 * 1000).toLong())
    }
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    //服务被关闭时调用
    override fun onDestroy() {
        super.onDestroy()
        Log.v("Service", "onDestroy 服务关闭")
        handler = Handler(Looper.getMainLooper())
        handler.post { Toast.makeText(applicationContext, "服务关闭", Toast.LENGTH_LONG).show() }
    }
}
