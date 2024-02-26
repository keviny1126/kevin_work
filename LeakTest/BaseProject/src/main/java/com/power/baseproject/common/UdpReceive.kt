package com.power.baseproject.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket

class UdpReceive constructor(port: Int) {
    var socket = DatagramSocket(port)
    @Volatile var isRunning = true

    suspend fun startReceive(block: (content: String) -> Unit) {
        withContext(Dispatchers.IO) {
            while (isRunning) {
                val bytes = ByteArray(1024)
                val packet = DatagramPacket(bytes, bytes.size)
                socket.receive(packet)
                val content = packet.data.toString()
                block(content)
                delay(50)
            }
        }
    }

    fun close() {
        isRunning = false
        socket.close()
    }
}