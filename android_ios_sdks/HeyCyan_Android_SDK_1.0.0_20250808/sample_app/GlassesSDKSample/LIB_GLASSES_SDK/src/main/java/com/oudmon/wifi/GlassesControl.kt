package com.oudmon.wifi
import android.app.Application
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.oudmon.ble.base.bluetooth.BleOperateManager
import com.oudmon.ble.base.bluetooth.DeviceManager
import com.oudmon.ble.base.communication.ILargeDataResponse
import com.oudmon.ble.base.communication.LargeDataHandler
import com.oudmon.ble.base.communication.bigData.resp.GlassModelControlResponse
import com.oudmon.ble.base.communication.bigData.resp.GlassesDeviceNotifyListener
import com.oudmon.ble.base.communication.bigData.resp.GlassesDeviceNotifyRsp
import com.oudmon.ble.base.communication.utils.ByteUtil
import com.oudmon.qc_utils.date.DateUtil
import com.oudmon.wifi.LogTag.TAG
import com.oudmon.wifi.bean.DownloadSpeedCalculator
import com.oudmon.wifi.bean.GlassAlbumEntity
import com.oudmon.wifi.bean.PictureDownloadBean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Arrays
import java.util.concurrent.BlockingDeque
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import kotlin.math.roundToInt

class GlassesControl(private val application: Application) :WifiP2pManagerSingleton.WifiP2pCallback {
    private val coreSize = Runtime.getRuntime().availableProcessors() + 1
    private val fix: ExecutorService = Executors.newFixedThreadPool(coreSize)
    private val singleBle: ExecutorService = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var dirPath: String
    private var importing = false
    private val timeoutRunnable: MyRunnable= MyRunnable()
    private val heartbeatRunnable: HeartBeatRunnable= HeartBeatRunnable()
    private var failRetry = 0
    private val p2pConnectFailRunnable = P2pConnectFailRunnable()
    private var systemSuccess = false
    private var bleCallbackSuccess = false
    private var isP2PConnecting = false
    private lateinit var deviceNotifyListener: MyDeviceNotifyListener
    private var glassDeviceWifiIP=""
    private var configFileName: String = "media.config"
    private var fileQueue: BlockingDeque<PictureDownloadBean> = LinkedBlockingDeque(100)
    private val recordQueue: BlockingDeque<GlassAlbumEntity> = LinkedBlockingDeque(50)
    private var wifiFilesDownloadListener: WifiFilesDownloadListener? = null
    private var totalFiles = 0
    private var lastUpdateTime = 0L
    private var intervalTime = 500
    private var camera8Mp=false
    private var opusToPcmIng = false

    fun setWifiDownloadListener(wifiFilesDownloadListener: WifiFilesDownloadListener) {
        this.wifiFilesDownloadListener = wifiFilesDownloadListener
    }



    fun initGlasses(dirPath: String){
        this.dirPath = dirPath
        deviceNotifyListener = MyDeviceNotifyListener()
        LargeDataHandler.getInstance().addOutDeviceListener(2, deviceNotifyListener)
        WifiP2pManagerSingleton.getInstance(application).addCallback(this)
        WifiP2pManagerSingleton.getInstance(application).registerReceiver()
    }


    internal inner class MyRunnable : Runnable {
        override fun run() {
            tvImportBtnReset()
        }
    }

    internal inner class HeartBeatRunnable : Runnable {
        override fun run() {
            if (BleOperateManager.getInstance().isConnected) {
                LargeDataHandler.getInstance().syncHeartBeat(0x04)
                handler.postDelayed(heartbeatRunnable, 5000)
            }else{
                handler.removeCallbacks(heartbeatRunnable)
            }
        }
    }

    internal inner class P2pConnectFailRunnable : Runnable {
        override fun run() {
            importing = false
            if (failRetry < 1) {
                try {
                    Log.i(TAG, "p2p连接超时,准备再次走流程")
                    systemSuccess = false
                    bleCallbackSuccess = false
                    importAlbum()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                failRetry++
            } else {
                handler.removeCallbacks(p2pConnectFailRunnable)
                tvImportBtnReset()
            }
        }
    }

    private fun tvImportBtnReset() {
        importing = false
    }

    fun importAlbum() {
        importing = true
        handler.postDelayed(timeoutRunnable, (10 * 1000).toLong())
        WifiP2pManagerSingleton.getInstance(application).startPeerDiscovery()
        LargeDataHandler.getInstance().glassesControl(
            byteArrayOf(0x02, 0x01, 0x04), object : ILargeDataResponse<GlassModelControlResponse> {
                override fun parseData(cmdType: Int, response: GlassModelControlResponse) {
                    if (response.dataType == 1 && response.glassWorkType == 4) {
                        if (response.errorCode == 0) {
                            when (response.workTypeIng) {
                                2 -> {
                                    wifiFilesDownloadListener?.onGlassesFail(2)
                                }
                                5 -> {
                                    wifiFilesDownloadListener?.onGlassesFail(5)
                                }
                                1, 6 -> {
                                    wifiFilesDownloadListener?.onGlassesFail(1)
                                }
                                7 -> {
                                    wifiFilesDownloadListener?.onGlassesFail(7)
                                }
                                8 -> {
                                    wifiFilesDownloadListener?.onGlassesFail(8)
                                }
                            }
                            handler.removeCallbacks(timeoutRunnable)
                            tvImportBtnReset()
                            return
                        }
                        handler.removeCallbacks(timeoutRunnable)
                        handler.postDelayed(p2pConnectFailRunnable, (40 * 1000).toLong())
                        WifiP2pManagerSingleton.getInstance(application).resetFailCount()
                        initHeartBeat()
                        LargeDataHandler.getInstance().removeGlassesControlCallback()
                        wifiFilesDownloadListener?.onGlassesControlSuccess()
                    }
                }
            }
        )
    }

    private fun initHeartBeat() {
        handler.removeCallbacks(heartbeatRunnable)
        handler.postDelayed(heartbeatRunnable, 5000)
    }


    inner class MyDeviceNotifyListener : GlassesDeviceNotifyListener() {

        override fun parseData(cmdType: Int, response: GlassesDeviceNotifyRsp?) {
            when (response!!.loadData[6].toInt()) {
                0x08 -> {
                    val p2pIp = ByteUtil.byteToInt(response.loadData[7])
                        .toString() + "." + ByteUtil.byteToInt(
                        response.loadData[8]
                    ) + "." + ByteUtil.byteToInt(response.loadData[9]) + "." + ByteUtil.byteToInt(
                        response.loadData[10]
                    )
                    Log.i(TAG,"蓝牙返回ip:$p2pIp")
                    glassDeviceWifiIP = p2pIp
                    bleCallbackSuccess = true
                    downloadMediaConfig()
                }

                0x09 -> {
                    val error =
                        (ByteUtil.bytesToInt(Arrays.copyOfRange(response.loadData, 7, 8)))
                    Log.i(TAG,"蓝牙返回错误:$error")
                    if (error == 0xff) {
                        WifiP2pManagerSingleton.getInstance(application).resetDeviceP2p()
                        handler.removeCallbacks(p2pConnectFailRunnable)
                        tvImportBtnReset()
                    }
                }
            }
        }
    }

    companion object {
        private var glassesControl: GlassesControl? = null
        fun getInstance(application: Application): GlassesControl? {
            if (glassesControl == null) {
                synchronized(BleOperateManager::class.java) {
                    if (glassesControl == null) {
                        glassesControl = GlassesControl(application)
                    }
                }
            }
            return glassesControl
        }
    }

    override fun onWifiP2pEnabled() {
        Log.i(TAG, "onWifiP2pEnabled")
    }

    override fun onWifiP2pDisabled() {
        Log.i(TAG, "onWifiP2pDisabled")
    }

    override fun onPeersChanged(peers: Collection<WifiP2pDevice>) {
        if (peers.isNotEmpty()) {
            Log.i(TAG,"wifi p2p设备:" + peers.size + "-----" + !isP2PConnecting)
            if (!isP2PConnecting) {
                for (item in peers) {
                    if (item.deviceName.equals(
                            DeviceManager.getInstance().wifiName,
                            true
                        ) || item.deviceName.endsWith(DeviceManager.getInstance().deviceAddress.replace(":", ""))
                    ) {
                        Log.i(TAG,"匹配到设备:" + item.deviceName)
                        WifiP2pManagerSingleton.getInstance(application).connectToDevice(item)
                        isP2PConnecting = true
                        break
                    }
                }
            }
        }
    }

    override fun onConnected(info: WifiP2pInfo?) {
        if (info != null) {
            Log.i(TAG,buildString {
                append("onConnectionInfoAvailable")
                append("\n")
                append("groupFormed: " + info.groupFormed)
                append("\n")
                append("isGroupOwner: " + info.isGroupOwner)
                append("\n")
                append("groupOwnerAddress hostAddress: " + info.groupOwnerAddress.hostAddress)
            })
            Log.i(TAG,"isGroupOwner:" + info.isGroupOwner)

            systemSuccess = true
            if (!info.isGroupOwner) {
                val ip = info.groupOwnerAddress.hostAddress
                if (ip != null) {
                    glassDeviceWifiIP = ip
                    downloadMediaConfig()
                }
            }
        }
    }

    override fun onDisconnected() {
        Log.i(TAG,"P2P组网不可用:onDisconnected")
        isP2PConnecting = false
    }

    override fun onThisDeviceChanged(device: WifiP2pDevice?) {
        if (device != null) {
            Log.i(TAG,device.deviceName + "-----" + device.deviceAddress)
        }
    }

    override fun onPeerDiscoveryStarted() {
        isP2PConnecting = false
        Log.i(TAG,"P2P 开始扫描")
    }

    override fun onPeerDiscoveryFailed(reason: Int) {
        Log.i(TAG,"P2P 扫描失败原因:$reason")
    }

    override fun onConnectRequestSent() {
        Log.i(TAG,"P2P 发送连接请求成功")
    }

    override fun onConnectRequestFailed(reason: Int) {
        Log.i(TAG,"P2P 发送连接请求失败:$reason")
    }

    override fun connecting() {
    }

    override fun cancelConnect() {
        Log.i(TAG,"P2P cancelConnect success")
    }

    override fun cancelConnectFail(reason: Int) {
        Log.i(TAG,"P2P cancelConnectFail: $reason")
    }

    override fun retryAlsoFailed() {
        Log.i(TAG,"cancelConnectFail")
        tvImportBtnReset()
    }


    private fun downloadMediaConfig() {
        if (systemSuccess && bleCallbackSuccess) {
            handler.removeCallbacks(heartbeatRunnable)
            WifiP2pManagerSingleton.getInstance(application).setConnect(true)
            importing = true
            failRetry = 0
            val ip = glassDeviceWifiIP
            handler.removeCallbacks(p2pConnectFailRunnable)
            systemSuccess = false
            bleCallbackSuccess = false
            ktxRunOnUiDelay(1 * 1000) {
                getPhotoTextFile(
                    "http://$ip/files/$configFileName",
                    dirPath, configFileName
                )
            }
        } else {
            Log.i(TAG,"systemSuccess:$systemSuccess,bleCallbackSuccess:$bleCallbackSuccess")
        }
    }

    fun <T> T.ktxRunOnUiDelay(delayMillis: Long, block: T.() -> Unit) {
        handler.postDelayed({
            block()
        }, delayMillis)
    }


    fun <T> T.ktxRunOnBgSingleBle(block: T.() -> Unit) {
        singleBle.execute {
            block()
        }
    }

    /**
     * 子线程执行。SingleThreadPool
     */
    fun <T> T.ktxRunOnBgSingle( block: T.() -> Unit) {
        val single = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        val job = CoroutineScope(single).launch {
            withTimeout(2000) {
                block()
            }
        }
        // 等待任务完成或超时
        runBlocking {
            job.join()
        }.runCatching {
            this.toString()
        }
    }

    fun <T> T.ktxRunOnBgFix(block: T.() -> Unit) {
        fix.execute {
            block()
        }
    }

    fun fileExists(path: String):Boolean{
        return File(path).exists()
    }

    fun deleteFile(path: String?): Boolean {
        val file = File(path)
        if (file.exists() && file.isFile) {
            file.delete()
            return true
        }
        return false
    }

    fun getPhotoTextFile(url: String, dirPath: String, fileName: String) {
        ktxRunOnBgSingle {
            if (fileExists("$dirPath/$fileName")) {
                deleteFile("$dirPath/$fileName")
            }
            var retryCount = 0
            val maxRetries = 1
            fun startDownload() {
                AndroidNetworking.download(url, dirPath, fileName)
                    .setTag("photo.txt")
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .startDownload(object : DownloadListener {
                        override fun onDownloadComplete() {
                             Log.i(LogTag.TAG,"download photo text success")
                            fileQueue.clear()
                            readPhotoFile("$dirPath/$fileName")
                        }

                        override fun onError(error: ANError) {
                             Log.i(LogTag.TAG,error.errorCode.toString())
                             Log.i(LogTag.TAG,error.errorDetail)
                            if (retryCount < maxRetries) {
                                retryCount++
                                 Log.i(LogTag.TAG,"Download failed, retrying ($retryCount/$maxRetries)")
                                startDownload()
                            } else {
                                if (wifiFilesDownloadListener != null) {
                                    wifiFilesDownloadListener!!.fileDownloadError(
                                        1,
                                        error.errorCode
                                    )
                                }
                            }
                        }
                    })
            }
            startDownload()
        }
    }

    private fun readPhotoFile(path: String) {
        val file = File(path)
        try {
            val lines = file.readLines()
             Log.i(LogTag.TAG,"总文件数:" + lines.size)
            fileQueue = LinkedBlockingDeque(lines.size + 10)
            for (line in lines) {
                val filePath =
                    "http://$glassDeviceWifiIP/files/$line"
                val downloadBean = PictureDownloadBean(
                    filePath,
                    line
                )

                fileQueue.putLast(downloadBean)
            }
            totalFiles = lines.size
            ktxRunOnBgSingle {
                downloadGlassFile()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun downloadGlassFile() {
         Log.i(LogTag.TAG,"还剩下几个文件:" + fileQueue.size)
        if (fileQueue.isEmpty()) {
            wifiFilesDownloadListener!!.fileDownloadComplete()
            return
        } else {
            wifiFilesDownloadListener!!.fileCount((totalFiles - fileQueue.size + 1), totalFiles)
        }
        val bean = fileQueue.take()
        var lastReportedProgress = -1
        //计算下载速度
        val calculator = DownloadSpeedCalculator()
        calculator.start()
        lastUpdateTime=0
         Log.i(LogTag.TAG,bean.path)
        AndroidNetworking.download(bean.path, dirPath, bean.fileName)
            .setTag("download_file")
            .setPriority(Priority.MEDIUM)
            .build()
            .setDownloadProgressListener { bytesDownloaded, totalBytes ->
                if (totalBytes > 0) {
                    val currentTime = System.currentTimeMillis()
                    val isExactHalf = bytesDownloaded == totalBytes / 2
                    if (currentTime - lastUpdateTime >= intervalTime ||isExactHalf || bytesDownloaded == totalBytes) {
                        lastUpdateTime = currentTime
                        val speed = calculator.calculate(bytesDownloaded)
//                         Log.i(LogTag.TAG,"下载速度: $speed")
                        if (wifiFilesDownloadListener != null) {
                            if (speed != "-1") {
                                wifiFilesDownloadListener!!.wifiSpeed(speed)
                            }
                        }
                    }
                    val progress = (bytesDownloaded * 100 / totalBytes).toInt()
                    if (progress > lastReportedProgress || progress == 100) {
                        lastReportedProgress = progress
                        if (wifiFilesDownloadListener != null) {
                            wifiFilesDownloadListener!!.fileProgress(bean.fileName, progress)
                        }
                    }
                }
            }
            .startDownload(object : DownloadListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDownloadComplete() {
                    if (bean.fileName.endsWith(".log")) {
                        downloadGlassFile()
                        return
                    }
                     Log.i(LogTag.TAG,"download photo text success:" + bean.fileName)
                    val file = File(dirPath, bean.fileName)
                    if (file.length().toInt() == 0) {
                        deleteFile(file.absolutePath)
                    } else {
                        val fileType = identifyFileType(file.absolutePath)
                        var duration = 0
                        var firstFrame = ""
                        var eisInProgress = false
                        if(fileType ==1){
                            ktxRunOnBgFix {
                               ImageProcessor.processAndReplace(file.absolutePath)
                            }
                        }else if (fileType == 2) {
                            eisInProgress = true
                            duration = getVideoDuration(file.absolutePath)
                            val bitmap = loadVideoFirstFrame(file.absolutePath)
                            if (bitmap != null) {
                                firstFrame = saveBitmapToFolder(dirPath,
                                    bitmap,
                                    "first_frame_" + bean.fileName.split(".")[0] + ".png"
                                )
                            }
                        }
                        val entity = GlassAlbumEntity(
                            file.name,
                            DeviceManager.getInstance().deviceAddress,
                            file.absolutePath,
                            firstFrame,
                            fileType,
                            duration,
                            DateUtil(file.lastModified(), false).y_M_D,
                            DateUtil(file.lastModified(), false).timestamp,
                            0,
                            0,
                            editSelect = false,
                            eisInProgress = eisInProgress
                        )

                        if(fileType == 3){
                            ktxRunOnBgFix {
                                recordQueue.put(entity)
                                ktxRunOnUiDelay(2000) {
                                    opusToPcm()
                                }
                            }
                        }
                        if (wifiFilesDownloadListener != null) {
                            wifiFilesDownloadListener!!.fileWasDownloadSuccessfully(entity)
                        }
//                        if (fileType == 2) {
//                            try {
//                                ktxRunOnBgFix {
//                                    val eisFileName = "eis_" + bean.fileName
//                                    val eis = Mp4Decode()
//                                    eis.eisInit()
//                                    val outFile = File(dirPath, eisFileName)
//                                    eis.eisYuv2Mp4(file, outFile, eisCallback)
//                                     Log.i(LogTag.TAG,"eis fileName:$eisFileName")
//                                }
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            }
//                        }
                    }
                    downloadGlassFile()
                }

                override fun onError(error: ANError) {
                     Log.i(LogTag.TAG,error.errorCode.toString())
                     Log.i(LogTag.TAG,error.errorDetail.toString())
                    if (wifiFilesDownloadListener != null) {
                        wifiFilesDownloadListener!!.fileDownloadError(2, error.errorCode)
                    }
                    downloadGlassFile()
                }
            })
    }

    fun identifyFileType(filePath: String): Int {
        return if(filePath.endsWith("jpg")|| filePath.endsWith("jpeg")){
            1
        }else if(filePath.endsWith("mp4") || filePath.endsWith("avi")){
            2
        }else if(filePath.endsWith("opus")){
            3
        }else{
            1
        }
    }

    private fun loadVideoFirstFrame(videoPath: String): Bitmap? {
        try {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(videoPath)
                val bitmap = retriever.getFrameAtTime(
                    1,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
                return bitmap
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                retriever.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getVideoDuration(videoPath: String): Int {
        val retriever = MediaMetadataRetriever()
        try {
            // 设置数据源
            retriever.setDataSource(videoPath)
            // 获取视频时长（单位：毫秒）
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val width = widthStr?.toIntOrNull() ?: 0
            val height = heightStr?.toIntOrNull() ?: 0
            camera8Mp = if(width>1920||height>1080){
                true
            }else{
                false
            }
            if(durationStr!!.toIntOrNull()!!<=1000){
                return 1
            }
            return durationStr.toIntOrNull()!!.div(1000)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // 释放资源
            retriever.release()
        }
        return 0
    }

    fun saveBitmapToFolder(path: String,bitmap: Bitmap, fileName: String): String {
        val dir = File(path)
        if (!dir.exists() && !dir.mkdirs()) {
            return ""
        }
        val file = File(dir, fileName)
        try {
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
                return file.absolutePath
            }
        } catch (e: IOException) {
          Log.e(TAG,"Error saving bitmap: " + e.message)
            return ""
        }
    }


    private fun opusToPcm() {
        if (recordQueue.isNotEmpty()) {
            if (!opusToPcmIng) {
                Log.i(TAG,"opusToPcm")
                opusToPcmIng = true
                val entity = recordQueue.take()
//                decodeOpusStream(entity)
            } else {
                Log.i(TAG,"opusToPcm 正在进行中")
            }
        }

    }


//    fun decodeOpusStream(entity: GlassAlbumEntity) {
//        val name = entity.fileName.split(".")[0] + ".pcm"
//        val outFile = dirPath + "/" + name
//        val opusOption = OpusOption()
//        opusOption.setHasHead(false)
//        opusOption.setSampleRate(16 * 1000)
//        opusOption.setPacketSize(40)
//        opusOption.setChannel(1)
//        val mOpusManager = OpusManager()
//        mOpusManager.decodeFile(entity.filePath, outFile, opusOption, object : OnStateCallback {
//            override fun onStart() {
//                Log.i(TAG,"开始解码")
//            }
//
//            override fun onComplete(p0: String) {
//                Log.i(TAG,"解码结束 >> $p0")
//                opusToPcmIng = false
//                entity.filePath = "$dirPath/$name"
//                val duration = calculatePCMPlaybackDuration(File(entity.filePath).length())
//                Log.i(TAG,"recording length:$duration")
//                entity.videoLength = duration
//                wifiFilesDownloadListener?.recordingToPcm(
//                    entity.fileName,
//                    entity.filePath,
//                    duration.toInt()
//                )
//                mOpusManager.stopEncodeStream()
//                opusToPcm()
//
//            }
//
//            override fun onError(code: Int, message: String) {
//                try {
//                    opusToPcmIng = false
//                    Log.i(TAG,"$code, $message")
//                    recordQueue.put(entity)
//                    wifiFilesDownloadListener?.recordingToPcmError(entity.fileName, message)
//                } catch (e: java.lang.Exception) {
//                    e.printStackTrace()
//                }
//            }
//        })
//    }


    fun calculatePCMPlaybackDuration(totalBytes: Long): Int {
        val sampleRate = 16000
        val bytesPerSample = 2
        val bytesPerSecond = sampleRate * bytesPerSample
        val seconds = totalBytes.toDouble() / bytesPerSecond
        return (seconds * 1000).roundToInt()
    }


    interface WifiFilesDownloadListener {
        fun onGlassesControlSuccess()
        fun onGlassesFail(errorCode: Int)
        fun wifiSpeed(wifiSpeed: String)
        fun fileProgress(fileName: String, progress: Int)
        fun fileWasDownloadSuccessfully(entity: GlassAlbumEntity)
        fun fileCount(index: Int, total: Int)
        fun fileDownloadComplete()
        fun fileDownloadError(fileType: Int, errorType: Int)
        fun eisEnd(fileName: String, filePath: String)
        fun eisError(fileName: String,sourcePath: String, errorInfo: String)
        fun recordingToPcm(fileName: String, filePath: String, duration: Int)
        fun recordingToPcmError(fileName: String, errorInfo: String)
    }
}
