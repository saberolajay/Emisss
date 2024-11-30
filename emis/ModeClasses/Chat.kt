package com.example.emis.ModeClasses

class Chat {
    private var sender: String = ""
    private var message: String = ""
    private var receiver: String = ""
    private var isseen: Boolean = false
    private var url: String = ""
    private var messageId: String = ""
    private var fileUrl: String = ""
    private var messageType: String = "" // Type of the message (text, image, file)
    private var fileName: String = "" // Store file name if applicable

    constructor()

    constructor(
        sender: String,
        message: String,
        receiver: String,
        isseen: Boolean,
        url: String,
        messageId: String,
        fileUrl: String,
        messageType: String,
        fileName: String
    ) {
        this.sender = sender
        this.message = message
        this.receiver = receiver
        this.isseen = isseen
        this.url = url
        this.messageId = messageId
        this.fileUrl = fileUrl
        this.messageType = messageType
        this.fileName = fileName
    }

    // Getter and setter methods
    fun getSender(): String {
        return sender
    }

    fun setSender(sender: String) {
        this.sender = sender
    }

    fun getMessage(): String {
        return message
    }

    fun setMessage(message: String) {
        this.message = message
    }

    fun getReceiver(): String {
        return receiver
    }

    fun setReceiver(receiver: String) {
        this.receiver = receiver
    }

    fun isIsSeen(): Boolean {
        return isseen
    }

    fun setIsSeen(isseen: Boolean) {
        this.isseen = isseen
    }

    fun getUrl(): String {
        return url
    }

    fun setUrl(url: String) {
        this.url = url
    }

    fun getMessageId(): String {
        return messageId
    }

    fun setMessageId(messageId: String) {
        this.messageId = messageId
    }

    fun getFileUrl(): String {
        return fileUrl
    }

    fun setFileUrl(fileUrl: String) {
        this.fileUrl = fileUrl
    }

    fun getMessageType(): String {
        return messageType
    }

    fun setMessageType(messageType: String) {
        this.messageType = messageType
    }

    fun getFileName(): String {
        return fileName
    }

    fun setFileName(fileName: String) {
        this.fileName = fileName
    }
}
