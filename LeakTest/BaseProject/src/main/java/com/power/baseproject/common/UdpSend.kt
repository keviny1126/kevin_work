package com.power.baseproject.common

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class UdpSend constructor(port: Int) {
    private var socket: DatagramSocket? = null
    private var mPort = port

    fun onBroadcastSend(data: ByteArray) {
        socket = DatagramSocket()
        val inetAddress = InetAddress.getByName("255.255.255.255")
        socket?.use {
            val packet = DatagramPacket(data, data.size, inetAddress, mPort)
            it.send(packet)
            it.broadcast = true
        }
    }

    fun onSend(data: ByteArray, ip: String) {
        socket = DatagramSocket()
        val iAddress = InetAddress.getByName(ip)
        socket?.use {
            val packet = DatagramPacket(data, data.size, iAddress, mPort)
            it.send(packet)
        }
    }

}