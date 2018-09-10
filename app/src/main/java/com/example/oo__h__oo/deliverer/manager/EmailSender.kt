package com.example.oo__h__oo.deliverer.manager

import com.example.oo__h__oo.deliverer.receiver.MyReceiver
import java.text.SimpleDateFormat
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.Message.RecipientType.TO


class EmailSender {
    private val sendUserName = "hehang724590957@yeah.net"// 发送邮件需要连接的服务器的用户名
    private val receiveUserName = "1058946828@qq.com"// 发送邮件需要连接的服务器的用户名
    private val sendPassword = "HEHANG724590957"// 发送邮件需要连接的服务器的密码
    private val sendProtocol = "smtp"// 发送邮件使用的端口
    private val sendHostAddress = "smtp.yeah.net"// 发送邮件使用的服务器的地址

    fun sendEmail(myReceiver: MyReceiver,myMessage: com.example.oo__h__oo.deliverer.manager.Message) {
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
            val mailContent = myMessage.messagerTypeDescribe + myMessage.address + "\r\n" + myMessage.body
            mailMessage.setText(mailContent)
            // 发送邮件
            Transport.send(mailMessage)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            myReceiver.writeAllText("${dateFormat.format(Date())}.txt","已发送！")

        }catch (e: Exception){
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            myReceiver.writeAllText("${dateFormat.format(Date())}.txt",e.message.toString())
            e.printStackTrace();

        }
    }
}