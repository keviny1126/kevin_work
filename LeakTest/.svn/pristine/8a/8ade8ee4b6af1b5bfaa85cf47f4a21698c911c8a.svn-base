package com.power.baseproject.utils

import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import androidx.annotation.RequiresApi
import com.github.mjdev.libaums.fs.FileSystem
import com.github.mjdev.libaums.fs.UsbFile
import com.github.mjdev.libaums.fs.UsbFileStreamFactory
import com.power.baseproject.bean.FirmwareVersionBean
import com.power.baseproject.bean.UpdateInfoBean
import com.power.baseproject.utils.log.LogConfig
import com.power.baseproject.utils.log.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.*
import java.lang.reflect.Method
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


object FileUtils {

    const val TAG = "FileUtils"

    /**
     * 安全创建文件.
     * @param file
     * @return
     */
    fun createFileSafely(file: File): Boolean {
        var result = false
        if (!file.exists()) {
            val tmpPath = file.parent + File.separator + System.currentTimeMillis()
            val tmp = File(tmpPath)
            if (!tmp.exists()) {
                if (tmp.mkdirs()) {
                    result = tmp.renameTo(file)
                    tmp.delete()
                }
            }
        }
        return result
    }

    /**
     * 版本比较
     *
     * @param versionNo
     * @param maxOldVersion
     * @return
     */
    fun compareVersion(versionNo: String?, maxOldVersion: String?): Boolean {
        if (versionNo.isNullOrEmpty()) {
            return false
        }
        if (maxOldVersion.isNullOrEmpty()) {
            return true
        }
        var lastVersion = versionNo
        var curVersion = maxOldVersion
        if (versionNo.contains("V", ignoreCase = true)) {
            lastVersion = versionNo.replace("V", "", ignoreCase = true)
        }
        if (maxOldVersion.contains("V", ignoreCase = true)) {
            curVersion = maxOldVersion.replace("V", "", ignoreCase = true)
        }
        if (LogConfig.instance.isDebug) {
            if (versionNo == maxOldVersion) {
                return true
            }
        }
        return lastVersion.compareTo(curVersion, ignoreCase = true) > 0
    }

    fun checkVersion(versionNo: String?, maxOldVersion: String?): Boolean {
        if (versionNo.isNullOrEmpty()) {
            return false
        }
        if (maxOldVersion.isNullOrEmpty()) {
            return true
        }
        var lastVersion = versionNo
        var curVersion = maxOldVersion
        if (versionNo.contains("V", ignoreCase = true)) {
            lastVersion = versionNo.replace("V", "", ignoreCase = true)
        }
        if (maxOldVersion.contains("V", ignoreCase = true)) {
            curVersion = maxOldVersion.replace("V", "", ignoreCase = true)
        }
        var flag = false
        val appVersion1 = lastVersion.split(".")
        val appVersion2 = curVersion.split(".")
        //根据位数最短的判断
        val lim = if (appVersion1.size > appVersion2.size) appVersion2.size else appVersion1.size
        //根据位数循环判断各个版本
        for (i in 0 until lim) {
            if (appVersion1[i].toInt() > appVersion2[i].toInt()) {
                flag = true
                break
            }
        }
        return flag
    }

    private fun readZipFile(file: File, readFileName: String): String {
        val zipFile = ZipFile(file)
        val fileIs = FileInputStream(file)
        val zipIs = ZipInputStream(fileIs)

        val data = readZipFileNextEntry(zipFile, zipIs, readFileName)

        zipIs.close()
        fileIs.close()
        return data
    }

    private fun readZipFileNextEntry(
        zipFile: ZipFile,
        zipIs: ZipInputStream,
        readFileName: String
    ): String {
        var zipEntry = zipIs.nextEntry
        val bufSize = 1024
        while (zipEntry != null) {
            if (!zipEntry.isDirectory) {
                if (zipEntry.name.contains(readFileName)) {
                    val bos = ByteArrayOutputStream(bufSize)
                    var bin: BufferedInputStream? = null
                    return try {
                        bin = BufferedInputStream(zipFile.getInputStream(zipEntry))
                        val buffer = ByteArray(bufSize)
                        var len: Int
                        if (-1 != bin.read(buffer, 0, bufSize).also { len = it }) {
                            bos.write(buffer, 0, len)
                        }
                        String(bos.toByteArray())
                    } catch (e: IOException) {
                        e.printStackTrace()
                        throw e
                    } finally {
                        try {
                            bin?.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        bos.close()
                    }
                }
            }
            //关闭当前布姆
            zipIs.closeEntry();
            //读取下一个目录，作为循环条件
            zipEntry = zipIs.nextEntry
        }
        return ""
    }

    suspend fun readUpdateFile(
        downloadFile: File,
        infoName: String = ConstantsUtils.UPDATE_INFO_NAME
    ): UpdateInfoBean {
        val info = readZipFile(downloadFile, ConstantsUtils.UPDATE_FILE_INFO).trim()
        val infoBean = UpdateInfoBean(false)
        if (info.isNotEmpty()) {
            val js = JSONObject(info)
            if (!js.has("name")) {
                return infoBean
            }
            val name = js.get("name") as String
            if (name != infoName) {
                return infoBean
            }
            infoBean.name = name

            if (!js.has("version")) {
                return infoBean
            }
            val binVer = js.get("version") as String
            infoBean.version = binVer

            if (!js.has("size")) {
                return infoBean
            }
            val size = (js.get("size").toString()).toLongOrNull() ?: return infoBean
            infoBean.size = size

            if (!js.has("checkSum")) {
                return infoBean
            }
            val checkSum = (js.get("checkSum").toString()).toIntOrNull() ?: return infoBean
            infoBean.checkSum = checkSum

            if (js.has("hardwave")) {
                infoBean.hardwareVer = js.get("hardwave").toString()
            }
            infoBean.result = true
            LogUtil.i(
                TAG,
                "升级文件信息 name:$name === binVer:$binVer === size:$size == checkSum:$checkSum"
            )
        }
        return infoBean
    }

    fun copyAssetsFile(context: Context, toPath: String) {
        try {
            val fileNames = context.assets.list("")
            if (fileNames != null && fileNames.isNotEmpty()) {
                checkPathIsExists(toPath)
                for (fileName in fileNames) {
                    if (fileName == ConstantsUtils.BOOT_ANIMATION_ZIP) {
                        val filePath = toPath + fileName
                        deleteAllFile(toPath)
                        val inputStream = context.assets.open(fileName)
                        saveFile(inputStream, filePath)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun compareAndCopyFileAssets(
        context: Context,
        binVersion: String?,
        newPath: String?
    ): FirmwareVersionBean {
        val bean = FirmwareVersionBean(false)
        try {
            val fileNames = context.assets.list("")
            if (fileNames != null && fileNames.isNotEmpty()) {
                if (newPath == null) {
                    return bean
                }
                val file = File(newPath)
                file.mkdirs()
                for (fileName in fileNames) {
                    if (fileName.contains(ConstantsUtils.FACTORY_FIRMWARE_NAME)) {
                        val nameSplit = fileName.split("_")
                        val version = nameSplit[1]
                        val needUp = checkVersion(version, binVersion)
                        //需要升级copy文件
                        if (needUp) {
                            val filePath = newPath + fileName
                            val inputStream = context.assets.open(fileName)
                            deleteAllFile(newPath)
                            val result = saveFile(inputStream, filePath)
                            if (result) {
                                bean.result = true
                                bean.downloadPath = filePath
                                bean.curVersion = version
                                return bean
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bean
    }

    fun copyFile(formFilePath: String, toFilePath: String): Boolean {
        val fromFile = File(formFilePath)
        if (!fromFile.exists() || !fromFile.isFile || !fromFile.canRead()) {
            return false
        }
        return try {
            val inputStream = FileInputStream(fromFile)
            saveFile(inputStream, toFilePath)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 删除文件夹下所有文件
     */
    fun deleteAllFile(path: String) {
        try {
            val fileDir = File(path)
            if (fileDir.exists()) {
                val fileList = fileDir.listFiles()
                if (fileList != null) {
                    for (file in fileList) {
                        if (file.isFile) {
                            file.delete()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 保存数据流到本地
     *
     * @param instream
     * 数据流
     * @param filePath
     * 文件路径
     * @return
     */
    fun saveFile(instream: InputStream?, filePath: String): Boolean {
        val file = File(filePath)
        var buffer: FileOutputStream? = null
        try {
            if (instream != null) {
                buffer = FileOutputStream(file)
                val tmp = ByteArray(1024)
                var length: Int
                while (instream.read(tmp).also { length = it } != -1) {
                    buffer.write(tmp, 0, length)
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return false
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } finally {
            try {
                instream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                buffer?.flush()
                buffer?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return true
    }

    fun checkPathIsExists(path: String?): Boolean {
        try {
            if (path.isNullOrEmpty()) {
                return false
            }
            val fileDir = File(path)
            if (!fileDir.exists()) {
                return fileDir.mkdirs()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun checkFileIsExists(fileNamePath: String?): Boolean {
        try {
            if (fileNamePath.isNullOrEmpty()) {
                return false
            }
            val fileDir = File(fileNamePath)
            if (fileDir.exists()) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 根据label获取外部存储路径(此方法适用于android7.0以上系统)
     * @param context
     * @param label 内部存储:Internal shared storage    SD卡:SD card    USB:USB drive(USB storage)
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun getExternalPath(context: Context, label: String): String {
        var path = ""
        val mStorageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        //获取所有挂载的设备（内部sd卡、外部sd卡、挂载的U盘）
        val volumes = mStorageManager.storageVolumes //此方法是android 7.0以上的
        try {
            val storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
            //通过反射调用系统hide的方法
            val getPath: Method = storageVolumeClazz.getMethod("getPath")
            val isRemovable: Method = storageVolumeClazz.getMethod("isRemovable")
            //       Method getUserLabel = storageVolumeClazz.getMethod("getUserLabel");//userLabel和description是一样的
            for (i in volumes.indices) {
                val storageVolume = volumes[i] //获取每个挂载的StorageVolume
                // 通过反射调用getPath、isRemovable、userLabel
                val storagePath = getPath.invoke(storageVolume) as String //获取路径
                val isRemovableResult = isRemovable.invoke(storageVolume) as Boolean //是否可移除
                val description = storageVolume.getDescription(context) //此方法是android 7.0以上的
                if (label == description) {
                    path = storagePath
                    break
                }
                LogUtil.d(
                    TAG + "getExternalPath--",
                    " i=$i ,storagePath=$storagePath ,description=$description"
                )
            }
        } catch (e: java.lang.Exception) {
            LogUtil.d(TAG + "getExternalPath--", " e:$e")
        }
        LogUtil.d(TAG + "getExternalPath--", " path:$path")
        return path
    }

    /**
     * 删除文件
     *
     * @param path
     * @return
     */
    fun deleteFile(path: String): Boolean {
        try {
            val file = File(path)
            if (file.exists()) {
                if (file.delete()) {
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 删除文件夹以及目录下的文件
     * @param   filePath 被删除目录的文件路径
     * @return  目录删除成功返回true，否则返回false
     */
    fun deleteDirectory(path: String): Boolean {
        var result = true
        try {
            var filePath = path
            var flag: Boolean
            //如果filePath不以文件分隔符结尾，自动添加文件分隔符
            if (!filePath.endsWith(File.separator)) {
                filePath += File.separator
            }
            val dirFile = File(filePath)
            if (!dirFile.exists() || !dirFile.isDirectory) {
                return false
            }
            flag = true
            val files = dirFile.listFiles()
            if (!files.isNullOrEmpty()) {
                //遍历删除文件夹下的所有文件(包括子目录)
                for (i in files.indices) {
                    if (files[i].isFile) {
                        //删除子文件
                        flag = deleteFile(files[i].absolutePath)
                        if (!flag) break
                    } else {
                        //删除子目录
                        flag = deleteDirectory(files[i].absolutePath)
                        if (!flag) break
                    }
                }
                result = flag
            }
        } catch (e: Exception) {
            e.printStackTrace()
            result = false
        }
        return result
    }

    /**
     * 根据路径删除指定的目录或文件，无论存在与否
     * @param filePath  要删除的目录或文件
     * @return 删除成功返回 true，否则返回 false。
     */
    fun deleteFolder(filePath: String): Boolean {
        val file = File(filePath)
        return if (!file.exists()) {
            false
        } else {
            if (file.isFile) {
                // 为文件时调用删除文件方法
                deleteFile(filePath)
            } else {
                // 为目录时调用删除目录方法
                deleteDirectory(filePath)
            }
        }
    }

    /**
     *
     * 压缩文件和文件夹
     *
     *
     *
     * @param srcFileString 要压缩的文件或文件夹
     *
     * @param zipFileString 压缩完成的Zip路径
     *
     * @throws Exception
     */
    suspend fun ZipFolder(srcFileString: String, zipFileString: String) =
        withContext(
            Dispatchers.IO
        ) {
            var outZip: ZipOutputStream? = null
            try {
                //创建ZIP
                outZip = ZipOutputStream(FileOutputStream(zipFileString))
                //创建文件
                val file = File(srcFileString)
                //压缩
                ZipFiles(file.parent + File.separator, file.name, outZip)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                //完成和关闭
                try {
                    outZip?.flush()
                    outZip?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                LogUtil.d("kevin", "<--压缩结束-->")
            }

        }

    fun ZipUsbFolder(
        srcFileString: String,
        zipFileString: UsbFile,
        fileSystem: FileSystem
    ): Boolean {
        var outZip: ZipOutputStream? = null
        var result = true
        try {
            checkPathIsExists(srcFileString)
            //创建ZIP
            outZip = ZipOutputStream(
                UsbFileStreamFactory.createBufferedOutputStream(
                    zipFileString,
                    fileSystem
                )
            )
            //创建文件
            val file = File(srcFileString)
            //压缩
            ZipFiles(file.parent + File.separator, file.name, outZip)
        } catch (e: Exception) {
            e.printStackTrace()
            result = false
        } finally {
            //完成和关闭
            try {
                outZip?.flush()
                outZip?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            LogUtil.d("kevin", "<--压缩结束-->")
        }
        return result
    }

    /**
     *
     * 压缩文件
     *
     *
     *
     * @param folderString
     *
     * @param fileString
     *
     * @param zipOutputSteam
     *
     * @throws Exception
     */
    @Throws(java.lang.Exception::class)
    fun ZipFiles(
        folderString: String,
        fileString: String,
        zipOutputSteam: ZipOutputStream?
    ) {
        LogUtil.d(
            "kevin",
            "folderString:" + folderString + "\n" +
                    "fileString:" + fileString + "\n=========================="
        )
        if (zipOutputSteam == null) return
        val file = File(folderString + fileString)
        if (file.isFile) {
            val zipEntry = ZipEntry(fileString)
            val inputStream = FileInputStream(file)
            zipOutputSteam.putNextEntry(zipEntry)
            var len: Int
            val buffer = ByteArray(4096)
            while ((inputStream.read(buffer).also { len = it }) != -1) {
                zipOutputSteam.write(buffer, 0, len)
            }
            zipOutputSteam.closeEntry()
        } else {
            //文件夹
            val fileList = file.list()
            //没有子文件和压缩
            if (fileList.isEmpty()) {
                val zipEntry = ZipEntry(fileString + File.separator)
                zipOutputSteam.putNextEntry(zipEntry)
                zipOutputSteam.closeEntry()
            }
            //子文件和递归
            for (i in fileList.indices) {
                ZipFiles("$folderString$fileString/", fileList[i], zipOutputSteam)
            }
        }
    }

    fun saveUsbFolderToLocal(
        usbFile: UsbFile,
        toFilePath: String,
        fileSystem: FileSystem
    ): Boolean {
        val result: Boolean = try {
            deleteFile(toFilePath)
            //创建ZIP
            val inputStream = UsbFileStreamFactory.createBufferedInputStream(
                usbFile,
                fileSystem
            )
            saveFile(inputStream, toFilePath)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        return result
    }
}