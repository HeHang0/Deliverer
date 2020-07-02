package com.oo__h__oo.deliverer.manager

import com.oo__h__oo.deliverer.receiver.MyReceiver
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class ServerChanSender(extDir: String) {
    private var sendServerChanKey = ""

    init {
        sendServerChanKey = SysConfig.getConfig(extDir, SysConfig.SEND_SERVER_CHAN_KEY)
    }

    fun sendWeChatMsg(myReceiver: MyReceiver, myMessage: Message): Boolean{
        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        var sendOk = false
        try {
            val text = URLEncoder.encode(myMessage.messageTypeDescribe + myMessage.name,"utf-8")
            val desp =URLEncoder.encode(myMessage.messageFromTypeDescribe + myMessage.address + "\r\n" + myMessage.body,"utf-8")
            val url = URL("https://sc.ftqq.com/$sendServerChanKey.send?text=$text&desp=$desp")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            val inStream = connection.inputStream
            reader = BufferedReader(InputStreamReader(inStream))
            val allText = reader.use(BufferedReader::readText)
            sendOk = allText.startsWith("{\"errno\":0,")
            myReceiver.writeAllText("${dateFormat.format(Date())}.txt",allText)
        }catch (ex: Exception) {
            myReceiver.writeAllText("${dateFormat.format(Date())}.txt",ex.message.toString())
            ex.printStackTrace()
        } finally {
            reader?.close()
            connection?.disconnect()
        }
        return sendOk
//        Thread(Runnable {
//        }).start()
    }
}