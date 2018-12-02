package com.example.oo__h__oo.deliverer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.ListView
import com.bigkoo.alertview.AlertView
import com.example.oo__h__oo.deliverer.manager.Message
import java.io.File
import java.util.*
import android.widget.Toast
import com.example.oo__h__oo.deliverer.adapter.SmsListAdapter
import com.example.oo__h__oo.deliverer.service.MyService


class MainActivity : AppCompatActivity() {

    private var smsList : MutableList<Message> = listOf<Message>().toMutableList()
    private lateinit var smsListAdapter: SmsListAdapter
    private lateinit var listView :ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //启动Service
        var intent = Intent().setClass(this,MyService().javaClass)
        startService(intent)
        /*
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_SMS,
                android.Manifest.permission.RECEIVE_SMS,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.READ_PHONE_STATE
        ), 1)

        makeStatusBarTransparent()
        listView = findViewById(R.id.listView)
        try {
            smsList.addAll(getSmsInPhone())
        }catch (e: Exception){}
        smsListAdapter = SmsListAdapter(this.layoutInflater, smsList)
        listView.adapter = smsListAdapter
        listView.setOnItemClickListener({ adapterView, _, i, _ ->
            val item = adapterView.getItemAtPosition(i) as Message
            AlertView(item.messagerTypeDescribe + item.name, item.body, "确定", null, null, this, AlertView.Style.Alert, null).setCancelable(true).show()
        })
        Toast.makeText(this, "OK", Toast.LENGTH_LONG).show()
        */
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        when (requestCode) {
            1 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //创建文件夹
                    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                        val file = File(Environment.getExternalStorageDirectory().toString() + "/Deliver")
                        if (!file.exists()) {
                            Log.d("log", "path1 create:" + file.mkdirs())
                        }
                    }
                }
                if (grantResults[2] == PackageManager.PERMISSION_GRANTED && smsList.size <= 0) {
                    smsList.addAll(getSmsInPhone())
                    smsListAdapter.notifyDataSetChanged()
                }
            }
        }
    }
    /*设置透明状态栏*/
    @SuppressLint("ObsoleteSdkInt")
    private fun makeStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.colorPrimary, theme)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    private fun getSmsInPhone(): MutableList<Message> {
        val smsUriAll = "content://sms/"

        val list : MutableList<Message> = listOf<Message>().toMutableList()
        try {
            val cr = contentResolver
            val projection = arrayOf("_id", "address", "person", "body", "date", "type")
            val uri = Uri.parse(smsUriAll)
            @SuppressLint("Recycle")
            val cur = cr.query(uri, projection, null, null, "date desc")

            if (cur!!.moveToFirst()) {
                var name: String
                var phoneNumber: String
                var smsbody: String?
                var type: Message.MessagerType

                val nameColumn = cur.getColumnIndex("person")
                val phoneNumberColumn = cur.getColumnIndex("address")
                val smsbodyColumn = cur.getColumnIndex("body")
                val dateColumn = cur.getColumnIndex("date")
                val typeColumn = cur.getColumnIndex("type")

                do {
                    name = cur.getString(nameColumn) ?: cur.getString(phoneNumberColumn)
                    phoneNumber = cur.getString(phoneNumberColumn)
                    smsbody = cur.getString(smsbodyColumn)
                    val d = Date(cur.getLong(dateColumn))

                    val typeId = cur.getInt(typeColumn)
                    when(typeId){
                        1 -> type = Message.MessagerType.Receive
                        2 -> type = Message.MessagerType.Send
                        else -> type = Message.MessagerType.Unknow
                    }

                    list.add(Message(name, phoneNumber, smsbody?: "", d, Message.MessageType.Messages, type))
                } while (cur.moveToNext())
            } else {
            }

        } catch (ex: SQLiteException) {
            Log.d("SQLiteException getSms", ex.message)
        }

        return list
    }
}
