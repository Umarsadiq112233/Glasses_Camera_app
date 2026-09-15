import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageProcessor {

    private const val MAX_LONG_SIDE = 7584 // 长边最大像素
    private const val TARGET_MAX_SIZE_KB = 2048 // 目标最大200KB
    private const val INIT_QUALITY = 95       // 初始压缩质量
    private const val MIN_QUALITY = 40        // 最低质量阈值

    fun processAndReplace(filePath: String): Boolean {
        val originalFile = File(filePath)
        if (!originalFile.exists()) return false

        var tempFile: File? = null
        var srcBitmap: Bitmap? = null
        var scaledBitmap: Bitmap? = null

        try {
            // 1. 创建临时文件
            tempFile = createTempFile(originalFile.parentFile).apply {
                deleteOnExit()
            }

            // 2. 解码（带内存优化）
            srcBitmap = decodeWithMemoryControl(originalFile)
//            if(srcBitmap.width>1920||srcBitmap.height>1080){
//                UserConfig.getInstance().camera8Mp=true
//            }else{
//                UserConfig.getInstance().camera8Mp=false
//            }

            // 3. 放大处理
            scaledBitmap = scaleBitmap(srcBitmap, 2.0f).apply {
                srcBitmap.recycle()
            }

            // 4. 压缩写入
            smartCompress(scaledBitmap, tempFile)

            // 5. 原子替换
            atomicReplace(tempFile, originalFile)
            return true
        } catch (e: Exception) {
            tempFile?.takeIf { it.exists() }?.delete()
            return false
        } finally {
            // 最终资源回收
            scaledBitmap?.recycle()
        }
    }
    private fun smartCompress(bitmap: Bitmap, dest: File) {
        val compressedBitmap = scaleToMaxSize(bitmap) // 尺寸压缩
        adaptiveQualityCompress(compressedBitmap, dest) // 质量压缩
        compressedBitmap.recycle()
    }

    private fun scaleToMaxSize(src: Bitmap): Bitmap {
        val scale = if (src.height > src.width) {
            MAX_LONG_SIDE / src.height
        } else {
            MAX_LONG_SIDE / src.width
        }.coerceAtMost(1)

        return Bitmap.createScaledBitmap(
            src,
            (src.width * scale).toInt(),
            (src.height * scale).toInt(),
            true
        )
    }

    private fun adaptiveQualityCompress(bitmap: Bitmap, dest: File) {
        var currentQuality = INIT_QUALITY
        var outputSize = Long.MAX_VALUE

        while (outputSize > TARGET_MAX_SIZE_KB * 1024 && currentQuality >= MIN_QUALITY) {
            saveBitmap(bitmap, dest, currentQuality)
            outputSize = dest.length()
            currentQuality -= 5
        }

        // 保底压缩
        if (outputSize > TARGET_MAX_SIZE_KB * 1024) {
            saveBitmap(bitmap, dest, MIN_QUALITY)
        }
    }


    // 安全解码（带内存控制）
    private fun decodeWithMemoryControl(file: File): Bitmap {
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(file.absolutePath, this)

            // 动态计算采样率
            inSampleSize = calculateInSampleSize(outWidth, outHeight)
            inJustDecodeBounds = false
            inPreferredConfig = Bitmap.Config.RGB_565

            BitmapFactory.decodeFile(file.absolutePath, this)
                ?: throw IOException("Decode failed")
        }
    }

    // 智能采样率计算
    private fun calculateInSampleSize(width: Int, height: Int): Int {
        val maxPixels = (Runtime.getRuntime().maxMemory() / 4).toInt()
        var sampleSize = 1
        while (width * height / (sampleSize * sampleSize) > maxPixels) {
            sampleSize *= 2
        }
        return sampleSize
    }

    // 高质量放大
    private fun scaleBitmap(src: Bitmap, scale: Float): Bitmap {
        return Bitmap.createScaledBitmap(
            src,
            (src.width * scale).toInt(),
            (src.height * scale).toInt(),
            true
        )
    }

    // 安全保存（自动关闭流）
    private fun saveBitmap(bitmap: Bitmap, dest: File, quality: Int) {
        FileOutputStream(dest).use { fos ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos)) {
                throw IOException("Compress failed")
            }
            fos.fd.sync()
        }
    }

    // 原子文件替换（带重试机制）
    private fun atomicReplace(temp: File, target: File) {
        repeat(3) { retry ->
            try {
                if (target.delete() && temp.renameTo(target)) return
            } catch (e: SecurityException) {
                if (retry == 2) throw e
                Thread.sleep(100)
            }
        }
        throw IOException("File replace failed")
    }

    // 创建临时文件
    private fun createTempFile(parentDir: File?): File {
        return File.createTempFile(
            "tmp_${System.currentTimeMillis()}",
            ".jpg",
            parentDir
        )
    }
}