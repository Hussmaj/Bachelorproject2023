package com.hdtchat.wifip2p.chat

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.hdtchat.BR


class Comms: BaseObservable() {

    @get:Bindable
    var message: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.message)
        }
    @get:Bindable
    var sendMessage: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.sendMessage)
        }
}