package com.hdtchat.vhf.player

import java.io.File

interface Player {
    fun startPlayer(lydFil: File)
    fun stopPlayer()
}