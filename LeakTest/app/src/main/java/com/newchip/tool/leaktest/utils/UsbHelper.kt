package com.newchip.tool.leaktest.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import com.github.mjdev.libaums.UsbMassStorageDevice
import com.github.mjdev.libaums.fs.FileSystem
import com.github.mjdev.libaums.fs.UsbFile
import com.github.mjdev.libaums.fs.UsbFileInputStream
import com.github.mjdev.libaums.fs.UsbFileOutputStream
import com.github.mjdev.libaums.partition.Partition
import com.newchip.tool.leaktest.widget.USBBroadCastReceiver
import com.power.baseproject.common.UsbListener
import com.power.baseproject.utils.log.LogUtil
import com.power.baseproject.widget.NToast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


class UsbHelper(mContext: Context, listener: UsbListener) {
    //上下文对象
    private var context: Context = mContext

    //USB 设备列表
    private var storageDevices: Array<UsbMassStorageDevice>? = null

    //USB 广播
    private var mUsbReceiver: USBBroadCastReceiver? = null

    //回调
    private var usbListener: UsbListener = listener

    //当前路径
    private var currentFolder: UsbFile? = null
    var fileSystem:FileSystem? = null

    init {
        //注册广播
        registerReceiver()
    }

    /**
     * 注册 USB 监听广播
     */
    private fun registerReceiver() {
        mUsbReceiver = USBBroadCastReceiver()
        mUsbReceiver?.setUsbListener(usbListener)
        //监听otg插入 拔出
        val usbDeviceStateFilter = IntentFilter()
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        usbDeviceStateFilter.addAction(USBBroadCastReceiver.ACTION_USB_PERMISSION)
        context.registerReceiver(mUsbReceiver, usbDeviceStateFilter)
    }

    /**
     * 读取 USB设备列表
     *
     * @return USB设备列表
     */
    fun getDeviceList(): Array<UsbMassStorageDevice>? {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        //获取存储设备
        storageDevices = UsbMassStorageDevice.getMassStorageDevices(context)
        if (storageDevices.isNullOrEmpty()) {
            return null
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(USBBroadCastReceiver.ACTION_USB_PERMISSION),
            0
        )
        //可能有几个 一般只有一个 因为大部分手机只有1个otg插口
        for (device in storageDevices!!) {
            //有就直接读取设备是否有权限
            if (!usbManager.hasPermission(device.usbDevice)) {
                //没有权限请求权限
                usbManager.requestPermission(device.usbDevice, pendingIntent)
            }
        }
        return storageDevices
    }

    /**
     * 获取device 根目录文件
     *
     * @param device USB 存储设备
     * @return 设备根目录下文件列表
     */
    fun readDevice(device: UsbMassStorageDevice): MutableList<UsbFile> {
        val usbFiles = mutableListOf<UsbFile>()
        try {
            //初始化
            device.init()
            //获取partition
            val partitions: List<Partition> = device.partitions
            if (partitions.isEmpty()) {
                return usbFiles
            }
            // 仅使用第一分区
            val fs: FileSystem = partitions[0].fileSystem
            LogUtil.i("kevin", "分区名称: " + fs.volumeLabel)
            val root: UsbFile = fs.rootDirectory
            fileSystem = fs
            currentFolder = root
            usbFiles.addAll(root.listFiles())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return usbFiles
    }

    /**
     * 读取 USB 内文件夹下文件列表
     *
     * @param usbFolder usb文件夹
     * @return 文件列表
     */
    fun getUsbFolderFileList(usbFolder: UsbFile): MutableList<UsbFile> {
        //更换当前目录
        currentFolder = usbFolder
        val usbFiles = mutableListOf<UsbFile>()
        try {
            usbFiles.addAll(usbFolder.listFiles())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return usbFiles
    }

    /**
     * 复制文件到 USB
     *
     * @param targetFile       需要复制的文件
     * @param saveFolder       复制的目标文件夹
     * @param progressListener 下载进度回调
     * @return 复制结果
     */
    fun saveSDFileToUsb(
        targetFile: File,
        saveFolder: UsbFile,
        progressListener: DownloadProgressListener?
    ): Boolean {
        var result: Boolean
        try {
            //USB文件是否存在
            var isExist = false
            var saveFile: UsbFile? = null
            for (usbFile in saveFolder.listFiles()) {
                if (usbFile.name == targetFile.getName()) {
                    isExist = true
                    saveFile = usbFile
                }
            }
            if (isExist) {
                //文件已存在，删除文件
                saveFile?.delete()
            }
            //创建新文件
            saveFile = saveFolder.createFile(targetFile.name)
            //开始写入
            val fis = FileInputStream(targetFile) //读取选择的文件的
            val avi: Int = fis.available()
            val uos = UsbFileOutputStream(saveFile)
            var bytesRead: Int
            val buffer = ByteArray(1024 * 8)
            var writeCount = 0
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                uos.write(buffer, 0, bytesRead)
                writeCount += bytesRead
                progressListener?.downloadProgress(writeCount * 100 / avi)
            }
            uos.flush()
            fis.close()
            uos.close()
            result = true
        } catch (e: Exception) {
            e.printStackTrace()
            result = false
        }
        return result
    }

    /**
     * 复制 USB文件到本地
     *
     * @param targetFile       需要复制的文件
     * @param savePath         复制的目标文件路径
     * @param progressListener 下载进度回调
     * @return 复制结果
     */
    fun saveUSbFileToLocal(
        targetFile: UsbFile, savePath: String?,
        progressListener: DownloadProgressListener?
    ): Boolean {
        var result: Boolean
        try {
            //开始写入
            val uis = UsbFileInputStream(targetFile) //读取选择的文件的
            val fos = FileOutputStream(savePath)
            val avi = targetFile.length
            var writeCount = 0
            var bytesRead: Int
            val buffer = ByteArray(1024)
            while (uis.read(buffer).also { bytesRead = it } != -1) {
                fos.write(buffer, 0, bytesRead)
                writeCount += bytesRead
                progressListener?.downloadProgress((writeCount * 100 / avi).toInt())
            }
            fos.flush()
            uis.close()
            fos.close()
            result = true
        } catch (e: Exception) {
            e.printStackTrace()
            result = false
        }
        return result
    }

    /**
     * 获取上层目录文件夹
     *
     * @return usbFile : 父目录文件 / null ：无父目录
     */
    fun getParentFolder(): UsbFile? {
        return if (currentFolder != null && !currentFolder!!.isRoot) {
            currentFolder!!.parent
        } else {
            null
        }
    }


    /**
     * 获取当前 USBFolder
     */
    fun getCurrentFolder(): UsbFile? {
        return currentFolder
    }

    /**
     * 退出 UsbHelper
     */
    fun finishUsbHelper() {
        context.unregisterReceiver(mUsbReceiver)
    }

    /**
     * 下载进度回调
     */
    interface DownloadProgressListener {
        fun downloadProgress(progress: Int)
    }

    companion object {
        const val TAG = "UsbHelper"
    }
}