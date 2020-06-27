package com.oo__h__oo.deliverer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION
import android.telephony.SmsMessage
import android.telephony.TelephonyManager
import com.oo__h__oo.deliverer.MainActivity
import com.oo__h__oo.deliverer.manager.EmailSender
import com.oo__h__oo.deliverer.manager.Message
import com.oo__h__oo.deliverer.manager.SysConfig
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*


class MyReceiver : BroadcastReceiver() {

    private var dataDir = ""
    private var saveLog = false
    override fun onReceive(context: Context, intent: Intent) {
        if (dataDir.isBlank()){
            dataDir = context.getExternalFilesDir("")?.absolutePath ?: ""
        }
        try {
            if (intent.action != null) {
                val extDir = context.getExternalFilesDir("")?.absolutePath ?: ""
                saveLog = SysConfig.getLogConfig(extDir, SysConfig.SAVE_LOG)
                if (intent.action == SMS_RECEIVED_ACTION) {
                    val message = getMessageFromIntent(intent)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
                    writeAllText("${dateFormat.format(message.date)}.txt",message.address + ": " + message.body)
//                Toast.makeText(context, message.address + ": " + message.body, Toast.LENGTH_LONG).show();
                    Thread(Runnable {
                        EmailSender(extDir).sendEmail(this,message)
                    }).start()
                    val mIntent = Intent(MainActivity.ACTION_INTENT_RECEIVER)
                    context.sendBroadcast(mIntent)
                }else if (intent.action == PHONE_STATE){
                    val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                    if (state == RINGING){
                        val phoneNumber = intent.getStringExtra(EXTRA_INCOMING_NUMBER)
                                ?: return
                        val message = Message(phoneNumber,phoneNumber, state, Date(), Message.MessageType.Phone, Message.MessageFromType.Receive)
//                    Toast.makeText(context, message.address + ": " + message.body, Toast.LENGTH_LONG).show();
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
                        writeAllText("${dateFormat.format(message.date)}.txt",message.address + ": " + message.body)
                        Thread(Runnable {
                            EmailSender(extDir).sendEmail(this,message)
                        }).start()
                    }
                }
            }
        }catch (e:Exception){
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
            writeAllText("${dateFormat.format(Date())}.txt",e.message.toString())
        }

    }

    fun writeAllText(path: String, res: String) {
        if (dataDir.isBlank()){
            return
        }
        if (!saveLog){
            return
        }
        try {
            val dataDirFile = File(dataDir, "log")
            if (!dataDirFile.exists()) {
                dataDirFile.mkdirs()
            }
            val writeName = File(dataDirFile, path)
            if (!writeName.exists() || !writeName.canWrite()){
                writeName.createNewFile()
            }
            val out = BufferedWriter(FileWriter(writeName, true))
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
            out.write(dateFormat.format(Date()) + " : " + res + "\r\n")
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    companion object {
        /**
         *
         * Read the PDUs out of an [.SMS_RECEIVED_ACTION] or a
         * [.DATA_SMS_RECEIVED_ACTION] intent.
         *
         * @param intent
         * the intent to read from
         * @return an array of SmsMessages for the PDUs
         */
        fun getMessageFromIntent(intent: Intent): Message {
            val pdus = intent.extras?.get("pdus") as Array<*>
            var body = ""
            var phoneNumber = ""
            for (pdu in pdus) {
                //封装短信参数的对象
                @Suppress("DEPRECATION")
                val sms = SmsMessage.createFromPdu(pdu as ByteArray)
                phoneNumber = sms.originatingAddress.toString()
                body += sms.messageBody
            }
            val type = Message.MessageFromType.Receive
            return Message(phoneNumber, phoneNumber, body, Date(), Message.MessageType.Messages, type)
        }
        private const val PHONE_STATE = "android.intent.action.PHONE_STATE"
        private const val RINGING = "RINGING"
        private const val EXTRA_INCOMING_NUMBER = "incoming_number"
    }
}