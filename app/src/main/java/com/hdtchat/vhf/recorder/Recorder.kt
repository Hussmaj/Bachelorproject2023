package com.hdtchat.vhf.recorder

import java.io.File

interface Recorder {
    fun startRecord(lydFil: File)
    fun stopRecord()

}