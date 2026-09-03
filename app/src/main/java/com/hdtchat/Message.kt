package com.hdtchat

class Message {
    var message: String? =null
    var senderId: String? =null
    constructor(){}

    constructor(message: String?, senderId: Boolean){
        this.message =message
        this.senderId= senderId.toString()
    }
}