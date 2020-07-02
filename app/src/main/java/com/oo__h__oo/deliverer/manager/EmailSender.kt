package com.oo__h__oo.deliverer.manager

import com.oo__h__oo.deliverer.receiver.MyReceiver
import java.text.SimpleDateFormat
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


class EmailSender(extDir: String) {
    private var sendUserName = "***@yeah.net"// 接收邮件需要连接的服务器的用户名
    private var receiveUserName = "***@qq.com"// 发送邮件需要连接的服务器的用户名
    private var sendPassword = "***"// 发送邮件需要连接的服务器的密码
    private var sendProtocol = "smtp"// 发送邮件使用的协议
    private var sendHostAddress = "smtp.yeah.net"// 发送邮件使用的服务器的地址
    private var alreadConfig = false

    init {
        readConfig(extDir)
    }

    private fun readConfig(extDir :String){
        receiveUserName = SysConfig.getConfig(extDir, SysConfig.RECEIVE_USERNAME)
        sendUserName= SysConfig.getConfig(extDir, SysConfig.SEND_USERNAME)
        sendPassword= SysConfig.getConfig(extDir, SysConfig.SEND_PASSWORD)
        sendHostAddress= SysConfig.getConfig(extDir, SysConfig.SEND_HOST_ADDRESS)
        alreadConfig = !(receiveUserName.isBlank() || sendUserName.isBlank() || sendHostAddress.isBlank())
    }

    fun sendEmail(myReceiver: MyReceiver,myMessage: Message): Boolean {
        if (!alreadConfig){
            return false
        }
        val properties = Properties()
        properties.setProperty("mail.smtp.auth", "true")// 服务器需要认证
        properties.setProperty("mail.transport.protocol", sendProtocol)// 声明发送邮件使用的端口
        properties.setProperty("mail.host", sendHostAddress)// 发送邮件的服务器地址
        try {
            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(sendUserName, sendPassword)
                }
            })
            //session.debug = true//同意在当前线程的控制台打印与服务器对话信息

            // 根据session创建一个邮件消息
            val mailMessage = MimeMessage(session)
            // 创建邮件发送者地址
            val from = InternetAddress(sendUserName)
            // 设置邮件消息的发送者
            mailMessage.setFrom(from)
            // 创建邮件的接收者地址，并设置到邮件消息中
            val to = InternetAddress(receiveUserName)
            mailMessage.setRecipient(MimeMessage.RecipientType.TO, to)
            // 设置邮件消息的主题
            mailMessage.setSubject(myMessage.messageTypeDescribe + myMessage.name)
            // 设置邮件消息发送的时间
            mailMessage.setSentDate(Date())

            // 设置邮件消息的主要内容
            val mailContent = myMessage.messageFromTypeDescribe + myMessage.address + "\r\n" + myMessage.body
            mailMessage.setText(mailContent)
            // 发送邮件
            Transport.send(mailMessage)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
            myReceiver.writeAllText("${dateFormat.format(Date())}.txt","已发送！")
            return true

        }catch (e: Exception){
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
            myReceiver.writeAllText("${dateFormat.format(Date())}.txt",e.message.toString())
            e.printStackTrace();
            return false
        }
    }
}