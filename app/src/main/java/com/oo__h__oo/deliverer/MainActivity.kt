package com.oo__h__oo.deliverer


import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import com.bigkoo.alertview.AlertView
import com.oo__h__oo.deliverer.adapter.SmsListAdapter
import com.oo__h__oo.deliverer.manager.Message
import com.oo__h__oo.deliverer.manager.SysConfig
import com.oo__h__oo.deliverer.service.MyService
import java.util.*


class MainActivity : AppCompatActivity() {
    companion object{
        const val ACTION_INTENT_RECEIVER = "ACTION_INTENT_MSG_RECEIVER"
    }

    private var smsList : MutableList<Message> = listOf<Message>().toMutableList()
    private lateinit var smsListAdapter: SmsListAdapter
    private lateinit var refreshBtn :FloatingActionButton
    private var refreshList: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //启动Service
        val intent = Intent().setClass(this,MyService().javaClass)
        startService(intent)

        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_SMS,
                android.Manifest.permission.RECEIVE_SMS,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.READ_CALL_LOG
        ), 1)

        makeStatusBarTransparent()
        val listView = findViewById<ListView>(R.id.listView)
        smsListAdapter = SmsListAdapter(this.layoutInflater, smsList)
        listView.adapter = smsListAdapter
        listView.setOnItemClickListener { adapterView, _, i, _ ->
            val item = adapterView.getItemAtPosition(i) as Message
            AlertView(item.messageFromTypeDescribe + item.name, item.body, "确定", null, null, this, AlertView.Style.Alert, null).setCancelable(true).show()
        }
        refreshBtn = findViewById(R.id.refreshBtn)
        refreshBtn.setOnClickListener {
            displayMsg()
        }
        refreshBtn.setOnLongClickListener {
            displaySetting()
        }
        displayMsg()
        registerMessageReceiver()
    }

    //在销毁时要与广播解绑
    override fun onDestroy() {
        unregisterReceiver(mMessageReceiver)
        super.onDestroy()
    }


    private var mMessageReceiver: MessageReceiver? = null
    /**
     * 动态注册广播
     */
    private fun registerMessageReceiver() {
        mMessageReceiver = MessageReceiver()
        val filter = IntentFilter()
        filter.addAction(ACTION_INTENT_RECEIVER)
        registerReceiver(mMessageReceiver, filter)
    }

    inner class MessageReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == ACTION_INTENT_RECEIVER) {
                if (isForeground){
                    displayMsg()
                }else{
                    refreshList = true
                }
            }
        }
    }

    private var isForeground: Boolean = false

    override fun onResume() {
        super.onResume()
        isForeground = true
        if (refreshList){
            displayMsg()
        }
    }

    override fun onPause() {
        super.onPause()
        isForeground = false
    }


    private fun displayMsg(){
        try {
            smsList.clear()
            smsList.addAll(getSmsInPhone())
            smsListAdapter.notifyDataSetChanged()
        }catch (e: Exception){}
        refreshList = false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        when (requestCode) {
            1 -> {
                if (grantResults[2] == PackageManager.PERMISSION_GRANTED && smsList.size <= 0) {
                    displayMsg()
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
                var type: Message.MessageFromType

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

                    type = when(cur.getInt(typeColumn)){
                        1 -> Message.MessageFromType.Receive
                        2 -> Message.MessageFromType.Send
                        else -> Message.MessageFromType.Unknown
                    }

                    list.add(Message(name, phoneNumber, smsbody?: "", d, Message.MessageType.Messages, type))
                    if (list.size > 100) break
                } while (cur.moveToNext())
            }

        } catch (ex: SQLiteException) {
            ex.message?.let { Log.d("SQLiteException getSms", it) }
        }

        return list
    }


    private fun displaySetting(): Boolean {
        val extDir = applicationContext.getExternalFilesDir("")?.absolutePath ?: ""
        val view: View = LayoutInflater.from(this@MainActivity).inflate(R.layout.setting_dialog, null)
        val receiveUserNameET = view.findViewById<EditText>(R.id.receiveUserName)
        val sendUserNameET = view.findViewById<EditText>(R.id.sendUserName)
        val sendPasswordET = view.findViewById<EditText>(R.id.sendPassword)
        val sendHostAddressET = view.findViewById<EditText>(R.id.sendHostAddress)
        val sendServerChanKeyET = view.findViewById<EditText>(R.id.sendServerChanKey)
        val sendEmailCB = view.findViewById<CheckBox>(R.id.sendEmail)
        val sendWechatCB = view.findViewById<CheckBox>(R.id.sendWechat)
        val saveLogCB = view.findViewById<CheckBox>(R.id.saveLog)
        receiveUserNameET.setText(SysConfig.getConfig(extDir, SysConfig.RECEIVE_USERNAME))
        sendUserNameET.setText(SysConfig.getConfig(extDir, SysConfig.SEND_USERNAME))
        sendPasswordET.setText(SysConfig.getConfig(extDir, SysConfig.SEND_PASSWORD))
        sendHostAddressET.setText(SysConfig.getConfig(extDir, SysConfig.SEND_HOST_ADDRESS))
        sendServerChanKeyET.setText(SysConfig.getConfig(extDir, SysConfig.SEND_SERVER_CHAN_KEY))
        sendEmailCB.isChecked = SysConfig.getLogConfig(extDir, SysConfig.SEND_EMAIL)
        sendWechatCB.isChecked = SysConfig.getLogConfig(extDir, SysConfig.SEND_WECHAT)
        saveLogCB.isChecked = SysConfig.getLogConfig(extDir, SysConfig.SAVE_LOG)
        AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_launcher)
                .setTitle("邮箱配置")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton(R.string.confirm,
                    fun(_: DialogInterface?, _: Int) {
                        val receiveUserName = receiveUserNameET.text.toString().trim { it <= ' ' }
                        val sendUserName = sendUserNameET.text.toString().trim { it <= ' ' }
                        val sendPassword = sendPasswordET.text.toString().trim { it <= ' ' }
                        val sendHostAddress = sendHostAddressET.text.toString().trim { it <= ' ' }
                        val sendServerChanKey = sendServerChanKeyET.text.toString().trim { it <= ' ' }
                        val sendEmail = sendEmailCB.isChecked
                        val sendWechat = sendWechatCB.isChecked
                        val saveLog = saveLogCB.isChecked

                        val saveResult = SysConfig.saveConfig(extDir,
                                receiveUserName, sendUserName, sendPassword, sendHostAddress, sendServerChanKey, sendEmail, sendWechat, saveLog)
                        if (saveResult.isBlank()){
                            Toast.makeText(this@MainActivity, "可以了", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this@MainActivity, saveResult, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                .setNegativeButton(R.string.cancel,
                    fun(_: DialogInterface?, _: Int) {
                        Toast.makeText(this@MainActivity, "不设了啊！！！", Toast.LENGTH_SHORT).show()
                    }
                )
                .show()
        return true
    }
}
