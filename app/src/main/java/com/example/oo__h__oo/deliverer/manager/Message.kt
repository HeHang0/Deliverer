package com.example.oo__h__oo.deliverer.manager

import java.util.*

class Message {
    constructor(
            name: String,
            address: String,
            body: String,
            date: Date,
            messageType: MessageType,
            messagerType: MessagerType){
        this.name = name
        this.address = address
        this.body = body
        this.date = date
        this.messageType = messageType
        this.messagerType = messagerType
    }
    public var name: String
    public var address: String
    public var body: String
    public var date: Date
    public var messageType: MessageType
    public var messagerType: MessagerType
    public var messageTypeDescribe: String = ""
        get() {
            when(messageType){
                MessageType.Phone -> return "电话: "
                MessageType.Messages -> return "信息: "
            }
        }
    public var messagerTypeDescribe: String = ""
        get() {
            when(messagerType){
                MessagerType.Send -> return "To: "
                MessagerType.Receive -> return "From: "
                MessagerType.Unknow -> return "Unknow: "
            }
        }

    enum class MessagerType{
        Send, Receive, Unknow
    }
    enum class MessageType{
        Messages, Phone
    }
}