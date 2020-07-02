package com.oo__h__oo.deliverer.manager

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class SysConfig {
    companion object {
        const val RECEIVE_USERNAME = "run"
        const val SEND_USERNAME = "sun"
        const val SEND_PASSWORD = "spw"
        const val SEND_HOST_ADDRESS = "sha"
        const val SEND_SERVER_CHAN_KEY = "ssck"
        const val SEND_EMAIL = "se"
        const val SEND_WECHAT = "sw"
        const val SAVE_LOG = "sl"


        fun saveConfig(dataDir: String,
                       receiveUserName: String,
                       sendUserName: String,
                       sendPassword: String,
                       sendHostAddress: String,
                       sendServerChanKey: String,
                       sendEmail: Boolean,
                       sendWechat: Boolean,
                       saveLog: Boolean) : String{
            val emailRegex = Regex("^[A-Za-z0-9\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")

            val dataDirFile = File(dataDir, "config")
            if (!dataDirFile.exists()) {
                if (!dataDirFile.mkdirs()){
                    return "出了点问题！"
                }
            }

            if (!emailRegex.matches(receiveUserName)){
                return "接收邮件的邮箱格式有问题"
            }
            if (!emailRegex.matches(sendUserName)){
                return "发送邮件的邮箱格式有问题"
            }
            if (sendHostAddress.isBlank()){
                return "发送邮件的服务器没填啊！"
            }

            if (!(writeFile(dataDirFile.absolutePath, RECEIVE_USERNAME, receiveUserName) &&
                            writeFile(dataDirFile.absolutePath, SEND_USERNAME, sendUserName) &&
                            writeFile(dataDirFile.absolutePath, SEND_PASSWORD, sendPassword) &&
                            writeFile(dataDirFile.absolutePath, SEND_HOST_ADDRESS, sendHostAddress) &&
                            writeFile(dataDirFile.absolutePath, SEND_SERVER_CHAN_KEY, sendServerChanKey) &&
                            writeBoolFile(dataDirFile.absolutePath, SEND_EMAIL, sendEmail) &&
                            writeBoolFile(dataDirFile.absolutePath, SEND_WECHAT, sendWechat) &&
                            writeBoolFile(dataDirFile.absolutePath, SAVE_LOG, saveLog))){
                return "出了点问题！"
            }


            return ""
        }

        fun getConfig(configDir: String, configName : String): String{
            val configFile = File(File(configDir, "config"), configName)
            if (configFile.exists() && configFile.canRead()){
                return configFile.readText()
            }
            return ""
        }

        fun getLogConfig(configDir: String, configName : String): Boolean{
            val configFile = File(File(configDir, "config"), configName)
            if (configFile.exists()){
                return true
            }
            return false
        }

        private fun writeFile(configDir: String, configName : String, configValue: String): Boolean{
            val configFile = File(configDir, configName)
            return try {
                if (!configFile.exists() || !configFile.canWrite()){
                    configFile.createNewFile()
                }
                val out = BufferedWriter(FileWriter(configFile))
                out.write(configValue)
                out.flush()
                out.close()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        private fun writeBoolFile(configDir: String, configName : String, configValue: Boolean): Boolean{
            val configFile = File(configDir, configName)
            return try {
                if (configFile.exists() && !configValue){
                    configFile.delete()
                }
                if (!configFile.exists() && configValue){
                    configFile.createNewFile()
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }


    }
}