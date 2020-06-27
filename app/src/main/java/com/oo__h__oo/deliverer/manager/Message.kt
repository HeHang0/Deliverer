package com.oo__h__oo.deliverer.manager

import java.util.*


class Message(var name: String?, var address: String?, var body: String, var date: Date, var messageType: MessageType, var messageFromType: MessageFromType) {
    var messageTypeDescribe: String = ""
        get() {
            return when(messageType){
                MessageType.Phone -> "电话: "
                MessageType.Messages -> "信息: "
            }
        }
    var messageFromTypeDescribe: String = ""
        get() {
            return when(messageFromType){
                MessageFromType.Send -> "To: "
                MessageFromType.Receive -> "From: "
                MessageFromType.Unknown -> "Unknown: "
            }
        }

    enum class MessageFromType{
        Send, Receive, Unknown
    }
    enum class MessageType{
        Messages, Phone
    }
}